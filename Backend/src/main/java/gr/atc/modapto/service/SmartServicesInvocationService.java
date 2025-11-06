package gr.atc.modapto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.config.properties.KeycloakProperties;
import gr.atc.modapto.config.properties.SmartServiceDebugProperties;
import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import static gr.atc.modapto.exception.CustomExceptions.*;

import gr.atc.modapto.dto.dt.SmartServiceRequest;
import gr.atc.modapto.dto.dt.SmartServiceResponse;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.service.interfaces.IModaptoModuleService;
import gr.atc.modapto.service.processors.NoOpResponseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SmartServicesInvocationService {

    private final Logger logger = LoggerFactory.getLogger(SmartServicesInvocationService.class);

    private final RestClient restClient;

    private final KeycloakProperties keycloakProperties;

    private final SmartServiceDebugProperties debugProperties;

    private final IModaptoModuleService modaptoModuleService;

    private final NoOpResponseProcessor noOpResponseProcessor;

    private final ObjectMapper objectMapper;

    @Value("${dt.management.url}")
    private String dtmUrl;

    private static final String TOKEN = "access_token";
    private static final String MODAPTO_HEADER = "X-MODAPTO-Invocation-Id";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public SmartServicesInvocationService(RestClient restClient,
                                         KeycloakProperties keycloakProperties,
                                         SmartServiceDebugProperties debugProperties,
                                         IModaptoModuleService modaptoModuleService,
                                         NoOpResponseProcessor noOpResponseProcessor,
                                         ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.keycloakProperties = keycloakProperties;
        this.debugProperties = debugProperties;
        this.modaptoModuleService = modaptoModuleService;
        this.noOpResponseProcessor = noOpResponseProcessor;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate a JWT Token to access Keycloak resources
     *
     * @return Token or null if authentication fails
     */
    private String retrieveComponentJwtToken() {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", keycloakProperties.clientId());
            formData.add("client_secret", keycloakProperties.clientSecret());
            formData.add("grant_type", "client_credentials");

            ResponseEntity<Map<String, Object>> response = restClient.post()
                    .uri(keycloakProperties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {});

            return Optional.of(response)
                    .filter(resp -> resp.getStatusCode().is2xxSuccessful())
                    .map(ResponseEntity::getBody)
                    .filter(body -> body.get(TOKEN) != null)
                    .map(body -> body.get(TOKEN).toString())
                    .orElse(null);
                    
        } catch (RestClientException e) {
            logger.error("Rest Client error during authenticating the client: Error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Invoke smart service via DTM using POST request
     *
     * @param smartServiceId Identifier for the smart service
     * @param moduleId Module identifier
     * @param invocationData Generic input data for the service
     * @param modaptoHeader MODAPTO header for the request
     * @param <T> Type of the input data
     * @return DtmResponseDto containing the response from DTM
     * @throws SmartServiceInvocationException if invocation fails
     */
    public <T> ResponseEntity<DtResponseDto> invokeSmartService(String smartServiceId,
                                                                String moduleId,
                                                                T invocationData, 
                                                                ModaptoHeader modaptoHeader) {
        
        logger.debug("Invoking smart service: {} for module: {} with header: {}", smartServiceId, moduleId, modaptoHeader);
        
        // Validate input parameters
        if (smartServiceId == null || smartServiceId.trim().isEmpty()) {
            throw new SmartServiceInvocationException("Smart service ID cannot be null or empty");
        }
        
        if (moduleId == null || moduleId.trim().isEmpty()) {
            throw new SmartServiceInvocationException("Module ID cannot be null or empty");
        }

        
        if (invocationData == null) {
            throw new SmartServiceInvocationException("Invocation data cannot be null");
        }
        
        if (modaptoHeader == null) {
            throw new SmartServiceInvocationException("MODAPTO header cannot be null");
        }
        
        // Retrieve JWT token for authentication
        String jwtToken = retrieveComponentJwtToken();
        if (jwtToken == null) {
            throw new SmartServiceInvocationException("Failed to retrieve JWT token for DTM authentication");
        }
        
        String smartServiceUrl = retrieveSmartServiceUrl(smartServiceId, moduleId);
        StringBuilder uri = new StringBuilder().append(extractUriFromUrl(smartServiceUrl)).append("/invoke/$value");
        logger.debug("URI: {}, Invocation Data: {}", uri, invocationData);

        // Store request body as JSON file for debugging if enabled
        storeRequestBodyAsJson(invocationData, smartServiceId, moduleId);

        try {
            RestClient.ResponseSpec responseSpec = restClient.post()
                    .uri(uri.toString())
                    .header("Authorization", "Bearer " + jwtToken)
                    .header(MODAPTO_HEADER, modaptoHeader.toString())
                    .body(invocationData)
                    .retrieve();

            return responseSpec
                    .onStatus(HttpStatusCode::is4xxClientError, (request, errorResponse) -> {
                        throw new DtmClientErrorException("Client error invoking smart service: " + smartServiceId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, errorResponse) -> {
                        throw new DtmServerErrorException("Server error invoking smart service: " + smartServiceId);
                    })
                    .toEntity(DtResponseDto.class);
                    
        } catch (Exception e) {
            logger.error("Error invoking smart service: {} for module: {} - {}", smartServiceId, moduleId, e.getMessage());
            throw new SmartServiceInvocationException("Unable to invoke smart service - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve smart service URL from ModaptoModule repository based on smartServiceId and moduleId
     *
     * @param smartServiceId Identifier for the smart service
     * @param moduleId Module identifier
     * @return Full URL for the smart service
     * @throws DtmClientErrorException if service URL cannot be retrieved
     */
    private String retrieveSmartServiceUrl(String smartServiceId, String moduleId) {
        logger.debug("Retrieving smart service URL for service: {} and module: {}", smartServiceId, moduleId);
        
        // Validate input parameters
        if (smartServiceId == null || smartServiceId.trim().isEmpty()) {
            throw new DtmClientErrorException("Smart service ID cannot be null or empty");
        }
        
        if (moduleId == null || moduleId.trim().isEmpty()) {
            throw new DtmClientErrorException("Module ID cannot be null or empty");
        }
        
        try {
            String serviceUrl = modaptoModuleService.retrieveSmartServiceUrl(moduleId, smartServiceId);
            
            if (serviceUrl == null || serviceUrl.trim().isEmpty()) {
                throw new DtmClientErrorException("Retrieved smart service URL is null or empty for service: " + smartServiceId + " and module: " + moduleId);
            }
            
            logger.debug("Successfully retrieved smart service URL: {} for service: {} and module: {}", serviceUrl, smartServiceId, moduleId);
            return serviceUrl;
            
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found - service: {} and module: {} - {}", smartServiceId, moduleId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving smart service URL for service: {} and module: {} - {}", smartServiceId, moduleId, e.getMessage());
            throw new DtmClientErrorException("Failed to retrieve smart service URL: " + e.getMessage());
        }
    }

    /**
     * Extract URI path from the full smart service URL
     *
     * @param smartServiceUrl Full URL of the smart service
     * @return URI path to be used with RestClient
     */
    private String extractUriFromUrl(String smartServiceUrl) {
        if (smartServiceUrl != null && dtmUrl != null && smartServiceUrl.startsWith(dtmUrl)) {
            String uri = smartServiceUrl.substring(dtmUrl.length());
            return uri.startsWith("/") ? uri : "/" + uri;
        }
        throw new SmartServiceInvocationException("Invalid smart service URL: '" + smartServiceUrl +"'. URL must be a combination of DTM URL.");
    }

    /**
     * Store the request body as a JSON file for debugging and inspection purposes
     *
     * @param requestBody The request body object to serialize and store
     * @param smartServiceId Identifier for the smart service
     * @param moduleId Module identifier
     * @param <T> Type of the request body
     */
    private <T> void storeRequestBodyAsJson(T requestBody, String smartServiceId, String moduleId) {
        if (!debugProperties.storeRequestJson()) {
            logger.debug("JSON request storage is disabled. Skipping...");
            return;
        }

        try {
            // Create output directory if it doesn't exist
            Path outputDir = Paths.get(debugProperties.jsonOutputDirectory());
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
                logger.info("Created JSON output directory: {}", outputDir.toAbsolutePath());
            }

            // Generate filename with timestamp, module ID, and smart service ID
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String sanitizedModuleId = moduleId.replaceAll("[^a-zA-Z0-9_-]", "_");
            String sanitizedServiceId = smartServiceId.replaceAll("[^a-zA-Z0-9_-]", "_");
            String filename = String.format("%s_%s_%s_request.json", timestamp, sanitizedModuleId, sanitizedServiceId);

            Path filePath = outputDir.resolve(filename);

            // Serialize request body to pretty-printed JSON
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(requestBody);

            // Write to file
            Files.writeString(filePath, jsonContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Stored request JSON to file: {}", filePath.toAbsolutePath());

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize request body to JSON for service: {} and module: {} - {}",
                    smartServiceId, moduleId, e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to write request JSON file for service: {} and module: {} - {}",
                    smartServiceId, moduleId, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error storing request JSON for service: {} and module: {} - {}",
                    smartServiceId, moduleId, e.getMessage());
        }
    }


    /**
     * Common algorithm processing and invocation logic for Async Processing
     *
     * @param invocationData: Input data object
     * @param algorithmType: Type of algorithm for logging purposes
     */
    void formulateAndImplementSmartServiceRequest(Object invocationData, String route, String algorithmType) {
        SmartServiceRequest request;
        try {
            // Check CRF Case Input Format structure
            String encodedInput;
            if (invocationData instanceof CrfInvocationInputDto crfOptData)
                encodedInput = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(crfOptData.getData()).getBytes());
            else
                encodedInput = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(invocationData).getBytes());

            // Route corresponds to AUEB services, thus the format changes
            if (route != null){
                request = SmartServiceRequest.builder()
                        .request(encodedInput)
                        .route(route)
                        .build();
            } else {
                request = SmartServiceRequest.builder()
                        .request(encodedInput)
                        .build();
            }


        } catch (JsonProcessingException e) {
            logger.error("Unable to convert {} input to Base64 Encoding", algorithmType);
            throw new SmartServiceInvocationException("Unable to convert " + algorithmType + " input to Base64 Encoding");
        }

        try {
            // Get smartServiceId and moduleId using reflection
            String smartServiceId = (String) invocationData.getClass().getMethod("getSmartServiceId").invoke(invocationData);
            String moduleId = (String) invocationData.getClass().getMethod("getModuleId").invoke(invocationData);

            // Wrap Smart Service Input data to DtInputDto
            DtInputDto<SmartServiceRequest> dtInput = DtInputDto.<SmartServiceRequest>builder()
                    .inputArguments(request)
                    .build();

            // Invoke smart service
            ResponseEntity<DtResponseDto> response = invokeSmartService(
                    smartServiceId,
                    moduleId,
                    dtInput,
                    ModaptoHeader.ASYNC
            );

            logger.debug("Successfully invoked {} algorithm", algorithmType);

            // Just discard the response as it will be handled via the MB
            noOpResponseProcessor.processResponse(response, moduleId, smartServiceId);
        } catch (Exception e) {
            logger.error("Error invoking {} algorithm: {}", algorithmType, e.getMessage());
            throw new SmartServiceInvocationException("Error invoking " + algorithmType + " algorithm");
        }
    }

    /*
     * Helper method to Decode Digital Twin response to specific class
     */
    public <T> T decodeDigitalTwinResponseToDto(Class<T> clazz, DtResponseDto response, String smartService){
        try {
            logger.debug("Digital Twin response: {}", response);
            // Convert output arguments to specific Smart Service results DTO
            SmartServiceResponse serviceResponse = objectMapper.convertValue(
                    response.getOutputArguments(),
                    SmartServiceResponse.class
            );

            // Decode Response from Base64 Encoding to specific DTO
            byte[] decodedBytes = Base64.getDecoder().decode(serviceResponse.getResponse());
            T outputDto = objectMapper.readValue(decodedBytes, clazz);
            return outputDto;
        } catch (Exception e) {
            logger.error("Error processing Digital Twin response for {} - Error: {}", smartService, e.getMessage());
            throw new SmartServiceInvocationException("Failed to process Digital Twin response for " + smartService + ": " + e.getMessage());
        }
    }

    /*
     * Helper method to validate whether the DT Response is valid
     */
    public boolean validateDigitalTwinResponse(ResponseEntity<DtResponseDto> response, String smartService){
        // Validate response
        if (response == null || response.getBody() == null) {
            throw new DtmServerErrorException("No response received from DTM service for requested operation");
        }

        DtResponseDto dtmResponse = response.getBody();

        if (dtmResponse == null) {
            logger.error("DTM service response body is null for {}", smartService);
            throw new DtmServerErrorException("DTM service returned a null body for requested operation");
        }

        if (!dtmResponse.isSuccess()) {
            logger.error("DTM service execution failed for requested operation. Messages: {}",
                    Optional.ofNullable(dtmResponse.getMessages()).orElse(List.of("No error messages provided")));
            throw new DtmServerErrorException("DTM service execution failed for "+ smartService +" operation");
        }

        return true;
    }

    /*
     * Helper method to implement SYNC request to Smart Services via DT
     */
    public <T> ResponseEntity<DtResponseDto> formulateAndImplementSyncSmartServiceRequest(T inputData, String moduleId, String smartServiceId) {
        if (inputData == null)
            return null;

        // Encode the invocationData to Base64
        String encodedInput;
        try {
            encodedInput = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(inputData).getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        SmartServiceRequest smartServiceRequest = SmartServiceRequest.builder()
                .request(encodedInput)
                .build();

        // Wrap invocation data in DtInputDto
        DtInputDto<SmartServiceRequest> dtInput = DtInputDto.<SmartServiceRequest>builder()
                .inputArguments(smartServiceRequest)
                .build();

        // Invoke smart service using the generic service
        return invokeSmartService(
                smartServiceId,
                moduleId,
                dtInput,
                ModaptoHeader.SYNC
        );
    }
}

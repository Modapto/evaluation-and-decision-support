package gr.atc.modapto.service;

import gr.atc.modapto.config.properties.KeycloakProperties;
import gr.atc.modapto.dto.dt.DtResponseDto;
import static gr.atc.modapto.exception.CustomExceptions.*;

import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.service.interfaces.IModaptoModuleService;
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

import java.util.Map;
import java.util.Optional;

@Service
public class SmartServicesInvocationService {

    private final Logger logger = LoggerFactory.getLogger(SmartServicesInvocationService.class);

    private final RestClient restClient;
    
    private final KeycloakProperties keycloakProperties;
    
    private final IModaptoModuleService modaptoModuleService;

    @Value("${dt.management.url}")
    private String dtmUrl;
    
    private static final String TOKEN = "access_token";
    private static final String MODAPTO_HEADER = "X-MODAPTO-Invocation-Id";

    public SmartServicesInvocationService(RestClient restClient, KeycloakProperties keycloakProperties, IModaptoModuleService modaptoModuleService) {
        this.restClient = restClient;
        this.keycloakProperties = keycloakProperties;
        this.modaptoModuleService = modaptoModuleService;
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
}

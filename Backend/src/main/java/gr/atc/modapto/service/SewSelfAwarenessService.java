package gr.atc.modapto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.dt.SmartServiceRequest;
import gr.atc.modapto.dto.dt.SmartServiceResponse;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceInvocations.SewLocalAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessRealTimeMonitoringInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewFilteringOptionsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessRealTimeMonitoringResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.exception.CustomExceptions.*;
import gr.atc.modapto.exception.CustomExceptions.DtmServerErrorException;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.exception.CustomExceptions.SmartServiceInvocationException;
import gr.atc.modapto.model.sew.SewMonitorKpisComponents;
import gr.atc.modapto.repository.SewMonitorKpisComponentsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessMonitoringKpisResultsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessRealTimeMonitoringResultsRepository;
import gr.atc.modapto.service.interfaces.ISewSelfAwarenessService;

@Service
public class SewSelfAwarenessService implements ISewSelfAwarenessService {

    private static final Logger logger = LoggerFactory.getLogger(SewSelfAwarenessService.class);

    private final SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository;

    private final SewSelfAwarenessRealTimeMonitoringResultsRepository sewSelfAwarenessRealTimeMonitoringResultsRepository;

    private final SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandler;

    private final ObjectMapper objectMapper;

    private final ModelMapper modelMapper;

    public SewSelfAwarenessService(SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository,
                                   SewSelfAwarenessRealTimeMonitoringResultsRepository sewSelfAwarenessRealTimeMonitoringResultsRepository,
                                   SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository,
                                   SmartServicesInvocationService smartServicesInvocationService,
                                   ExceptionHandlerService exceptionHandler,
                                   ModelMapper modelMapper,
                                   ObjectMapper objectMapper){
        this.sewSelfAwarenessMonitoringKpisResultsRepository = sewSelfAwarenessMonitoringKpisResultsRepository;
        this.sewSelfAwarenessRealTimeMonitoringResultsRepository = sewSelfAwarenessRealTimeMonitoringResultsRepository;
        this.sewMonitorKpisComponentsRepository = sewMonitorKpisComponentsRepository;
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.exceptionHandler = exceptionHandler;
        this.modelMapper = modelMapper;
        this.objectMapper =objectMapper;
    }

    /**
     * Invoke Self Awareness Monitoring KPIs algorithm (Self-Awareness 1)
     *
     * @param invocationData: Input Data
     */
    @Override
    public void invokeSelfAwarenessMonitoringKpisAlgorithm(SewSelfAwarenessMonitoringKpisInputDto invocationData) {
        // Retrieve and set component data
        setComponentDataForInvocation(invocationData);

        // Invoke the algorithm
        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, null,"Self-Awareness Monitoring KPIs");
    }

    /**
     * Retrieve Latest Self Awareness Monitoring KPI Results
     *
     * @return SewSelfAwarenessMonitoringKpisResultsDto
     */
    @Override
    public SewSelfAwarenessMonitoringKpisResultsDto retrieveLatestSelfAwarenessMonitoringKpisResults() {
        return exceptionHandler.handleOperation(() -> sewSelfAwarenessMonitoringKpisResultsRepository.findFirstByOrderByTimestampDesc()
                .map(result -> modelMapper.map(result, SewSelfAwarenessMonitoringKpisResultsDto.class))
                .orElseThrow(() -> new ResourceNotFoundException("There are no available SEW Self-Awareness Monitoring KPIs results")), "retrieveLatestSelfAwarenessMonitoringKpisResults");
    }

    /**
     * Retrieve Latest Self Awareness Monitoring KPI Results by Module ID
     *
     * @param moduleId : ID of Module
     * @return SewSelfAwarenessMonitoringKpisResultsDto
     */
    @Override
    public SewSelfAwarenessMonitoringKpisResultsDto retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(String moduleId) {
        return exceptionHandler.handleOperation(() -> sewSelfAwarenessMonitoringKpisResultsRepository.findFirstByModuleIdOrderByTimestampDesc(moduleId)
                .map(result -> modelMapper.map(result, SewSelfAwarenessMonitoringKpisResultsDto.class))
                .orElseThrow(() -> new ResourceNotFoundException("There are no available SEW Self-Awareness Monitoring KPIs results for Module ID: " + moduleId)), "retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId");
    }


    /**
     * Retrieve all Self Awareness Monitoring KPI Results
     *
     * @return List<SewSelfAwarenessMonitoringKpisResultsDto>
     */
    @Override
    public List<SewSelfAwarenessMonitoringKpisResultsDto> retrieveAllSelfAwarenessMonitoringKpisResults() {
        return exceptionHandler.handleOperation(() -> sewSelfAwarenessMonitoringKpisResultsRepository.findAll(Pageable.unpaged())
                .getContent()
                .stream()
                .map(result -> modelMapper.map(result, SewSelfAwarenessMonitoringKpisResultsDto.class))
                .toList(), "retrieveAllSelfAwarenessMonitoringKpisResults");
    }

    /**
     * Retrieve all Self Awareness Monitoring KPI Results by Module ID
     *
     * @param moduleId : ID of Module
     * @return List<SewSelfAwarenessMonitoringKpisResultsDto>
     */
    @Override
    public List<SewSelfAwarenessMonitoringKpisResultsDto> retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(String moduleId) {
        return exceptionHandler.handleOperation(() -> sewSelfAwarenessMonitoringKpisResultsRepository.findByModuleId(moduleId,Pageable.unpaged())
                .getContent()
                .stream()
                .map(result -> modelMapper.map(result, SewSelfAwarenessMonitoringKpisResultsDto.class))
                .toList(), "retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId");
    }

    /**
     * Invoke Self Awareness Real-Time Monitoring algorithm (Self-Awareness 2)
     *
     * @param invocationData: Input Data
     */
    @Override
    public void invokeSelfAwarenessRealTimeMonitoringAlgorithm(SewSelfAwarenessRealTimeMonitoringInputDto invocationData) {
        // Retrieve and set component data
        setComponentDataForInvocation(invocationData);

        // Invoke the algorithm
        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, null,"Self-Awareness Real-Time Monitoring");
    }

    /**
     * Retrieve all Self Awareness Real-Time Monitoring Results
     *
     * @return List<SewSelfAwarenessRealTimeMonitoringResultsDto>
     */
    @Override
    public List<SewSelfAwarenessRealTimeMonitoringResultsDto> retrieveAllSelfAwarenessRealTimeMonitoringResults() {
        return exceptionHandler.handleOperation(() -> sewSelfAwarenessRealTimeMonitoringResultsRepository.findAll(Pageable.unpaged())
                .getContent()
                .stream()
                .map(result -> modelMapper.map(result, SewSelfAwarenessRealTimeMonitoringResultsDto.class))
                .toList(), "retrieveAllSelfAwarenessRealTimeMonitoringResults");
    }

    /**
     * Retrieve all Self Awareness Real-Time Monitoring Results by Module ID
     *
     * @param moduleId : ID of Module
     * @return List<SewSelfAwarenessRealTimeMonitoringResultsDto>
     */
    @Override
    public List<SewSelfAwarenessRealTimeMonitoringResultsDto> retrieveAllSelfAwarenessRealTimeMonitoringResultsByModuleId(String moduleId) {
        return exceptionHandler.handleOperation(() -> sewSelfAwarenessRealTimeMonitoringResultsRepository.findByModuleId(moduleId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(result -> modelMapper.map(result, SewSelfAwarenessRealTimeMonitoringResultsDto.class))
                .toList(), "retrieveAllSelfAwarenessRealTimeMonitoringResultsByModuleId");
    }

    /**
     * Upload Module Components List for Self-Awareness Operations
     *
     * @param componentsData : Components Data to upload
     */
    @Override
    public void uploadModuleComponentsList(SewMonitorKpisComponentsDto componentsData) {
        exceptionHandler.handleOperation(() -> {
            // Delete existing components list to replace it if it exists
            if (sewMonitorKpisComponentsRepository.findByModuleId(componentsData.getModuleId()).isPresent()) {
                sewMonitorKpisComponentsRepository.deleteByModuleId(componentsData.getModuleId());
                logger.debug("Deleted existing component list for module: {}", componentsData.getModuleId());
            }

            // Map DTO to entity
            SewMonitorKpisComponents entity = modelMapper.map(componentsData, SewMonitorKpisComponents.class);
            entity.setTimestampCreated(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));

            // Save the new components list
            sewMonitorKpisComponentsRepository.save(entity);
            logger.debug("Successfully uploaded component list for module: {}", componentsData.getModuleId());
            
            return null;
        }, "uploadModuleComponentsList");
    }

    /**
     * Retrieve Self-Awareness Component List by Module ID
     *
     * @param moduleId : ID of Module
     * @return SewMonitorKpisComponentsDto
     */
    @Override
    public SewMonitorKpisComponentsDto retrieveSelfAwarenessComponentListByModuleId(String moduleId) {
        return exceptionHandler.handleOperation(() -> sewMonitorKpisComponentsRepository.findByModuleId(moduleId)
                .map(components -> {
                    return modelMapper.map(components, SewMonitorKpisComponentsDto.class);
                })
                .orElseThrow(() -> new ResourceNotFoundException("No component list found for Module ID: " + moduleId)), "retrieveSelfAwarenessComponentListByModuleId");
    }

    /**
     * Delete Self-Awareness Component List by Module ID
     *
     * @param moduleId : ID of Module
     */
    @Override
    public void deleteSelfAwarenessComponentListByModuleId(String moduleId) {
        exceptionHandler.handleOperation(() -> {
            // Verify component list exists before deletion
            if (sewMonitorKpisComponentsRepository.findByModuleId(moduleId).isEmpty()) {
                throw new ResourceNotFoundException("No component list found for Module ID: " + moduleId);
            }

            sewMonitorKpisComponentsRepository.deleteByModuleId(moduleId);
            logger.debug("Successfully deleted component list for module: {}", moduleId);

            return null;
        }, "deleteSelfAwarenessComponentListByModuleId");
    }

    @Override
    public SewFilteringOptionsDto retrieveFilteringOptionsForLocalAnalytics(GlobalRequestDto request) {
        return exceptionHandler.handleOperation(() -> {
            List<SewSelfAwarenessMonitoringKpisResultsDto> inputData = sewSelfAwarenessMonitoringKpisResultsRepository.findAll(Pageable.unpaged())
                    .stream()
                    .map(output -> modelMapper.map(output, SewSelfAwarenessMonitoringKpisResultsDto.class))
                    .toList();

            ResponseEntity<DtResponseDto> response = formulateAndImplementSyncSmartServiceRequest(inputData, request.getModuleId(), request.getSmartServiceId());

            logger.debug("Successfully invoked Local-Analytics to provide the filtering options..Processing results..");

            // Use processor for the important response type
            SewFilteringOptionsDto results = null;
            if (validateDigitalTwinResponse(response))
                results = decodeDigitalTwinResponseToDto(SewFilteringOptionsDto.class, response.getBody());

            // Locate the Distinct values
            results.setDistinctValues(generateDistinctValuesFromFilteringOptions(results.getFilteringOptions()));

            return results;
        }, "retrieveFilteringOptionsForLocalAnalytics");
    }

    /*
     * Helper method to locate the distinct values for each category
     */
    private SewFilteringOptionsDto.DistinctValues generateDistinctValuesFromFilteringOptions(List<SewFilteringOptionsDto.Options> filteringOptions) {

        // Handle null or empty filtering options
        if (filteringOptions == null || filteringOptions.isEmpty()) {
            return new SewFilteringOptionsDto.DistinctValues(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );
        }

        // Extract distinct values for each category using streams
        List<String> distinctCells = filteringOptions.stream()
                .map(SewFilteringOptionsDto.Options::getCell)
                .filter(cell -> cell != null && !cell.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        List<String> distinctModules = filteringOptions.stream()
                .map(SewFilteringOptionsDto.Options::getModule)
                .filter(module -> module != null && !module.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        List<String> distinctSubElements = filteringOptions.stream()
                .map(SewFilteringOptionsDto.Options::getSubElement)
                .filter(subElement -> subElement != null && !subElement.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        List<String> distinctComponents = filteringOptions.stream()
                .map(SewFilteringOptionsDto.Options::getComponent)
                .filter(component -> component != null && !component.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        List<String> distinctVariables = filteringOptions.stream()
                .map(SewFilteringOptionsDto.Options::getVariable)
                .filter(variable -> variable != null && !variable.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        // Construct and return the DistinctValues object
        return new SewFilteringOptionsDto.DistinctValues(
                distinctCells,
                distinctModules,
                distinctSubElements,
                distinctComponents,
                distinctVariables
        );
    }

    @Override
    public String generateHistogramForComparingModules(GlobalRequestDto<SewLocalAnalyticsInputDto> request) {
        return exceptionHandler.handleOperation(() -> {
            List<SewSelfAwarenessMonitoringKpisResultsDto> inputData = sewSelfAwarenessMonitoringKpisResultsRepository.findAll(Pageable.unpaged())
                    .stream()
                    .map(output -> modelMapper.map(output, SewSelfAwarenessMonitoringKpisResultsDto.class))
                    .toList();

            SewLocalAnalyticsInputDto serviceInput = SewLocalAnalyticsInputDto.builder()
                    .firstParameters(request.getInput().getFirstParameters())
                    .secondParameters(request.getInput().getSecondParameters())
                    .histogramData(inputData)
                    .build();

            ResponseEntity<DtResponseDto> response = formulateAndImplementSyncSmartServiceRequest(serviceInput, request.getModuleId(), request.getSmartServiceId());

            logger.debug("Successfully invoked Local-Analytics to produce the Histogram..Processing results..");

            // Use processor for the important response type
            String encodedImage = null;
            if (validateDigitalTwinResponse(response))
                encodedImage = decodeDigitalTwinResponseToDto(String.class, response.getBody());

            return encodedImage;
        }, "generateHistogramForComparingModules");
    }

    /**
     * Helper method to set component data for algorithm invocation
     *
     * @param invocationData: Input data object that has moduleId and components setter
     */
    private void setComponentDataForInvocation(Object invocationData) {
        try {
            // Use reflection to get moduleId and set components
            String moduleId = (String) invocationData.getClass().getMethod("getModuleId").invoke(invocationData);

            // Retrieve component list for given Module
            SewMonitorKpisComponents componentData = sewMonitorKpisComponentsRepository.findByModuleId(moduleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Component list information for module '" + moduleId + "' not found"));

            // Transform components to DTO format
            List<SewMonitorKpisComponentsDto.SewMonitorKpisComponentsDataDto> componentsDto = componentData.getComponents().stream()
                    .map(component -> modelMapper.map(component, SewMonitorKpisComponentsDto.SewMonitorKpisComponentsDataDto.class))
                    .toList();

            // Set components using reflection
            invocationData.getClass().getMethod("setComponents", List.class).invoke(invocationData, componentsDto);
        } catch (Exception e) {
            logger.error("Error setting component data for invocation: {}", e.getMessage());
            throw new SmartServiceInvocationException("Error setting component data for algorithm invocation");
        }
    }

    /*
     * Helper method to implement SYNC request to Smart Services via DT
     */
    private <T> ResponseEntity<DtResponseDto> formulateAndImplementSyncSmartServiceRequest(T inputData, String moduleId, String smartServiceId) {
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
        return smartServicesInvocationService.invokeSmartService(
                smartServiceId,
                moduleId,
                dtInput,
                ModaptoHeader.SYNC
        );
    }

    /*
     * Helper method to Decode Digital Twin response to specific class
     */
    private <T> T decodeDigitalTwinResponseToDto(Class<T> clazz, DtResponseDto response){
        try {
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
            logger.error("Error processing Digital Twin response for Local Analytics - Error: {}", e.getMessage());
            throw new SmartServiceInvocationException("Failed to process Digital Twin response for Local Analytics: " + e.getMessage());
        }
    }

    /*
     * Helper method to validate whether the DT Response is valid
     */
    private boolean validateDigitalTwinResponse(ResponseEntity<DtResponseDto> response){
        // Validate response
        if (response == null || response.getBody() == null) {
            throw new DtmServerErrorException("No response received from DTM service for requested operation");
        }

        DtResponseDto dtmResponse = response.getBody();

        if (dtmResponse == null) {
            logger.error("DTM service response body is null for threshold maintenance.");
            throw new DtmServerErrorException("DTM service returned a null body for requested operation");
        }

        if (!dtmResponse.isSuccess()) {
            logger.error("DTM service execution failed for requested operation. Messages: {}",
                    Optional.ofNullable(dtmResponse.getMessages()).orElse(List.of("No error messages provided")));
            throw new DtmServerErrorException("DTM service execution failed for threshold maintenance");
        }

        return true;
    }


}

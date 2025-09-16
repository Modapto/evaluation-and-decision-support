package gr.atc.modapto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.dt.SmartServiceRequest;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessRealTimeMonitoringInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessRealTimeMonitoringResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.model.sew.SewMonitorKpisComponents;
import gr.atc.modapto.repository.SewMonitorKpisComponentsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessRealTimeMonitoringResultsRepository;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.repository.SewSelfAwarenessMonitoringKpisResultsRepository;
import gr.atc.modapto.service.interfaces.ISelfAwarenessService;
import gr.atc.modapto.service.processors.NoOpResponseProcessor;
import gr.atc.modapto.exception.CustomExceptions.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class SelfAwarenessService implements ISelfAwarenessService {

    private static final Logger logger = LoggerFactory.getLogger(SelfAwarenessService.class);

    private final SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository;

    private final SewSelfAwarenessRealTimeMonitoringResultsRepository sewSelfAwarenessRealTimeMonitoringResultsRepository;

    private final SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandler;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final NoOpResponseProcessor noOpResponseProcessor;

    public SelfAwarenessService(SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository,
                                SewSelfAwarenessRealTimeMonitoringResultsRepository sewSelfAwarenessRealTimeMonitoringResultsRepository,
                                SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository,
                                SmartServicesInvocationService smartServicesInvocationService,
                                ExceptionHandlerService exceptionHandler,
                                ModelMapper modelMapper,
                                ObjectMapper objectMapper,
                                NoOpResponseProcessor noOpResponseProcessor){
        this.sewSelfAwarenessMonitoringKpisResultsRepository = sewSelfAwarenessMonitoringKpisResultsRepository;
        this.sewSelfAwarenessRealTimeMonitoringResultsRepository = sewSelfAwarenessRealTimeMonitoringResultsRepository;
        this.sewMonitorKpisComponentsRepository = sewMonitorKpisComponentsRepository;
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.exceptionHandler = exceptionHandler;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
        this.noOpResponseProcessor = noOpResponseProcessor;
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
        invokeAlgorithm(invocationData, "Self-Awareness Monitoring KPIs");
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
        invokeAlgorithm(invocationData, "Self-Awareness Real-Time Monitoring");
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

    /**
     * Common algorithm invocation logic
     *
     * @param invocationData: Input data object
     * @param algorithmType: Type of algorithm for logging purposes
     */
    private void invokeAlgorithm(Object invocationData, String algorithmType) {
        SmartServiceRequest request;
        try {
            String encodedInput = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(invocationData).getBytes());
            request = SmartServiceRequest.builder()
                    .request(encodedInput)
                    .build();
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
            ResponseEntity<DtResponseDto> response = smartServicesInvocationService.invokeSmartService(
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
}

package gr.atc.modapto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.dt.SmartServiceRequest;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
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

import java.util.Base64;
import java.util.List;

@Service
public class SelfAwarenessService implements ISelfAwarenessService {

    private static final Logger logger = LoggerFactory.getLogger(SelfAwarenessService.class);

    private final SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandler;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final NoOpResponseProcessor noOpResponseProcessor;

    public SelfAwarenessService(SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository,
                                SmartServicesInvocationService smartServicesInvocationService,
                                ExceptionHandlerService exceptionHandler,
                                ModelMapper modelMapper,
                                ObjectMapper objectMapper,
                                NoOpResponseProcessor noOpResponseProcessor){
        this.sewSelfAwarenessMonitoringKpisResultsRepository = sewSelfAwarenessMonitoringKpisResultsRepository;
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
        // Transform input data into Base64 encoding and properly format the SmartServiceRequest Input
        SmartServiceRequest request;
        try {
            String encodedInput = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(invocationData).getBytes());
            request = SmartServiceRequest.builder()
                    .request(encodedInput)
                    .build();
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert Self-Awareness Monitoring KPIs input to Base64 Encoding");
            throw new SmartServiceInvocationException("Unable to convert Self-Awareness Monitoring KPIs input to Base64 Encoding");
        }

        // Wrap Smart Service Input data to DtInputDto
        DtInputDto<SmartServiceRequest> dtInput = DtInputDto.<SmartServiceRequest>builder()
                .inputArguments(request)
                .build();

        // Invoke smart service
        ResponseEntity<DtResponseDto> response = smartServicesInvocationService.invokeSmartService(
                invocationData.getSmartServiceId(),
                invocationData.getModuleId(),
                dtInput,
                ModaptoHeader.ASYNC
        );

        logger.debug("Successfully invoked Self-Awareness Monitoring KPIs algorithm");

        // Just discard the response as it will be handled via the MB
        noOpResponseProcessor.processResponse(response, invocationData.getModuleId(), invocationData.getSmartServiceId());
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
}

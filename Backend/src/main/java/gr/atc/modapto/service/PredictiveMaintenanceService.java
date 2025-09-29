package gr.atc.modapto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.atc.modapto.dto.ScheduledTaskDto;
import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.dt.SmartServiceRequest;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.dto.serviceResults.sew.SewGroupingPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewThresholdBasedPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import gr.atc.modapto.dto.sew.SewComponentInfoDto;
import gr.atc.modapto.dto.serviceInvocations.SewGroupingPredictiveMaintenanceInputDataDto;
import gr.atc.modapto.enums.KafkaTopics;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.events.ScheduledTaskRegistrationEvent;
import gr.atc.modapto.model.MaintenanceData;
import gr.atc.modapto.model.sew.SewComponentInfo;
import gr.atc.modapto.model.serviceResults.SewGroupingPredictiveMaintenanceResult;
import gr.atc.modapto.model.serviceResults.SewThresholdBasedPredictiveMaintenanceResult;
import gr.atc.modapto.repository.MaintenanceDataRepository;
import gr.atc.modapto.repository.SewComponentInfoRepository;
import gr.atc.modapto.repository.SewGroupingBasedPredictiveMaintenanceRepository;
import gr.atc.modapto.repository.SewThresholdBasedPredictiveMaintenanceRepository;
import gr.atc.modapto.service.interfaces.IPredictiveMaintenanceService;
import gr.atc.modapto.service.processors.ThresholdBasedMaintenanceResponseProcessor;
import gr.atc.modapto.util.ExcelFilesUtils;

import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.enums.MessagePriority;
import gr.atc.modapto.exception.CustomExceptions.*;
import gr.atc.modapto.kafka.KafkaMessageProducer;

@Service
public class PredictiveMaintenanceService implements IPredictiveMaintenanceService {

    private final Logger logger = LoggerFactory.getLogger(PredictiveMaintenanceService.class);

    private final KafkaMessageProducer kafkaMessageProducer;

    private final MaintenanceDataRepository maintenanceDataRepository;

    private final SewComponentInfoRepository componentInfoRepository;

    private final SewGroupingBasedPredictiveMaintenanceRepository sewGroupingBasedPredictiveMaintenanceRepository;

    private final SewThresholdBasedPredictiveMaintenanceRepository sewThresholdBasedPredictiveMaintenanceRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ThresholdBasedMaintenanceResponseProcessor thresholdMaintenanceResponseProcessor;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher eventPublisher;

    private static final String COMPONENT_MAPPING_ERROR = "Unable to parse DTO SewComponentInfo to Model or vice-versa - Error: ";

    private static final String MAPPING_ERROR = "Unable to parse SEW Predictive Maintenance Results to DTO or vice-versa - Error: ";

    private static final String THRESHOLD_BASED_TYPE = "THRESHOLD_BASED_PREDICTIVE_MAINTENANCE";

    private static final int BATCH_SIZE = 1000; // Batch Size

    public PredictiveMaintenanceService(MaintenanceDataRepository maintenanceDataRepository,
                                        ModelMapper modelMapper,
                                        ElasticsearchOperations elasticsearchOperations,
                                        SewComponentInfoRepository componentInfoRepository,
                                        SmartServicesInvocationService smartServicesInvocationService,
                                        SewGroupingBasedPredictiveMaintenanceRepository sewGroupingBasedPredictiveMaintenanceRepository,
                                        SewThresholdBasedPredictiveMaintenanceRepository sewThresholdBasedPredictiveMaintenanceRepository,
                                        ThresholdBasedMaintenanceResponseProcessor thresholdBasedMaintenanceResponseProcessor,
                                        ObjectMapper objectMapper,
                                        ApplicationEventPublisher eventPublisher,
                                        KafkaMessageProducer kafkaMessageProducer) {
        this.maintenanceDataRepository = maintenanceDataRepository;
        this.modelMapper = modelMapper;
        this.elasticsearchOperations = elasticsearchOperations;
        this.componentInfoRepository = componentInfoRepository;
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.sewGroupingBasedPredictiveMaintenanceRepository = sewGroupingBasedPredictiveMaintenanceRepository;
        this.sewThresholdBasedPredictiveMaintenanceRepository = sewThresholdBasedPredictiveMaintenanceRepository;
        this.thresholdMaintenanceResponseProcessor = thresholdBasedMaintenanceResponseProcessor;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @Override
    public void storeCorimData(MultipartFile file) {
        try {
            // Extract all data at once using parallel processing
            List<MaintenanceDataDto> allData = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            logger.debug("-----------------------------------------------");
            logger.debug("Extracted {} records from CORIM file", allData.size());
            logger.debug("-----------------------------------------------");

            // Store in database batches
            storeBatch(allData);

        } catch (Exception e) {
            logger.error("Error processing Excel file", e);
            throw new FileHandlingException("Error processing Excel file");
        }
    }

    /**
     * Store component information data in Elasticsearch
     *
     * @param componentInfoList : List of SewComponentInfoDto
     * @throws ModelMappingException Thrown if unable to parse DTO SewComponentInfo to Model
     */
    @Override
    public void storeComponentsListData(List<SewComponentInfoDto> componentInfoList) {
        try {
            // Delete old data
            componentInfoRepository.deleteAll();

            // Save new data
            List<SewComponentInfo> componentData = componentInfoList.stream().map(componentInfoDto -> modelMapper.map(componentInfoDto, SewComponentInfo.class)).toList();
            componentInfoRepository.saveAll(componentData);
        } catch (MappingException e) {
            throw new ModelMappingException("Unable to parse DTO SewComponentInfo to Model - Error: " + e.getMessage());
        }
    }


    /**
     * Store a chunk of data in database batches
     */
    private void storeBatch(List<MaintenanceDataDto> chunkData) {
        try {
            for (int i = 0; i < chunkData.size(); i += BATCH_SIZE) {
                List<MaintenanceDataDto> dtoBatch = chunkData.subList(i, Math.min(i + BATCH_SIZE, chunkData.size()));
                List<MaintenanceData> batch = dtoBatch.stream().map(maintenanceDataDto -> modelMapper.map(maintenanceDataDto, MaintenanceData.class)).toList();
                maintenanceDataRepository.saveAll(batch);
            }
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }

    /**
     * Retrieve all completed maintenance data for SEW plant (process drifts completed)
     *
     * @param pageable: Pagination attributes
     * @return Page<MaintenanceData>
     */
    @Override
    public Page<MaintenanceDataDto> retrieveMaintenanceDataPaginated(Pageable pageable) {
        // Set the criteria to find sorted all data that include the "tsInterventionFinished" field (not null)
        // This retrieves only completed process drifts as intended
        CriteriaQuery query = new CriteriaQuery(new Criteria("tsInterventionFinished").exists(), pageable);

        SearchHits<MaintenanceData> searchHits = elasticsearchOperations.search(query, MaintenanceData.class);

        try {
            List<MaintenanceDataDto> dtoList = searchHits.getSearchHits()
                    .stream()
                    .map(SearchHit::getContent)
                    .map(data -> modelMapper.map(data, MaintenanceDataDto.class))
                    .toList();

            // Create PageImpl with the results
            return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
        } catch (MappingException e) {
            throw new ModelMappingException("Unable to parse Maintenance Data to DTO - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve Component List given Stage, Cell, Module and Module ID
     *
     * @param stage    : Stage of the module
     * @param cell     : Cell of the module
     * @param module   : Module description
     * @param moduleId : Module ID
     * @return List<SewComponentInfoDto>
     */
    @Override
    public List<SewComponentInfoDto> retrieveComponentListGivenFilterAttributes(String stage, String cell, String module, String moduleId) {
        try {
            return componentInfoRepository.findByStageAndCellAndModuleAndModuleId(stage, cell, module, moduleId)
                    .stream()
                    .map(component -> modelMapper.map(component, SewComponentInfoDto.class))
                    .toList();
        } catch (MappingException e) {
            logger.error(COMPONENT_MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(COMPONENT_MAPPING_ERROR + e.getMessage());
        }
    }

    /**
     * Locate the latest maintenance action (by started time) for a specific component and update the equivalent value in the Components List
     */
    @Override
    @Async("taskExecutor")
    public void locateLastMaintenanceActionForStoredComponents() {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "tsInterventionStarted"));

        // Retrieve all Components
        List<SewComponentInfo> componentInfoList = componentInfoRepository.findAll(Pageable.unpaged()).getContent();
        componentInfoList.forEach(component -> {
            // Set the search criteria
            Criteria criteria = new Criteria("stage").is(component.getStage())
                    .and(new Criteria("cell").is(component.getCell()))
                    .and(new Criteria("module").is(component.getModule()))
                    .and(new Criteria("moduleId").is(component.getModuleId()));

            // Execute query
            CriteriaQuery query = new CriteriaQuery(criteria, pageable);
            SearchHits<MaintenanceData> searchHits = elasticsearchOperations.search(query, MaintenanceData.class);

            // Check if nothing found
            if (searchHits.isEmpty())
                component.setLastMaintenanceActionTime("");
            else
                component.setLastMaintenanceActionTime(searchHits.getSearchHits().getFirst().getContent().getTsInterventionStarted().format(DateTimeFormatter.ISO_DATE_TIME));
        });
        // Update instances
        componentInfoRepository.saveAll(componentInfoList);
    }

    /**
     * Invoke Grouping Predictive Maintenance Service, by locating the components list and constructing the input for the Service
     * Results will be handled via MB event
     *
     * @param invocationData : Input data to the algorithm
     */
    @Override
    public void invokeGroupingPredictiveMaintenance(SewGroupingPredictiveMaintenanceInputDataDto invocationData) {
        try {
            // Retrieve Component List
            List<SewComponentInfoDto> componentInfoDto = componentInfoRepository.findAll(Pageable.unpaged())
                    .getContent()
                    .stream()
                    .map(component -> modelMapper.map(component, SewComponentInfoDto.class))
                    .toList();

            invocationData.setComponentList(componentInfoDto);


        } catch (MappingException e) {
            logger.error("Exception occurred while mapping Grouping Predictive Maintenance Entity to DTO: {}", e.getMessage());
            throw new ModelMappingException("Exception occurred while mapping Grouping Predictive Maintenance Entity to DTO: " + e.getMessage());
        }

        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, null, "Grouping Predictive Maintenance");
    }

    /**
     * Invoker Threshold Based Predictive Maintenance and Register the Scheduled Task with the input data
     *
     * @param invocationData : Input Data
     * @return SewThresholdBasedPredictiveMaintenanceOutputDto
     */
    @Override
    public SewThresholdBasedPredictiveMaintenanceOutputDto invokeAndRegisterThresholdBasedPredictiveMaintenance(SewThresholdBasedMaintenanceInputDataDto invocationData) {

        SewThresholdBasedPredictiveMaintenanceOutputDto responseData = invokeThresholdBasedPredictiveMaintenance(invocationData);

        // If no exception occurs in the above process, request was successful and thus we can register the Task (via Application Events)
        // Reset maintenance data
        invocationData.setEvents(null);

        // Create the scheduled task
        ScheduledTaskDto newTask = ScheduledTaskDto.builder()
                .frequencyValue(invocationData.getFrequencyValue())
                .frequencyType(invocationData.getFrequencyType())
                .smartServiceId(invocationData.getSmartServiceId())
                .moduleId(invocationData.getModuleId())
                .smartServiceType(THRESHOLD_BASED_TYPE)
                .requestBody(invocationData)
                .build();

        // Publish event
        ScheduledTaskRegistrationEvent event = new ScheduledTaskRegistrationEvent(this, newTask, THRESHOLD_BASED_TYPE);
        eventPublisher.publishEvent(event);
        logger.debug("Event published to register a new Scheduled Task - Event: {}", event);

        return responseData;
    }

    @Override
    public SewThresholdBasedPredictiveMaintenanceOutputDto invokeThresholdBasedPredictiveMaintenance(SewThresholdBasedMaintenanceInputDataDto invocationData) {
        SmartServiceRequest request;
        try {
            // Retrieve all CORIM Data
            List<MaintenanceDataDto> maintenanceData = maintenanceDataRepository.findAll(Pageable.unpaged())
                    .getContent()
                    .stream()
                    .map(data -> {
                        MaintenanceDataDto dto = modelMapper.map(data, MaintenanceDataDto.class);
                        // Remove/Hide unnecessary fields
                        refactorMaintenanceDto(dto);

                        return dto;
                    })
                    .toList();

            invocationData.setEvents(maintenanceData);

            // Encode the invocationData to Base64
            String encodedInput = Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(invocationData).getBytes());
            request = SmartServiceRequest.builder()
                    .request(encodedInput)
                    .build();

        } catch (MappingException e) {
            logger.error("Exception occurred while mapping Threshold Based Predictive Maintenance Entity to DTO: {}", e.getMessage());
            throw new ModelMappingException("Exception occurred while mapping Threshold Based Predictive Maintenance Entity to DTO: " + e.getMessage());
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert Threshold-Based Predictive Maintenance input to Base64 Encoding");
            throw new SmartServiceInvocationException("Unable to convert Threshold-Based Predictive Maintenance input to Base64 Encoding");
        }

        // Wrap invocation data in DtInputDto
        DtInputDto<SmartServiceRequest> dtInput = DtInputDto.<SmartServiceRequest>builder()
                .inputArguments(request)
                .build();

        // Invoke smart service using the generic service
        ResponseEntity<DtResponseDto> response = smartServicesInvocationService.invokeSmartService(
                invocationData.getSmartServiceId(),
                invocationData.getModuleId(),
                dtInput,
                ModaptoHeader.SYNC
        );

        logger.debug("Successfully invoked threshold-based predictive maintenance service..Processing results..");

        // Use processor for the important response type
        SewThresholdBasedPredictiveMaintenanceOutputDto results = thresholdMaintenanceResponseProcessor.processResponse(
                response,
                invocationData.getModuleId(),
                invocationData.getSmartServiceId()
        );

        // Send Event via MB
        EventDto event = EventDto.builder()
                .module(invocationData.getModuleId())
                .smartService(invocationData.getSmartServiceId())
                .priority(MessagePriority.MID)
                .description("Threshold Based maintenance completed for Module: " + invocationData.getModuleId())
                .eventType("Threshold Based Maintenance action completed")
                .sourceComponent("Predictive Maintenance")
                .results(objectMapper.valueToTree(results))
                .topic(KafkaTopics.SEW_THRESHOLD_PREDICTIVE_MAINTENANCE.toString())
                .timestamp(LocalDateTime.now().withNano(0))
                .build();

        kafkaMessageProducer.sendMessage(event.getTopic(), event);

        return results;
    }

    /*
     * Remove unnecessary fields
     */
    private void refactorMaintenanceDto(MaintenanceDataDto dto) {
        dto.setFaultyElementId(null);
        dto.setTsRequestCreation(null);
        dto.setTsInterventionFinished(null);
        dto.setModule(null);
        dto.setComponent(null);
        dto.setModaptoModule(null);
    }

    /**
     * Retrieve latest threshold-based predictive maintenance results for a specific Module
     *
     * @param moduleId : Module ID
     * @return SewThresholdBasedPredictiveMaintenanceOutputDto
     * @throws ResourceNotFoundException Thrown if no results are found
     * @throws ModelMappingException     Thrown if unable to parse Model to DTO
     */
    @Override
    public SewThresholdBasedPredictiveMaintenanceOutputDto retrieveLatestThresholdBasedMaintenanceResults(String moduleId) {
        try {
            Optional<SewThresholdBasedPredictiveMaintenanceResult> latestResult = sewThresholdBasedPredictiveMaintenanceRepository.findFirstByModuleId(moduleId);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No SEW Grouping Based Predictive Maintenance Results for Module: " + moduleId + " found");

            return modelMapper.map(latestResult.get(), SewThresholdBasedPredictiveMaintenanceOutputDto.class);
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "for Module {} - {}", moduleId, e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Threshold Based Maintenance Results Results to DTO for Module: " + moduleId + " - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve latest grouping predictive maintenance results for a specific Module
     *
     * @param moduleId : Module ID
     * @return SewGroupingPredictiveMaintenanceOutputDto
     * @throws ResourceNotFoundException Thrown if no results are found
     * @throws ModelMappingException     Thrown if unable to parse Model to DTO
     */
    @Override
    public SewGroupingPredictiveMaintenanceOutputDto retrieveLatestGroupingMaintenanceResults(String moduleId) {
        try {
            Optional<SewGroupingPredictiveMaintenanceResult> latestResult = sewGroupingBasedPredictiveMaintenanceRepository.findFirstByModuleId(moduleId);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No SEW Grouping Based Predictive Maintenance Results for Module: " + moduleId + " found");

            return modelMapper.map(latestResult.get(), SewGroupingPredictiveMaintenanceOutputDto.class);
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "for Module {} - {}", moduleId, e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Grouping Maintenance Results to DTO for Module: " + moduleId + " - Error: " + e.getMessage());
        }
    }


    /**
     * Declare a new Process Drift
     *
     * @param processDriftData : Input data for the new Process Drift
     * @throws ModelMappingException     ModelMapper Exception
     * @throws ResourceNotFoundException Resource not found in DB
     */
    @Override
    public String declareProcessDrift(MaintenanceDataDto processDriftData) {
        // Check if start/create timestamps are set
        LocalDateTime now = LocalDateTime.now().withNano(0);

        if (processDriftData.getTsRequestCreation() == null) {
            processDriftData.setTsRequestCreation(now);
        }

        if (processDriftData.getTsInterventionStarted() == null) {
            processDriftData.setTsInterventionStarted(now);
        }

        try {
            MaintenanceData entity = modelMapper.map(processDriftData, MaintenanceData.class);
            MaintenanceData storedDrift = maintenanceDataRepository.save(entity);

            // Send Event via MB
            EventDto event = EventDto.builder()
                    .module(processDriftData.getModaptoModule())
                    .smartService(null)
                    .priority(MessagePriority.HIGH)
                    .description("A process drift was declared for Stage: " + processDriftData.getStage() +
                            ", Cell: " + processDriftData.getCell() +
                            ", Module: " + processDriftData.getModule() +
                            ", Component: " + processDriftData.getComponent() +
                            ", by worker: " + processDriftData.getWorkerName())
                    .eventType("Process Drift Declared")
                    .sourceComponent("Evaluation and Decision Support")
                    .results(null)
                    .topic(KafkaTopics.SEW_PROCESS_DRIFT.toString())
                    .timestamp(LocalDateTime.now().withNano(0))
                    .build();

            kafkaMessageProducer.sendMessage(event.getTopic(), event);

            return storedDrift.getId();
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }

    /**
     * Retrieve a Process Drift / Maintenance Data by ID
     *
     * @param processDriftId : ID
     * @return MaintenanceDataDto
     */
    @Override
    public MaintenanceDataDto retrieveProcessDriftById(String processDriftId) {
        try {
            MaintenanceData drift = maintenanceDataRepository.findById(processDriftId)
                    .orElseThrow(() -> new ResourceNotFoundException("Process drift with ID = '" + processDriftId + "' not found in PKB"));

            return modelMapper.map(drift, MaintenanceDataDto.class);
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }

    /**
     * Retrieve all uncompleted Process Drifts (by the TsInterventionFinished timestamp - null field)
     *
     * @param pageable : Pagination parameters
     * @return Page<MaintenanceDataDto>
     */
    @Override
    public Page<MaintenanceDataDto> retrievePaginatedUncompletedProcessDrifts(Pageable pageable) {
        // Set the search criteria
        Criteria criteria = new Criteria("tsInterventionFinished").exists().not();

        CriteriaQuery query = new CriteriaQuery(criteria, pageable);
        SearchHits<MaintenanceData> searchHits = elasticsearchOperations.search(query, MaintenanceData.class);

        // Convert SearchHits to Page<MaintenanceDataDto>
        List<MaintenanceDataDto> dtoList = searchHits.getSearchHits().stream()
                .map(hit -> modelMapper.map(hit.getContent(), MaintenanceDataDto.class))
                .toList();

        // Create PageImpl with the results
        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    /**
     * Declare a Process Drift as Completed
     *
     * @param processDriftId : ID of Maintenance Data Drift
     * @param endDatetime    : Datetime of completion
     * @throws ResourceNotFoundException : In case the requested resource not found
     */
    @Override
    public void completeProcessDrift(String processDriftId, LocalDateTime endDatetime) {
        MaintenanceData drift = maintenanceDataRepository.findById(processDriftId)
                .orElseThrow(() -> new ResourceNotFoundException("Process drift with ID = '" + processDriftId + "' not found in PKB"));

        drift.setTsInterventionFinished(endDatetime);
        maintenanceDataRepository.save(drift);

        // Send Event via MB
        EventDto event = EventDto.builder()
                .module(drift.getModaptoModule())
                .smartService(null)
                .priority(MessagePriority.HIGH)
                .description("A process drift was completed for Stage: " + drift.getStage() +
                        ", Cell: " + drift.getCell() +
                        ", Module: " + drift.getModule() +
                        ", Component: " + drift.getComponent() +
                        ", by worker: " + drift.getWorkerName())
                .eventType("Process Drift Completed")
                .sourceComponent("Evaluation and Decision Support")
                .results(null)
                .topic(KafkaTopics.SEW_PROCESS_DRIFT.toString())
                .timestamp(LocalDateTime.now().withNano(0))
                .build();

        kafkaMessageProducer.sendMessage(event.getTopic(), event);
    }
}

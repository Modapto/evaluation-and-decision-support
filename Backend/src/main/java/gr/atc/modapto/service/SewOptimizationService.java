package gr.atc.modapto.service;

import java.util.Map;
import java.util.Optional;

import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.atc.modapto.dto.serviceInvocations.SewOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import static gr.atc.modapto.enums.OptEngineRoute.PRODUCTION_SCHEDULE_OPTIMIZATION;
import gr.atc.modapto.exception.CustomExceptions.ModelMappingException;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.exception.CustomExceptions.SmartServiceInvocationException;
import gr.atc.modapto.model.serviceResults.SewOptimizationResults;
import gr.atc.modapto.model.sew.ProductionSchedule;
import gr.atc.modapto.repository.ProductionScheduleRepository;
import gr.atc.modapto.repository.SewOptimizationResultsRepository;
import gr.atc.modapto.service.interfaces.IProductionScheduleOptimizationService;

@Service
public class SewOptimizationService implements IProductionScheduleOptimizationService {

    private final Logger log = LoggerFactory.getLogger(SewOptimizationService.class);

    private final static String MAPPING_ERROR = "Unable to parse SEW Optimization Results to DTO - Error: ";

    private final static String PROD_SCHEDULE_ID = "latest-production-schedule";

    private final SewOptimizationResultsRepository sewOptimizationResultsRepository;

    private final ProductionScheduleRepository productionScheduleRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandler;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    public SewOptimizationService(SewOptimizationResultsRepository sewOptimizationResultsRepository, ExceptionHandlerService exceptionHandler, ProductionScheduleRepository productionScheduleRepository, ObjectMapper objectMapper, ModelMapper modelMapper, SmartServicesInvocationService smartServicesInvocationService) {
        this.sewOptimizationResultsRepository = sewOptimizationResultsRepository;
        this.productionScheduleRepository = productionScheduleRepository;
        this.modelMapper = modelMapper;
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.exceptionHandler = exceptionHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieve latest results regarding SEW Optimization Smart Service
     *
     * @return SewOptimizationResultsDto
     */
    @Override
    public SewOptimizationResultsDto retrieveLatestOptimizationResults() {
        try {
            Optional<SewOptimizationResults> latestResult = sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc();
            if (latestResult.isEmpty()) {
                throw new ResourceNotFoundException("No SEW Optimization Results found");
            }

            return modelMapper.map(latestResult.get(), SewOptimizationResultsDto.class);
        } catch (MappingException e) {
            log.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Optimization Results to DTO - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve latest results regarding SEW Optimization Smart Service for a
     * specific MODAPTO module
     *
     * @param moduleId MODAPTO module
     * @throws ResourceNotFoundException Thrown when the requested resource not
     * found in Elasticsearch
     * @throws ModelMappingException Thrown when a mismatch exists between DTO
     * and Entity data
     * @return SewOptimizationResultsDto
     */
    @Override
    public SewOptimizationResultsDto retrieveLatestOptimizationResultsByModuleId(String moduleId) {
        try {
            Optional<SewOptimizationResults> latestResult = sewOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(moduleId);
            if (latestResult.isEmpty()) {
                throw new ResourceNotFoundException("No SEW Optimization Results for Module: " + moduleId + " found");
            }

            return modelMapper.map(latestResult.get(), SewOptimizationResultsDto.class);
        } catch (MappingException e) {
            log.error(MAPPING_ERROR + "for Module {} - {}", moduleId, e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Optimization Results to DTO for Module: " + moduleId + " - Error: " + e.getMessage());
        }
    }

    /**
     * Upload Production Schedules for SEW replacing any existing schedule
     *
     * @param scheduleDto : SEW Production Schedule
     */
    @Override
    public void uploadProductionSchedule(SewProductionScheduleDto scheduleDto) {
        exceptionHandler.handleOperation(() -> {
            ProductionSchedule schedule = modelMapper.map(scheduleDto, ProductionSchedule.class);
            schedule.setId(PROD_SCHEDULE_ID); // Singleton Prod. Schedule
            productionScheduleRepository.save(schedule);
            return null;
        }, "uploadProductionSchedule");
    }

    /**
     * Retrieve the latest stored Prod. Schedule
     *
     * @return SewProductionScheduleDto
     */
    @Override
    public SewProductionScheduleDto retrieveLatestProductionSchedule() {
        return productionScheduleRepository.findById(PROD_SCHEDULE_ID)
                .map(schedule -> modelMapper.map(schedule, SewProductionScheduleDto.class))
                .orElseThrow(() -> new ResourceNotFoundException("There is no stored production schedule in the DB"));
    }

    /**
     * Invoke method of Optimization of Production Schedules
     *
     * @param invocationData Invocation Data for Optimization of Prod. Schedules
     */
    @Override
    public void invokeOptimizationOfProductionSchedules(SewOptimizationInputDto invocationData) {
        // Check if Prod. Schedules data is provided
        if (invocationData.getInput() == null || invocationData.getInput().isEmpty()) {
            Map<String, SewProductionScheduleDto.DailyDataDto> data = retrieveLatestProductionSchedule().getData();
            if (data == null || data.isEmpty()) {
                throw new SmartServiceInvocationException("No Prod. Schedule data provided and no stored Prod. Schedule found in the DB");
            }
            invocationData.setInput(data);
            log.debug("No Prod. Schedule data provided. Using the latest stored Prod. Schedule");
        }

        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, PRODUCTION_SCHEDULE_OPTIMIZATION.toString(), "SEW Optimization of Production Schedules");
    }
}

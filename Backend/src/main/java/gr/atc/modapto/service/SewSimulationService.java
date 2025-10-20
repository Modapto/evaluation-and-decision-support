package gr.atc.modapto.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import gr.atc.modapto.dto.serviceInvocations.SewSimulationInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.dto.sew.SewPlantEnvironmentDto;
import gr.atc.modapto.model.serviceResults.SewSimulationResults;
import gr.atc.modapto.model.sew.SewPlantEnvironment;
import gr.atc.modapto.repository.SewPlantEnvironmentRepository;
import gr.atc.modapto.repository.SewSimulationResultsRepository;
import gr.atc.modapto.service.interfaces.IProductionScheduleSimulationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static gr.atc.modapto.enums.OptEngineRoute.PRODUCTION_SCHEDULE_SIMULATION;

@Service
public class SewSimulationService implements IProductionScheduleSimulationService {

    private final Logger log = LoggerFactory.getLogger(SewSimulationService.class);

    private final String MAPPING_ERROR = "Unable to parse SEW Simulation Results to DTO - Error: ";

    private final SewSimulationResultsRepository sewSimulationResultsRepository;

    private final SewPlantEnvironmentRepository sewPlantEnvironmentRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ModelMapper modelMapper;

    public SewSimulationService(SewSimulationResultsRepository sewSimulationResultsRepository, SewPlantEnvironmentRepository sewPlantEnvironmentRepository, ModelMapper modelMapper, SmartServicesInvocationService smartServicesInvocationService){
        this.sewSimulationResultsRepository = sewSimulationResultsRepository;
        this.sewPlantEnvironmentRepository = sewPlantEnvironmentRepository;
        this.modelMapper = modelMapper;
        this.smartServicesInvocationService = smartServicesInvocationService;
    }

    /**
     * Retrieve latest results regarding SEW Simulation Smart Service
     *
     * @throws ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return SewSimulationResultsDto
     */
    @Override
    public SewSimulationResultsDto retrieveLatestSimulationResults() {
        try {
            Optional<SewSimulationResults> latestResult = sewSimulationResultsRepository.findFirstByOrderByTimestampDesc();
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No SEW Simulation Results found");

            return modelMapper.map(latestResult.get(), SewSimulationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Simulation Results to DTO - Error: " + e.getMessage());
        } catch (UncategorizedElasticsearchException e) {
            log.error("ElasticSearch Exception" + "{}", e.getMessage());
            throw new DatabaseException("An ELK error occurred or no data found for the requested resource");
        }
    }

    /**
     * Retrieve latest results regarding SEW Simulation Smart Service for a specific MODAPTO module
     *
     * @param module MODAPTO module
     * @throws ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return SewSimulationResultsDto
     */
    @Override
    public SewSimulationResultsDto retrieveLatestSimulationResultsByModule(String module) {
        try {
            Optional<SewSimulationResults> latestResult = sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(module);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No SEW Simulation Results for Module: " + module + " found");

            return modelMapper.map(latestResult.get(), SewSimulationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", module, e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Simulation Results to DTO for Module: " + module + " - Error: " + e.getMessage());
        }
    }

    /**
     * Invoke Simulation of Production Schedules
     *
     * @param invocationData Invocation Data for Simulation of Prod. Schedules
     */
    @Override
    public void invokeSimulationOfProductionSchedules(SewSimulationInputDto invocationData) {
        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, PRODUCTION_SCHEDULE_SIMULATION.toString(), "SEW Simulation of Production Schedules");
    }

    @Override
    public SewPlantEnvironmentDto retrieveLatestPlantEnvironment() {
        try {
            Optional<SewPlantEnvironment> latestEnvironment = sewPlantEnvironmentRepository.findFirstByOrderByTimestampCreatedDesc();
            if (latestEnvironment.isEmpty())
                throw new ResourceNotFoundException("No SEW Current Environment found");

            return modelMapper.map(latestEnvironment.get(), SewPlantEnvironmentDto.class);
        } catch (MappingException e) {
            log.error("Unable to parse SEW Current Environment to DTO - Error: {}", e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Current Environment to DTO - Error: " + e.getMessage());
        }
    }

    @Override
    public void uploadPlantEnvironment(SewPlantEnvironmentDto environment) {
        try {
            SewPlantEnvironment currentEnvironment = modelMapper.map(environment, SewPlantEnvironment.class);
            currentEnvironment.setTimestampCreated(LocalDateTime.now());
            sewPlantEnvironmentRepository.save(currentEnvironment);
            log.debug("Successfully uploaded SEW Current Environment");
        } catch (MappingException e) {
            log.error("Unable to map SEW Current Environment DTO to Entity - Error: {}", e.getMessage());
            throw new ModelMappingException("Unable to map SEW Current Environment DTO to Entity - Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to upload SEW Current Environment - Error: {}", e.getMessage());
            throw new ServiceOperationException("Failed to upload SEW Current Environment - Error: " + e.getMessage());
        }
    }
}

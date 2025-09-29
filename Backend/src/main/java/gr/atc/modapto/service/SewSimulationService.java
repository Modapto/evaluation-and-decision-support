package gr.atc.modapto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.serviceInvocations.SewSimulationInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.model.serviceResults.SewSimulationResults;
import gr.atc.modapto.repository.SewSimulationResultsRepository;
import gr.atc.modapto.service.interfaces.IProductionScheduleSimulationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.util.Optional;

import static gr.atc.modapto.enums.OptEngineRoute.PRODUCTION_SCHEDULE_SIMULATION;

@Service
public class SewSimulationService implements IProductionScheduleSimulationService {

    private final Logger log = LoggerFactory.getLogger(SewSimulationService.class);

    private final String MAPPING_ERROR = "Unable to parse SEW Simulation Results to DTO - Error: ";

    private final SewSimulationResultsRepository sewSimulationResultsRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ModelMapper modelMapper;

    public SewSimulationService(SewSimulationResultsRepository sewSimulationResultsRepository, ModelMapper modelMapper, SmartServicesInvocationService smartServicesInvocationService){
        this.sewSimulationResultsRepository = sewSimulationResultsRepository;
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
        }
    }

    /**
     * Retrieve latest results regarding SEW Simulation Smart Service for a specific MODAPTO module
     *
     * @param productionModule MODAPTO module
     * @throws ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return SewSimulationResultsDto
     */
    @Override
    public SewSimulationResultsDto retrieveLatestSimulationResultsByProductionModule(String productionModule) {
        try {
            Optional<SewSimulationResults> latestResult = sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No SEW Simulation Results for Module: " + productionModule + " found");

            return modelMapper.map(latestResult.get(), SewSimulationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", productionModule, e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Simulation Results to DTO for Module: " + productionModule + " - Error: " + e.getMessage());
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
}

package gr.atc.modapto.service;

import gr.atc.modapto.dto.crf.CrfSimulationKittingConfigDto;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.CrfSimulationResults;
import gr.atc.modapto.repository.CrfSimulationKittingConfigRepository;
import gr.atc.modapto.repository.CrfSimulationResultsRepository;
import gr.atc.modapto.service.interfaces.IKitHolderSimulationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.util.Optional;

import static gr.atc.modapto.enums.OptEngineRoute.ROBOT_PICKING_SEQUENCE;

@Service
public class CrfSimulationService implements IKitHolderSimulationService {

    private final Logger log = LoggerFactory.getLogger(CrfSimulationService.class);

    private final String MAPPING_ERROR = "Unable to parse CRF Simulation Results to DTO - Error: ";

    private final String SIM_CONFIG_ID = "sim-current";

    private final CrfSimulationResultsRepository crfSimulationResultsRepository;

    private final CrfSimulationKittingConfigRepository crfSimulationKittingConfigRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandlerService;

    private final ModelMapper modelMapper;

    public CrfSimulationService(ExceptionHandlerService exceptionHandlerService, CrfSimulationKittingConfigRepository crfSimulationKittingConfigRepository, CrfSimulationResultsRepository crfSimulationResultsRepository, ModelMapper modelMapper, SmartServicesInvocationService smartServicesInvocationService){
        this.crfSimulationResultsRepository = crfSimulationResultsRepository;
        this.modelMapper = modelMapper;
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.exceptionHandlerService = exceptionHandlerService;
        this.crfSimulationKittingConfigRepository = crfSimulationKittingConfigRepository;
    }

    /**
     * Retrieve latest results regarding CRF Simulation Smart Service
     *
     * @return CrfSimulationResultsDto
     */
    @Override
    public CrfSimulationResultsDto retrieveLatestSimulationResults() {
        try {
            Optional<CrfSimulationResults> latestResult = crfSimulationResultsRepository.findFirstByOrderByTimestampDesc();
            if (latestResult.isEmpty())
                throw new CustomExceptions.ResourceNotFoundException("No CRF Simulation Results found");

            return modelMapper.map(latestResult.get(), CrfSimulationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException("Unable to parse CRF Simulation Results to DTO - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve latest results regarding CRF Simulation Smart Service for a specific MODAPTO module
     *
     * @param module MODAPTO module
     * @throws  ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws  ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return CrfSimulationResultsDto
     */
    @Override
    public CrfSimulationResultsDto retrieveLatestSimulationResultsByModule(String module) {
        try {
            Optional<CrfSimulationResults> latestResult = crfSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(module);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No CRF Simulation Results for Module: " + module + " found");

            return modelMapper.map(latestResult.get(), CrfSimulationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", module, e.getMessage());
            throw new ModelMappingException("Unable to parse CRF Simulation Results to DTO for Module: " + module + " - Error: " + e.getMessage());
        }
    }

    /**
     * Invoke Simulation of Kit Holders Picking Sequence
     *
     * @param invocationData Invocation Data
     */
    @Override
    public void invokeSimulationOfKhPickingSequence(CrfInvocationInputDto invocationData) {
        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, ROBOT_PICKING_SEQUENCE.toString(), "CRF KH Picking Sequence Simulation");
    }

    /**
     * Retrieve CRF Simulation Kitting Config
     *
     * @return CrfSimulationKittingConfigDto
     */
     @Override
    public CrfSimulationKittingConfigDto retrieveSimulationKittingConfig() {
        return exceptionHandlerService.handleOperation(() -> crfSimulationKittingConfigRepository.findById(SIM_CONFIG_ID)
                .map(config -> modelMapper.map(config, CrfSimulationKittingConfigDto.class))
                .get(), "retrieveSimulationKittingConfig");
    }
}

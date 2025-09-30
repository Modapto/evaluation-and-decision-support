package gr.atc.modapto.service;

import gr.atc.modapto.dto.crf.CrfOptimizationKittingConfigDto;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.crf.CrfOptimizationKittingConfig;
import gr.atc.modapto.model.serviceResults.CrfOptimizationResults;
import gr.atc.modapto.repository.CrfOptimizationKittingConfigRepository;
import gr.atc.modapto.repository.CrfOptimizationResultsRepository;
import gr.atc.modapto.service.interfaces.IKhPickingSequenceOptimizationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.util.Optional;

import static gr.atc.modapto.enums.OptEngineRoute.ROBOT_PICKING_SEQUENCE;

@Service
public class CrfOptimizationService implements IKhPickingSequenceOptimizationService {

    private final Logger log = LoggerFactory.getLogger(CrfOptimizationService.class);

    private final String MAPPING_ERROR = "Unable to parse CRF Optimization Results to DTO - Error: ";

    private final String OPT_CONFIG_ID = "opt-current";

    private final CrfOptimizationResultsRepository crfOptimizationResultsRepository;

    private final CrfOptimizationKittingConfigRepository crfOptimizationKittingConfigRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandlerService;

    private final ModelMapper modelMapper;

    public CrfOptimizationService(CrfOptimizationKittingConfigRepository crfOptimizationKittingConfigRepository,ExceptionHandlerService exceptionHandlerService, CrfOptimizationResultsRepository crfOptimizationResultsRepository, ModelMapper modelMapper, SmartServicesInvocationService smartServicesInvocationService){
        this.crfOptimizationResultsRepository = crfOptimizationResultsRepository;
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.modelMapper = modelMapper;
        this.exceptionHandlerService = exceptionHandlerService;
        this.crfOptimizationKittingConfigRepository = crfOptimizationKittingConfigRepository;
    }

    /**
     * Retrieve latest results regarding CRF Optimization Smart Service
     *
     * @return CrfOptimizationResultsDto
     */
    @Override
    public CrfOptimizationResultsDto retrieveLatestOptimizationResults() {
        try {
            Optional<CrfOptimizationResults> latestResult = crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc();
            if (latestResult.isEmpty())
                throw new CustomExceptions.ResourceNotFoundException("No CRF Optimization Results found");

            return modelMapper.map(latestResult.get(), CrfOptimizationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException("Unable to parse CRF Optimization Results to DTO - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve latest results regarding CRF Optimization Smart Service for a specific MODAPTO module
     *
     * @param moduleId MODAPTO module
     * @throws  ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws  ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return CrfOptimizationResultsDto
     */
    @Override
    public CrfOptimizationResultsDto retrieveLatestOptimizationResultsByModuleId(String moduleId) {
        try {
            Optional<CrfOptimizationResults> latestResult = crfOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(moduleId);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No CRF Optimization Results for Module: " + moduleId + " found");

            return modelMapper.map(latestResult.get(), CrfOptimizationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", moduleId, e.getMessage());
            throw new ModelMappingException("Unable to parse CRF Optimization Results to DTO for Module: " + moduleId + " - Error: " + e.getMessage());
        }
    }

    /**
     * Invoke Optimization of Kit Holder Picking Sequence
     *
     * @param invocationData Invocation Data
     */
    @Override
    public void invokeOptimizationOfKhPickingSequence(CrfInvocationInputDto invocationData) {
        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, ROBOT_PICKING_SEQUENCE.toString(), "CRF KH Picking Sequence Optimization");
    }

    /**
     * Retrieve CRF Optimization Kitting Config
     *
     * @return CrfOptimizationKittingConfigDto
     */
    @Override
    public CrfOptimizationKittingConfigDto retrieveOptimizationKittingConfig() {
        return exceptionHandlerService.handleOperation(() -> {
           return crfOptimizationKittingConfigRepository.findById(OPT_CONFIG_ID)
                   .map(config -> modelMapper.map(config, CrfOptimizationKittingConfigDto.class))
                   .get();
        }, "retrieveOptimizationKittingConfig");
    }
}

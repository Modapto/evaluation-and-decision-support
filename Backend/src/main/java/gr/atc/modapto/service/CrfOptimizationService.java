package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.CrfOptimizationResults;
import gr.atc.modapto.repository.CrfOptimizationResultsRepository;
import gr.atc.modapto.service.interfaces.IOptimizationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.util.Optional;

@Service
public class CrfOptimizationService implements IOptimizationService<CrfOptimizationResultsDto> {

    private final Logger log = LoggerFactory.getLogger(CrfOptimizationService.class);

    private final String MAPPING_ERROR = "Unable to parse CRF Optimization Results to DTO - Error: ";

    private final CrfOptimizationResultsRepository crfOptimizationResultsRepository;

    private final ModelMapper modelMapper;

    public CrfOptimizationService(CrfOptimizationResultsRepository crfOptimizationResultsRepository, ModelMapper modelMapper){
        this.crfOptimizationResultsRepository = crfOptimizationResultsRepository;
        this.modelMapper = modelMapper;
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
     * @param productionModule MODAPTO module
     * @throws  ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws  ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return CrfOptimizationResultsDto
     */
    @Override
    public CrfOptimizationResultsDto retrieveLatestOptimizationResultsByProductionModule(String productionModule) {
        try {
            Optional<CrfOptimizationResults> latestResult = crfOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No CRF Optimization Results for Module: " + productionModule + " found");

            return modelMapper.map(latestResult.get(), CrfOptimizationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", productionModule, e.getMessage());
            throw new ModelMappingException("Unable to parse CRF Optimization Results to DTO for Module: " + productionModule + " - Error: " + e.getMessage());
        }
    }

    @Override
    public void uploadProductionSchedule(SewProductionScheduleDto schedule) {
        // Do nothing
    }
}

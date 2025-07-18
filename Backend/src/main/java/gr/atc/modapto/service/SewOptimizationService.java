package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.SewOptimizationResultsDto;
import gr.atc.modapto.model.serviceResults.SewOptimizationResults;
import gr.atc.modapto.repository.SewOptimizationResultsRepository;
import gr.atc.modapto.service.interfaces.IOptimizationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.util.Optional;

@Service
public class SewOptimizationService implements IOptimizationService<SewOptimizationResultsDto> {

    private final Logger log = LoggerFactory.getLogger(SewOptimizationService.class);

    private final String MAPPING_ERROR = "Unable to parse SEW Optimization Results to DTO - Error: ";

    private final SewOptimizationResultsRepository sewOptimizationResultsRepository;

    private final ModelMapper modelMapper;

    public SewOptimizationService(SewOptimizationResultsRepository sewOptimizationResultsRepository, ModelMapper modelMapper){
        this.sewOptimizationResultsRepository = sewOptimizationResultsRepository;
        this.modelMapper = modelMapper;
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
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No SEW Optimization Results found");

            return modelMapper.map(latestResult.get(), SewOptimizationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Optimization Results to DTO - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve latest results regarding SEW Optimization Smart Service for a specific MODAPTO module
     *
     * @param productionModule MODAPTO module
     * @throws  ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws  ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return SewOptimizationResultsDto
     */
    @Override
    public SewOptimizationResultsDto retrieveLatestOptimizationResultsByProductionModule(String productionModule) {
        try {
            Optional<SewOptimizationResults> latestResult = sewOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No SEW Optimization Results for Module: " + productionModule + " found");

            return modelMapper.map(latestResult.get(), SewOptimizationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", productionModule, e.getMessage());
            throw new ModelMappingException("Unable to parse SEW Optimization Results to DTO for Module: " + productionModule + " - Error: " + e.getMessage());
        }
    }
}

package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.CrfSimulationResults;
import gr.atc.modapto.repository.CrfSimulationResultsRepository;
import gr.atc.modapto.service.interfaces.ISimulationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.util.Optional;

@Service
public class CrfSimulationService implements ISimulationService<CrfSimulationResultsDto> {

    private final Logger log = LoggerFactory.getLogger(CrfSimulationService.class);

    private final String MAPPING_ERROR = "Unable to parse CRF Simulation Results to DTO - Error: ";

    private final CrfSimulationResultsRepository crfSimulationResultsRepository;

    private final ModelMapper modelMapper;

    public CrfSimulationService(CrfSimulationResultsRepository crfSimulationResultsRepository, ModelMapper modelMapper){
        this.crfSimulationResultsRepository = crfSimulationResultsRepository;
        this.modelMapper = modelMapper;
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
     * @param productionModule MODAPTO module
     * @throws  ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws  ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return CrfSimulationResultsDto
     */
    @Override
    public CrfSimulationResultsDto retrieveLatestSimulationResultsByProductionModule(String productionModule) {
        try {
            Optional<CrfSimulationResults> latestResult = crfSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No CRF Simulation Results for Module: " + productionModule + " found");

            return modelMapper.map(latestResult.get(), CrfSimulationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", productionModule, e.getMessage());
            throw new ModelMappingException("Unable to parse CRF Simulation Results to DTO for Module: " + productionModule + " - Error: " + e.getMessage());
        }
    }
}

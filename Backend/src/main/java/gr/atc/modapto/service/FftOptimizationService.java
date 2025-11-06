package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceInvocations.FftOptimizationInputDto;
import gr.atc.modapto.dto.serviceResults.fft.FftOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions.ModelMappingException;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.model.serviceResults.FftOptimizationResults;
import gr.atc.modapto.repository.FftOptimizationResultsRepository;
import gr.atc.modapto.service.interfaces.IRobotConfigurationOptimizationService;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static gr.atc.modapto.enums.OptEngineRoute.ROBOT_CONFIGURATION;

@Service
public class FftOptimizationService implements IRobotConfigurationOptimizationService {

    private final Logger log = LoggerFactory.getLogger(FftOptimizationService.class);

    private final String MAPPING_ERROR = "Unable to parse FFT Optimization Results to DTO - Error: ";

    private final FftOptimizationResultsRepository fftOptimizationResultsRepository;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ModelMapper modelMapper;

    public FftOptimizationService(FftOptimizationResultsRepository fftOptimizationResultsRepository, ModelMapper modelMapper, SmartServicesInvocationService smartServicesInvocationService){
        this.fftOptimizationResultsRepository = fftOptimizationResultsRepository;
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieve latest results regarding FFT Optimization Smart Service
     *
     * @return FftOptimizationResultsDto
     */
    @Override
    public FftOptimizationResultsDto retrieveLatestOptimizationResults() {
        try {
            Optional<FftOptimizationResults> latestResult = fftOptimizationResultsRepository.findFirstByOrderByTimestampDesc();
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No FFT Optimization Results found");

            return modelMapper.map(latestResult.get(), FftOptimizationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException("Unable to parse FFT Optimization Results to DTO - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve latest results regarding FFT Optimization Smart Service for a specific MODAPTO module
     *
     * @param moduleId MODAPTO module
     * @throws  ResourceNotFoundException Thrown when the requested resource not found in Elasticsearch
     * @throws  ModelMappingException Thrown when a mismatch exists between DTO and Entity data
     * @return FftOptimizationResultsDto
     */
    @Override
    public FftOptimizationResultsDto retrieveLatestOptimizationResultsByModuleId(String moduleId) {
        try {
            Optional<FftOptimizationResults> latestResult = fftOptimizationResultsRepository.findFirstByModuleOrderByTimestampDesc(moduleId);
            if (latestResult.isEmpty())
                throw new ResourceNotFoundException("No FFT Optimization Results for Module: " + moduleId + " found");

            return modelMapper.map(latestResult.get(), FftOptimizationResultsDto.class);
        } catch (MappingException e){
            log.error(MAPPING_ERROR + "for Module {} - {}", moduleId, e.getMessage());
            throw new ModelMappingException("Unable to parse FFT Optimization Results to DTO for Module: " + moduleId + " - Error: " + e.getMessage());
        }
    }

    /**
     * Invoke Optimization of Robot Configuration
     *
     * @param invocationData Invocation Data
     */
    @Override
    public void invokeOptimizationOfRobotConfiguration(FftOptimizationInputDto invocationData) {
        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, ROBOT_CONFIGURATION.toString(), "FFT Robot Configuration Optimization");
    }
}

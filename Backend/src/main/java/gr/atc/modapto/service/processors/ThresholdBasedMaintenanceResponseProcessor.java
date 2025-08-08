package gr.atc.modapto.service.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.dt.DtResponseDto;

import static gr.atc.modapto.exception.CustomExceptions.*;

import gr.atc.modapto.dto.dt.SmartServiceResponse;
import gr.atc.modapto.dto.serviceResults.sew.SewThresholdBasedPredictiveMaintenanceOutputDto;
import gr.atc.modapto.model.serviceResults.SewThresholdBasedPredictiveMaintenanceResult;
import gr.atc.modapto.repository.SewThresholdBasedPredictiveMaintenanceRepository;
import gr.atc.modapto.service.interfaces.IResponseProcessor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Component
public class ThresholdBasedMaintenanceResponseProcessor implements IResponseProcessor<SewThresholdBasedPredictiveMaintenanceOutputDto> {

    private final Logger logger = LoggerFactory.getLogger(ThresholdBasedMaintenanceResponseProcessor.class);
    
    private final SewThresholdBasedPredictiveMaintenanceRepository repository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    public ThresholdBasedMaintenanceResponseProcessor(
            SewThresholdBasedPredictiveMaintenanceRepository repository,
            ModelMapper modelMapper,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public SewThresholdBasedPredictiveMaintenanceOutputDto processResponse(
            ResponseEntity<DtResponseDto> response,
            String moduleId, 
            String smartServiceId) {
        
        // Validate response
        if (response == null || response.getBody() == null) {
            throw new DtmServerErrorException("No response received from DTM service for threshold maintenance");
        }
        
        DtResponseDto dtmResponse = response.getBody();

        if (dtmResponse == null) {
            logger.error("DTM service response body is null for threshold maintenance.");
            throw new DtmServerErrorException("DTM service returned a null body for threshold maintenance");
        }

        if (!dtmResponse.isSuccess()) {
            logger.error("DTM service execution failed for threshold maintenance. Messages: {}",
                    Optional.ofNullable(dtmResponse.getMessages()).orElse(List.of("No error messages provided")));
            throw new DtmServerErrorException("DTM service execution failed for threshold maintenance");
        }
        
        try {
            // Convert output arguments to specific Smart Service results DTO
            SmartServiceResponse serviceResponse = objectMapper.convertValue(
                dtmResponse.getOutputArguments(),
                    SmartServiceResponse.class
            );

            // Decode Response from Base64 Encoding to specific DTO
            byte[] decodedBytes = Base64.getDecoder().decode(serviceResponse.getResponse());
            SewThresholdBasedPredictiveMaintenanceOutputDto outputDto =
                    objectMapper.readValue(decodedBytes, SewThresholdBasedPredictiveMaintenanceOutputDto.class);
            
            // Set metadata with timestamp from Elasticsearch
            outputDto.setModuleId(moduleId);
            outputDto.setSmartServiceId(smartServiceId);
            outputDto.setTimestamp(LocalDateTime.now());
            
            // Save to database
            SewThresholdBasedPredictiveMaintenanceResult result = modelMapper.map(
                outputDto,
                    SewThresholdBasedPredictiveMaintenanceResult.class
            );
            repository.save(result);
            
            logger.debug("Successfully processed and saved  threshold-based predictive maintenance results for module: {}", moduleId);
            
            return outputDto;

        } catch (Exception e) {
            logger.error("Error processing  threshold-based predictive maintenance response for module {}: {}", moduleId, e.getMessage());
            throw new SmartServiceInvocationException("Failed to process DTM threshold-based predictive maintenance response: " + e.getMessage());
        }
    }
}
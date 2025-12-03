package gr.atc.modapto.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.serviceInvocations.FftSustainabilityAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceResults.fft.FftSustainabilityAnalyticsResultsDto;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.service.interfaces.ISustainabilityAnalyticsService;

@Service
public class FftSustainabilityAnalyticsService implements ISustainabilityAnalyticsService {

    private final Logger log = LoggerFactory.getLogger(FftSustainabilityAnalyticsService.class);

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandler;

    private final ObjectMapper objectMapper;

    public FftSustainabilityAnalyticsService(ExceptionHandlerService exceptionHandler, SmartServicesInvocationService smartServicesInvocationService, ObjectMapper objectMapper){
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.exceptionHandler = exceptionHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * Extract Sustainability Analytics for FFT Pilot Case
     *
     * @param request : Invocation Data
     * @return FftSustainabilityAnalyticsResultsDto
     */
    @Override
    public FftSustainabilityAnalyticsResultsDto extractFftSustainabilityAnalytics(GlobalRequestDto<FftSustainabilityAnalyticsInputDto> request) {
        return exceptionHandler.handleOperation(() -> {
            DtInputDto<FftSustainabilityAnalyticsInputDto> dtInput = DtInputDto.<FftSustainabilityAnalyticsInputDto>builder()
                    .inputArguments(request.getInput())
                    .build();

            ResponseEntity<DtResponseDto> response = smartServicesInvocationService.invokeSmartService(
                    request.getSmartServiceId(),
                    request.getModuleId(),
                    dtInput,
                    ModaptoHeader.SYNC);

            log.debug("Successfully invoked FFT Sustainability Analytics. Processing results..");

            FftSustainabilityAnalyticsResultsDto output = null;
            if (smartServicesInvocationService.validateDigitalTwinResponse(response, "FFT Sustainability Analytics")) {
                if (response.getBody().getOutputArguments() != null) {
                    log.debug("Captured Output: {}", response.getBody().getOutputArguments());
                    output = objectMapper.convertValue(
                            response.getBody().getOutputArguments(),
                            FftSustainabilityAnalyticsResultsDto.class
                    );
                }
            }

            return output;
        }, "extractFftSustainabilityAnalytics");
    }
}

package gr.atc.modapto.service;

import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.serviceInvocations.FftSustainabilityAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceResults.fft.FftSustainabilityAnalyticsResultsDto;
import gr.atc.modapto.service.interfaces.ISustainabilityAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FftSustainabilityAnalyticsService implements ISustainabilityAnalyticsService {

    private final Logger log = LoggerFactory.getLogger(FftSustainabilityAnalyticsService.class);

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandler;

    public FftSustainabilityAnalyticsService(ExceptionHandlerService exceptionHandler, SmartServicesInvocationService smartServicesInvocationService){
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.exceptionHandler = exceptionHandler;
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
            ResponseEntity<DtResponseDto> response = smartServicesInvocationService.formulateAndImplementSyncSmartServiceRequest(request.getInput(), request.getModuleId(), request.getSmartServiceId());

            log.debug("Successfully invoked FFT Sustainability Analytics to produce the Histogram..Processing results..");

            FftSustainabilityAnalyticsResultsDto output = null;;
            if (smartServicesInvocationService.validateDigitalTwinResponse(response, "FFT Sustainability Analytics"))
                output = smartServicesInvocationService.decodeDigitalTwinResponseToDto(FftSustainabilityAnalyticsResultsDto.class, response.getBody(), "FFT Sustainability Analytics");

            return output;
        }, "extractFftSustainabilityAnalytics");
    }
}

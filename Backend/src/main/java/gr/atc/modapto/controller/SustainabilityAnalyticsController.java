package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceInvocations.FftSustainabilityAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceResults.fft.FftSustainabilityAnalyticsResultsDto;
import gr.atc.modapto.service.interfaces.ISustainabilityAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eds/sustainability-analytics")
@Tag(name = "Sustainability Analytics Controller", description = "Handles optimization functionalities amongst Pilot Cases")
public class SustainabilityAnalyticsController {

    private final ISustainabilityAnalyticsService sustainabilityAnalyticsService;

    public SustainabilityAnalyticsController(ISustainabilityAnalyticsService sustainabilityAnalyticsService){
        this.sustainabilityAnalyticsService = sustainabilityAnalyticsService;
    }

    /**
     * Extract Sustainability Analytics Data [FFT]
     *
     * @param request : Smart Service ID, Modapto Module ID and Invocation Data
     * @return FftSustainabilityAnalyticsResultsDto
     */
    @Operation(summary = "Extract Sustainability Analytics Data [FFT]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sustainability Analytics data extracted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again.")
    })
    @PostMapping("/pilots/fft/invoke")
    public ResponseEntity<BaseResponse<FftSustainabilityAnalyticsResultsDto>> extractFftSustainabilityAnalytics(@RequestBody @Valid GlobalRequestDto<FftSustainabilityAnalyticsInputDto> request) {
        FftSustainabilityAnalyticsResultsDto sustainabilityAnalyticsData = sustainabilityAnalyticsService.extractFftSustainabilityAnalytics(request);
        return new ResponseEntity<>(
                BaseResponse.success(sustainabilityAnalyticsData, "Sustainability Analytics data extracted successfully"),
                HttpStatus.OK);
    }

}

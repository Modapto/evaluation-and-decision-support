package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceResults.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.SewOptimizationResultsDto;
import gr.atc.modapto.service.interfaces.IOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eds/optimization")
public class OptimizationController {

    private final IOptimizationService<CrfOptimizationResultsDto> crfOptimizationService;

    private final IOptimizationService<SewOptimizationResultsDto> sewOptimizationService;

    public OptimizationController(IOptimizationService<CrfOptimizationResultsDto> crfOptimizationService, IOptimizationService<SewOptimizationResultsDto> sewOptimizationService) {
        this.crfOptimizationService = crfOptimizationService;
        this.sewOptimizationService = sewOptimizationService;
    }

    /**
     * Retrieve latest CRF Optimization Results
     *
     * @return CrfOptimizationResultsDto
     */
    @Operation(summary = "Retrieve latest CRF Optimization Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest CRF Optimization results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No CRF Optimization Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/crf/latest")
    public ResponseEntity<BaseResponse<CrfOptimizationResultsDto>> retrieveLatestCrfResults() {
        return new ResponseEntity<>(BaseResponse.success(crfOptimizationService.retrieveLatestOptimizationResults(),
                "Latest CRF Optimization results retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Retrieve latest CRF Optimization Results by Production Module
     *
     * @return CrfOptimizationResultsDto
     */
    @Operation(summary = "Retrieve latest CRF Optimization Results by MODAPTO Module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest CRF Optimization results for Module: [moduleId] retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No CRF Optimization Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/crf/modules/{moduleId}/latest")
    public ResponseEntity<BaseResponse<CrfOptimizationResultsDto>> retrieveLatestCrfResultsByProductionModule(@PathVariable String moduleId) {
        return new ResponseEntity<>(BaseResponse.success(crfOptimizationService.retrieveLatestOptimizationResultsByProductionModule(moduleId),
                "Latest CRF Optimization results for Module " + moduleId + " retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Retrieve latest SEW Optimization Results
     *
     * @return SewOptimizationResultsDto
     */
    @Operation(summary = "Retrieve latest SEW Optimization Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest SEW Optimization results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No SEW Optimization Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/latest")
    public ResponseEntity<BaseResponse<SewOptimizationResultsDto>> retrieveLatestSewResults() {
        return new ResponseEntity<>(BaseResponse.success(sewOptimizationService.retrieveLatestOptimizationResults(),
                "Latest SEW Optimization results retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Retrieve latest SEW Optimization Results by Production Module
     *
     * @return SewOptimizationResultsDto
     */
    @Operation(summary = "Retrieve latest SEW Optimization Results by MODAPTO Module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest SEW Optimization results for Module: [moduleId] retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No SEW Optimization Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/modules/{moduleId}/latest")
    public ResponseEntity<BaseResponse<SewOptimizationResultsDto>> retrieveLatestSewResultsByProductionModule(@PathVariable String moduleId) {
        return new ResponseEntity<>(BaseResponse.success(sewOptimizationService.retrieveLatestOptimizationResultsByProductionModule(moduleId),
                "Latest SEW Optimization results for Module " + moduleId + " retrieved successfully"), HttpStatus.OK);
    }
}

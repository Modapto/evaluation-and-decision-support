package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.service.interfaces.ISelfAwarenessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eds/self-awareness")
@Validated
@Tag(name = "Self Awareness Controller", description = "API Controller for managing Self-Awareness")
public class SelfAwarenessController {

    private final ISelfAwarenessService selfAwarenessService;

    public SelfAwarenessController(ISelfAwarenessService selfAwarenessService){
        this.selfAwarenessService = selfAwarenessService;
    }

    /**
     * Invoke Self-Awareness Monitoring KPIs algorithm [SEW - SA1]
     *
     * @param invocationData : Input Data for algorithm
     * @return Message of success
     */
    @Operation(summary = "Invoke Self-Awareness Monitoring KPIs algorithm [SEW - SA1]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Self-Awareness Monitoring KPIs algorithm invoked successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/monitor-kpis/invoke")
    public ResponseEntity<BaseResponse<String>> invokeSelfAwarenessMonitoringKpisProcess(@Valid @RequestBody SewSelfAwarenessMonitoringKpisInputDto invocationData) {
        selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(invocationData);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Self-Awareness Monitoring KPIs algorithm invoked successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve latest Self-Awareness Monitoring KPIs Results
     *
     * @return SewSelfAwarenessMonitoringKpisResultsDto
     */
    @Operation(summary = "Retrieve latest Self-Awareness Monitoring KPIs Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest Self-Awareness Monitoring KPIs results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No Self-Awareness Monitoring KPIs results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/monitor-kpis/results/latest")
    public ResponseEntity<BaseResponse<SewSelfAwarenessMonitoringKpisResultsDto>> retrieveLatestSelfAwarenessMonitoringKpisResults() {
        return new ResponseEntity<>(
                BaseResponse.success(
                        selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults(),
                        "Latest Self-Awareness Monitoring KPIs results retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve latest Self-Awareness Monitoring KPIs Results by Module ID
     *
     * @param moduleId : Module ID
     * @return SewSelfAwarenessMonitoringKpisResultsDto
     */
    @Operation(summary = "Retrieve latest Self-Awareness Monitoring KPIs Results by Module ID", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest Self-Awareness Monitoring KPIs results for Module retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No Self-Awareness Monitoring KPIs results found for the specified module"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/monitor-kpis/results/{moduleId}/latest")
    public ResponseEntity<BaseResponse<SewSelfAwarenessMonitoringKpisResultsDto>> retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(
            @PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId),
                        "Latest Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all Self-Awareness Monitoring KPIs Results
     *
     * @return List<SewSelfAwarenessMonitoringKpisResultsDto>
     */
    @Operation(summary = "Retrieve all Self-Awareness Monitoring KPIs Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Self-Awareness Monitoring KPIs results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/monitor-kpis/results")
    public ResponseEntity<BaseResponse<List<SewSelfAwarenessMonitoringKpisResultsDto>>> retrieveAllSelfAwarenessMonitoringKpisResults() {
        return new ResponseEntity<>(
                BaseResponse.success(
                        selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults(),
                        "All Self-Awareness Monitoring KPIs results retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all Self-Awareness Monitoring KPIs Results by Module ID
     *
     * @param moduleId : Module ID
     * @return List<SewSelfAwarenessMonitoringKpisResultsDto>
     */
    @Operation(summary = "Retrieve all Self-Awareness Monitoring KPIs Results by Module ID", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Self-Awareness Monitoring KPIs results for Module retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/monitor-kpis/results/{moduleId}")
    public ResponseEntity<BaseResponse<List<SewSelfAwarenessMonitoringKpisResultsDto>>> retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(
            @PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId),
                        "All Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"),
                HttpStatus.OK);
    }
}

package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSimulationInputDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.service.interfaces.IKitHolderSimulationService;
import gr.atc.modapto.service.interfaces.IProductionScheduleSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eds/simulation")
@Tag(name = "Simulation Controller", description = "Handles simulation functionalities amongst Pilot Cases")
public class SimulationController {

    private final IKitHolderSimulationService crfSimulationService;

    private final IProductionScheduleSimulationService sewSimulationService;

    public SimulationController(IKitHolderSimulationService crfSimulationService, IProductionScheduleSimulationService sewSimulationService) {
        this.crfSimulationService = crfSimulationService;
        this.sewSimulationService = sewSimulationService;
    }

    //--------------------------------------------- CRF -----------------------------------------------------------

    /**
     * Retrieve latest CRF Simulation Results
     *
     * @return CrfSimulationResultsDto
     */
    @Operation(summary = "Retrieve latest CRF Simulation Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest CRF Simulation results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No CRF Simulation Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/crf/latest")
    public ResponseEntity<BaseResponse<CrfSimulationResultsDto>> retrieveLatestCrfResults() {
        return new ResponseEntity<>(BaseResponse.success(crfSimulationService.retrieveLatestSimulationResults(),
                "Latest CRF Simulation results retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Retrieve latest CRF Simulation Results by Production Module
     *
     * @return CrfSimulationResultsDto
     */
    @Operation(summary = "Retrieve latest CRF Simulation Results by MODAPTO Module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest CRF Simulation results for Module: [moduleId] retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No CRF Simulation Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/crf/modules/{moduleId}/latest")
    public ResponseEntity<BaseResponse<CrfSimulationResultsDto>> retrieveLatestCrfResultsByProductionModule(@PathVariable String moduleId) {
        return new ResponseEntity<>(BaseResponse.success(crfSimulationService.retrieveLatestSimulationResultsByProductionModule(moduleId),
                "Latest CRF Simulation results for Module " + moduleId + " retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Invoke Simulation algorithm to optimize KH Picking Sequence
     *
     * @param invocationData : CRF Invocation Data
     * @return Message of Success or Error
     */
    @Operation(summary = "Invoke Simulation service for CRF for KH Picking Sequence", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request for simulation of CRF KH Picking Sequence has been successfully submitted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Unable to invoke designated smart service"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/crf/kh-picking-sequence/simulate")
    public ResponseEntity<BaseResponse<SewOptimizationResultsDto>> invokeSimulationOfKhPickingSequence(@RequestBody CrfInvocationInputDto invocationData) {
        crfSimulationService.invokeSimulationOfKhPickingSequence(invocationData);
        return new ResponseEntity<>(BaseResponse.success(null,
                "Request for simulation of CRF KH Picking Sequence has been successfully submitted"), HttpStatus.OK);
    }

    //--------------------------------------------- SEW -----------------------------------------------------------
    /**
     * Retrieve latest SEW Simulation Results
     *
     * @return SewSimulationResultsDto
     */
    @Operation(summary = "Retrieve latest SEW Simulation Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest SEW Simulation results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No SEW Simulation Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/latest")
    public ResponseEntity<BaseResponse<SewSimulationResultsDto>> retrieveLatestSewResults() {
        return new ResponseEntity<>(BaseResponse.success(sewSimulationService.retrieveLatestSimulationResults(),
                "Latest SEW Simulation results retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Retrieve latest SEW Simulation Results by Production Module
     *
     * @return SewSimulationResultsDto
     */
    @Operation(summary = "Retrieve latest SEW Simulation Results by MODAPTO Module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest SEW Simulation results for Module: [moduleId] retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No SEW Simulation Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/modules/{moduleId}/latest")
    public ResponseEntity<BaseResponse<SewSimulationResultsDto>> retrieveLatestSewResultsByProductionModule(@PathVariable String moduleId) {
        return new ResponseEntity<>(BaseResponse.success(sewSimulationService.retrieveLatestSimulationResultsByProductionModule(moduleId),
                "Latest SEW Simulation results for Module " + moduleId + " retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Invoke Simulation algorithm to optimize SEW Production Schedules
     *
     * @param invocationData : SEW Invocation Data
     * @return Message of Success or Error
     */
    @Operation(summary = "Invoke Simulation service for SEW for Production Schedules", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request for simulation of SEW Production Schedules has been successfully submitted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Unable to invoke designated smart service"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/sew/schedules/simulate")
    public ResponseEntity<BaseResponse<SewOptimizationResultsDto>> invokeSimulationOfProductionSchedules(@RequestBody SewSimulationInputDto invocationData) {
        sewSimulationService.invokeSimulationOfProductionSchedules(invocationData);
        return new ResponseEntity<>(BaseResponse.success(null,
                "Request for simulation of SEW Production Schedules has been successfully submitted"), HttpStatus.OK);
    }
}

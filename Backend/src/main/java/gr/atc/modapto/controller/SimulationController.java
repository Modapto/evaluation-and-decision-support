package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.service.interfaces.ISimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eds/simulation")
@Tag(name = "Simulation Controller", description = "Handles simulation functionalities amongst Pilot Cases")
public class SimulationController {

    private final ISimulationService<CrfSimulationResultsDto> crfSimulationService;

    private final ISimulationService<SewSimulationResultsDto> sewSimulationService;

    public SimulationController(ISimulationService<CrfSimulationResultsDto> crfSimulationService, ISimulationService<SewSimulationResultsDto> sewSimulationService) {
        this.crfSimulationService = crfSimulationService;
        this.sewSimulationService = sewSimulationService;
    }

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
}

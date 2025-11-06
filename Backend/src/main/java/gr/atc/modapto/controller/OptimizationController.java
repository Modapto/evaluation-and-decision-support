package gr.atc.modapto.controller;

import gr.atc.modapto.dto.crf.CrfOptimizationKittingConfigDto;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceInvocations.FftOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.fft.FftOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.service.interfaces.IKhPickingSequenceOptimizationService;
import gr.atc.modapto.service.interfaces.IProductionScheduleOptimizationService;
import gr.atc.modapto.service.interfaces.IRobotConfigurationOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eds/optimization")
@Tag(name = "Optimization Controller", description = "Handles optimization functionalities amongst Pilot Cases")
public class OptimizationController {

    private final IKhPickingSequenceOptimizationService khPickingSequenceOptimizationService;

    private final IProductionScheduleOptimizationService productionScheduleOptimizationService;

    private final IRobotConfigurationOptimizationService robotConfigurationOptimizationService;

    public OptimizationController(IRobotConfigurationOptimizationService robotConfigurationOptimizationService, IKhPickingSequenceOptimizationService khPickingSequenceOptimizationService, IProductionScheduleOptimizationService productionScheduleOptimizationService) {
        this.khPickingSequenceOptimizationService = khPickingSequenceOptimizationService;
        this.productionScheduleOptimizationService = productionScheduleOptimizationService;
        this.robotConfigurationOptimizationService = robotConfigurationOptimizationService;
    }

    //--------------------------------------------- FFT -----------------------------------------------------------
    /**
     * Retrieve latest FFT Optimization Results
     *
     * @return FftOptimizationResultsDto
     */
    @Operation(summary = "Retrieve latest CRF Optimization Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest FFT Optimization results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No CRF Optimization Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/fft/latest")
    public ResponseEntity<BaseResponse<FftOptimizationResultsDto>> retrieveLatestFftResults() {
        return new ResponseEntity<>(BaseResponse.success(robotConfigurationOptimizationService.retrieveLatestOptimizationResults(),
                "Latest FFT Optimization results retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Retrieve latest FFT Optimization Results by Production Module
     *
     * @return FftOptimizationResultsDto
     */
    @Operation(summary = "Retrieve latest CRF Optimization Results by MODAPTO Module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest FFT Optimization results for Module: [moduleId] retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No CRF Optimization Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/fft/modules/{moduleId}/latest")
    public ResponseEntity<BaseResponse<FftOptimizationResultsDto>> retrieveLatestFftResultsByProductionModule(@PathVariable String moduleId) {
        return new ResponseEntity<>(BaseResponse.success(robotConfigurationOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId),
                "Latest FFT Optimization results for Module " + moduleId + " retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Invoke Optimization algorithm to optimize Robot Configuration
     *
     * @param invocationData : FFT Invocation Data
     * @return Message of Success or Error
     */
    @Operation(summary = "Invoke Optimization service for FFT for Robot Configuration", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request for optimization of FFT Robot Configuration has been successfully submitted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Unable to invoke designated smart service"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/fft/robot-configuration/optimize")
    public ResponseEntity<BaseResponse<Void>> invokeOptimizationOfRobotConfiguration(@RequestBody FftOptimizationInputDto invocationData) {
        robotConfigurationOptimizationService.invokeOptimizationOfRobotConfiguration(invocationData);
        return new ResponseEntity<>(BaseResponse.success(null,
                "Request for optimization of FFT Robot Configuration has been successfully submitted"), HttpStatus.OK);
    }
    //--------------------------------------------- CRF -----------------------------------------------------------

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
        return new ResponseEntity<>(BaseResponse.success(khPickingSequenceOptimizationService.retrieveLatestOptimizationResults(),
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
        return new ResponseEntity<>(BaseResponse.success(khPickingSequenceOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId),
                "Latest CRF Optimization results for Module " + moduleId + " retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Invoke Optimization algorithm to optimize KH Picking Sequence
     *
     * @param invocationData : CRF Invocation Data
     * @return Message of Success or Error
     */
    @Operation(summary = "Invoke Optimization service for CRF for KH Picking Sequence", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request for optimization of CRF KH Picking Sequence has been successfully submitted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Unable to invoke designated smart service"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/crf/kh-picking-sequence/optimize")
    public ResponseEntity<BaseResponse<Void>> invokeOptimizationOfKhPickingSequence(@RequestBody CrfInvocationInputDto invocationData) {
        khPickingSequenceOptimizationService.invokeOptimizationOfKhPickingSequence(invocationData);
        return new ResponseEntity<>(BaseResponse.success(null,
                "Request for optimization of CRF KH Picking Sequence has been successfully submitted"), HttpStatus.OK);
    }

    /**
     *  Retrieve stored Kitting Config [CRF]
     *
     * @return CrfOptimizationKittingConfigDto
     */
    @Operation(summary = "Retrieve stored Kitting Config [CRF]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kitting Configs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No CRF Simulation Kitting Config found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/crf/kitting-configs")
    public ResponseEntity<BaseResponse<CrfOptimizationKittingConfigDto>> retrieveSimulationKittingConfig() {
        return new ResponseEntity<>(BaseResponse.success(khPickingSequenceOptimizationService.retrieveOptimizationKittingConfig(),
                "Kitting Configs retrieved successfully"), HttpStatus.OK);
    }

    //--------------------------------------------- SEW -----------------------------------------------------------
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
        return new ResponseEntity<>(BaseResponse.success(productionScheduleOptimizationService.retrieveLatestOptimizationResults(),
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
        return new ResponseEntity<>(BaseResponse.success(productionScheduleOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId),
                "Latest SEW Optimization results for Module " + moduleId + " retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Upload JSON Production Schedule for the weekly plan of SEW
     *
     * @param scheduleDto : SEW Production Schedule for the Week
     * @return Message of Success or Error
     */
    @Operation(summary = "Upload JSON Production Schedule for the weekly plan of SEW", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SEW Production Schedule has been successfully uploaded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/sew/schedules/upload")
    public ResponseEntity<BaseResponse<Void>> uploadSewProductionSchedule(@RequestBody SewProductionScheduleDto scheduleDto) {
        productionScheduleOptimizationService.uploadProductionSchedule(scheduleDto);
        return new ResponseEntity<>(BaseResponse.success(null,
                "SEW Production Schedule has been successfully uploaded"), HttpStatus.OK);
    }

    /**
     * Retrieve latest SEW stored Prod. Schedule
     *
     * @return SewProductionScheduleDto
     */
    @Operation(summary = "Retrieve latest SEW stored Prod. Schedule", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest SEW Production Schedule retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/schedules/latest")
    public ResponseEntity<BaseResponse<SewProductionScheduleDto>> retrieveLatestProductionSchedule() {
        SewProductionScheduleDto latestSchedule = productionScheduleOptimizationService.retrieveLatestProductionSchedule();
        return new ResponseEntity<>(BaseResponse.success(latestSchedule,
                "Latest SEW Production Schedule retrieved successfully"), HttpStatus.OK);
    }

    /**
     * Invoke Optimization algorithm to optimize Production Schedules
     *
     * @param invocationData : SEW Production Schedule Optimization Input
     * @return Message of Success or Error
     */
    @Operation(summary = "Upload JSON Production Schedule for the weekly plan of SEW", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request for optimization of SEW production schedules has been successfully submitted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Unable to invoke designated smart service"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/sew/schedules/optimize")
    public ResponseEntity<BaseResponse<Void>> invokeOptimizationOfProductionSchedules(@RequestBody SewOptimizationInputDto invocationData) {
        productionScheduleOptimizationService.invokeOptimizationOfProductionSchedules(invocationData);
        return new ResponseEntity<>(BaseResponse.success(null,
                "Request for optimization of SEW production schedules has been successfully submitted"), HttpStatus.OK);
    }
}

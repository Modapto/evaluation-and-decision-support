package gr.atc.modapto.controller;

import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.dto.serviceResults.sew.SewGroupingPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewThresholdBasedPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import gr.atc.modapto.dto.sew.SewComponentInfoDto;
import gr.atc.modapto.dto.serviceInvocations.SewGroupingPredictiveMaintenanceInputDataDto;
import gr.atc.modapto.service.interfaces.IPredictiveMaintenanceService;
import gr.atc.modapto.validation.ValidExcelFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/eds/maintenance")
@Validated
@Tag(name = "Predictive Maintenance Controller", description = "API Controller for managing Predictive Maintenance")
public class PredictiveMaintenanceController {

    private final IPredictiveMaintenanceService predictiveMaintenanceService;

    public PredictiveMaintenanceController(IPredictiveMaintenanceService predictiveMaintenanceService) {
        this.predictiveMaintenanceService = predictiveMaintenanceService;
    }

    /**
     * Upload CORIM file regarding Maintenance Operations
     *
     * @param file : Excel file with CORIM data
     * @return Message of success
     */
    @Operation(summary = "Upload CORIM file with Maintenance Data", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Corim file uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/uploadCorimFile")
    public ResponseEntity<BaseResponse<String>> uploadCorimFile(
            @ValidExcelFile MultipartFile file) {
        predictiveMaintenanceService.storeCorimData(file);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Corim file uploaded successfully"),
                HttpStatus.OK);
    }

    /**
     * Upload JSON of Components List for SEW and store them in PKB
     *
     * @param componentInfoList : List of SEW Component
     * @return Message of success or error
     */
    @Operation(summary = "Upload JSON of Components List for SEW", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Components file uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/uploadComponentsList")
    public ResponseEntity<BaseResponse<String>> uploadComponentsList(@RequestBody @Valid List<SewComponentInfoDto> componentInfoList) {
        predictiveMaintenanceService.storeComponentsListData(componentInfoList);
        predictiveMaintenanceService.locateLastMaintenanceActionForStoredComponents();
        return new ResponseEntity<>(
                BaseResponse.success(null, "Components file uploaded successfully"),
                HttpStatus.OK);
    }

    /**
     * Invoke Predictive Maintenance Service for Grouping Maintenance (PdM 1)
     * In that case data are returned through the Message Bus (Kafka)
     *
     * @param invocationData : Invocation data for Grouping Predictive Maintenance service
     * @return Message of success or error
     */
    @Operation(summary = "Invoke Predictive Maintenance Service for Grouping Maintenance", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Predictive Maintenance service for Grouping Maintenance invoked successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again.")
    })
    @PostMapping("/predict/grouping-maintenance")
    public ResponseEntity<BaseResponse<String>> invokeGroupingPredictiveMaintenance(@Valid @RequestBody SewGroupingPredictiveMaintenanceInputDataDto invocationData) {
        predictiveMaintenanceService.invokeGroupingPredictiveMaintenance(invocationData);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Predictive Maintenance service for Grouping Maintenance invoked successfully"),
                HttpStatus.OK);
    }

    /**
     * Invoke Predictive Maintenance Service for Threshold Based Maintenance (PdM 2)
     * In that case data are returned through the HTTP request
     *
     * @param invocationData : Invocation data for Grouping Predictive Maintenance service
     * @return SewThresholdBasedPredictiveMaintenanceOutputDto
     */
    @Operation(summary = "Invoke Predictive Maintenance Service for Threshold-Based Maintenance", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Predictive Maintenance service for Threshold-Based Maintenance completed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again.")
    })
    @PostMapping("/predict/threshold-based-maintenance")
    public ResponseEntity<BaseResponse<SewThresholdBasedPredictiveMaintenanceOutputDto>> invokeThresholdBasedPredictiveMaintenance(@Valid @RequestBody SewThresholdBasedMaintenanceInputDataDto invocationData) {
        SewThresholdBasedPredictiveMaintenanceOutputDto output = predictiveMaintenanceService.invokeThresholdBasedPredictiveMaintenance(invocationData);
        return new ResponseEntity<>(
                BaseResponse.success(output, "Predictive Maintenance service for Threshold-Based Maintenance completed successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve the latest grouping predictive maintenance results for a specific Module [PdM1]
     *
     * @param moduleId : Module ID
     * @return SewGroupingPredictiveMaintenanceOutputDto
     */
    @Operation(summary = "Retrieve the latest grouping predictive maintenance results for a specific Module [PdM1]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouping Predictive Maintenance latest results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No SEW Grouping Predictive Maintenance Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/predict/grouping-maintenance/results")
    public ResponseEntity<BaseResponse<SewGroupingPredictiveMaintenanceOutputDto>> retrieveGroupingMaintenanceLatestResults(
            @RequestParam String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        predictiveMaintenanceService.retrieveLatestGroupingMaintenanceResults(moduleId),
                        "Grouping Predictive Maintenance latest results retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve the latest threshold-based predictive maintenance results for a specific Module [PdM2]
     *
     * @param moduleId : Module ID
     * @return SewThresholdBasedPredictiveMaintenanceOutputDto
     */
    @Operation(summary = "Retrieve the latest threshold-based  predictive maintenance results for a specific Module[PdM2]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Threshold-Based Predictive Maintenance latest results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "No SEW Threshold-based Predictive Maintenance Results found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/predict/threshold-based-maintenance/results")
    public ResponseEntity<BaseResponse<SewThresholdBasedPredictiveMaintenanceOutputDto>> retrieveThresholdBasedMaintenanceLatestResults(
            @RequestParam String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        predictiveMaintenanceService.retrieveLatestThresholdBasedMaintenanceResults(moduleId),
                        "Threshold-Based Predictive Maintenance latest results retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Declare a Process Drift and Store in Maintenance Data in PKB
     *
     * @param processDriftData : Maintenance Data related to the Process Drift
     * @return Success or Error message
     */
    @Operation(summary = "Declare a Process Drift", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Process Drift has been successfully registered in the System"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again.")
    })
    @PostMapping("/process-drifts/create")
    public ResponseEntity<BaseResponse<String>> declareProcessDrift(@Valid @RequestBody MaintenanceDataDto processDriftData) {
        return new ResponseEntity<>(
                BaseResponse.success(predictiveMaintenanceService.declareProcessDrift(processDriftData), "Process Drift has been successfully registered in the System"),
                HttpStatus.CREATED);
    }

    /**
     * Retrieve a specific Process Drift / Maintenance Data
     *
     * @param processDriftId : Maintenance Data ID
     * @return MaintenanceDataDto
     */
    @Operation(summary = "Retrieve a Process Drift by its ID", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Process Drift retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    @GetMapping("/process-drifts/{processDriftId}")
    public ResponseEntity<BaseResponse<MaintenanceDataDto>> retrieveProcessDriftById(@NotBlank(message = "Process Drift ID can not be empty") @PathVariable String processDriftId) {
        return new ResponseEntity<>(
                BaseResponse.success(predictiveMaintenanceService.retrieveProcessDriftById(processDriftId), "Process Drift retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Mark a Process Drift as Completed
     *
     * @param processDriftId : Maintenance Data ID
     * @return Success or Error message
     */
    @Operation(summary = "Mark a Process Drift as Completed", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Process Drift completed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "404", description = "Invalid input datetime format")
    })
    @PutMapping("/process-drifts/{processDriftId}/complete")
    public ResponseEntity<BaseResponse<MaintenanceDataDto>> completeProcessDrift(@NotBlank(message = "Process Drift ID can not be empty") @PathVariable String processDriftId,
                                                                                 @Parameter(
                                                                                         description = "End datetime of the process drift in ISO format (e.g., 2025-08-03T14:30:00)",
                                                                                         example = "2025-08-03T14:30:00",
                                                                                         required = true
                                                                                 )@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDatetime){
        predictiveMaintenanceService.completeProcessDrift(processDriftId, endDatetime);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Process Drift completed successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all uncompleted process drifts (paginated)
     *
     * @param page : Page Size
     * @param size : Size of elements per page
     * @param sortAttribute : Sort Attribute of Process Drift
     * @param isAscending : Order of sorting
     * @return SewThresholdBasedPredictiveMaintenanceOutputDto
     */
    @Operation(summary = "Retrieve all uncompleted process drifts (paginated)", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uncompleted paginated process drifts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Invalid pagination sort attributes")
    })
    @GetMapping("/process-drifts")
    public ResponseEntity<BaseResponse<PaginatedResultsDto<MaintenanceDataDto>>> retrievePaginatedUncompletedProcessDrifts( @RequestParam(required = false, defaultValue = "0") int page,
                                                                                                             @RequestParam(required = false, defaultValue = "10") int size,
                                                                                                             @RequestParam(required = false, defaultValue = "tsInterventionStarted") String sortAttribute,
                                                                                                             @RequestParam(required = false, defaultValue = "false") boolean isAscending) {

        // Fix the pagination parameters
        Pageable pageable = createPaginationParameters(page, size, sortAttribute, isAscending);
        if (pageable == null)
            return new ResponseEntity<>(BaseResponse.error("Invalid pagination sort attributes"), HttpStatus.BAD_REQUEST);

        // Retrieve stored results in pages
        Page<MaintenanceDataDto> output = predictiveMaintenanceService.retrievePaginatedUncompletedProcessDrifts(pageable);

        // Fix the pagination class object
        PaginatedResultsDto<MaintenanceDataDto> results = new PaginatedResultsDto<>(
                output.getContent(),
                output.getTotalPages(),
                (int) output.getTotalElements(),
                output.isLast());

        return new ResponseEntity<>(
                BaseResponse.success(results, "Uncompleted paginated process drifts retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve maintenance Data optionally filtered by Start and Finish datetimes
     *
     * @param startDate : Initial Datetime filtering
     * @param endDate : End Datetime filtering
     * @return List of Maintenance Data
     */
    @Operation(summary = "Retrieve maintenance Data optionally filtered by Start and Finish datetimes", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Maintenance data within given timeframe retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/data")
    public ResponseEntity<BaseResponse<List<MaintenanceDataDto>>> retrieveMaintenanceDataByDateRange(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(startDate, endDate),
                        "Maintenance data within given timeframe retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Create pagination parameters
     *
     * @param page : Page of results
     * @param size : Results per page
     * @param sortAttribute : Sort attribute
     * @param isAscending : Sort order
     * @return pageable : Pagination Object
     */
    private Pageable createPaginationParameters(int page, int size, String sortAttribute, boolean isAscending){
        // Check if sort attribute is valid
        boolean isValidField = Arrays.stream(MaintenanceDataDto.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals(sortAttribute));

        // If not valid, return null
        if (!isValidField) {
            return null;
        }

        // Create pagination parameters
        return isAscending
                ? PageRequest.of(page, size, Sort.by(sortAttribute).ascending())
                : PageRequest.of(page, size, Sort.by(sortAttribute).descending());
    }
}

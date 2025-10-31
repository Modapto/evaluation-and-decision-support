package gr.atc.modapto.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.dto.crf.CrfSelfAwarenessParametersDto;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceInvocations.SewLocalAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessRealTimeMonitoringInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewFilteringOptionsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessRealTimeMonitoringResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.service.interfaces.ICrfSelfAwarenessService;
import gr.atc.modapto.service.interfaces.ISewSelfAwarenessService;
import gr.atc.modapto.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/eds/self-awareness")
@Validated
@Tag(name = "Self Awareness Controller", description = "API Controller for managing Self-Awareness")
public class SelfAwarenessController {

    private final ISewSelfAwarenessService sewSelfAwarenessService;

    private final ICrfSelfAwarenessService crfSelfAwarenessService;

    public SelfAwarenessController(ISewSelfAwarenessService sewSelfAwarenessService, ICrfSelfAwarenessService crfSelfAwarenessService){
        this.sewSelfAwarenessService = sewSelfAwarenessService;
        this.crfSelfAwarenessService = crfSelfAwarenessService;
    }

    /*
     *--------------------------------- SEW --------------------------------
     */
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
    @PostMapping("/pilots/sew/monitor-kpis/invoke")
    public ResponseEntity<BaseResponse<String>> invokeSelfAwarenessMonitoringKpisProcess(@Valid @RequestBody SewSelfAwarenessMonitoringKpisInputDto invocationData) {
        sewSelfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(invocationData);
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
    @GetMapping("/pilots/sew/monitor-kpis/results/latest")
    public ResponseEntity<BaseResponse<SewSelfAwarenessMonitoringKpisResultsDto>> retrieveLatestSelfAwarenessMonitoringKpisResults() {
        return new ResponseEntity<>(
                BaseResponse.success(
                        sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults(),
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
    @GetMapping("/pilots/sew/monitor-kpis/results/{moduleId}/latest")
    public ResponseEntity<BaseResponse<SewSelfAwarenessMonitoringKpisResultsDto>> retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(
            @PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId),
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
    @GetMapping("/pilots/sew/monitor-kpis/results")
    public ResponseEntity<BaseResponse<List<SewSelfAwarenessMonitoringKpisResultsDto>>> retrieveAllSelfAwarenessMonitoringKpisResults() {
        return new ResponseEntity<>(
                BaseResponse.success(
                        sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults(),
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
    @GetMapping("/pilots/sew/monitor-kpis/results/{moduleId}")
    public ResponseEntity<BaseResponse<List<SewSelfAwarenessMonitoringKpisResultsDto>>> retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(
            @PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId),
                        "All Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Invoke Self-Awareness Real-Time Monitoring of KPIs algorithm [SEW - SA2]
     *
     * @param invocationData : Input Data for algorithm
     * @return Message of success
     */
    @Operation(summary = "Invoke Self-Awareness Real-Time Monitoring of KPIs algorithm [SEW - SA2]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Self-Awareness Real-Time Monitoring algorithm invoked successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/sew/real-time-monitoring/invoke")
    public ResponseEntity<BaseResponse<String>> invokeSelfAwarenessRealTimeMonitoringProcess(@Valid @RequestBody SewSelfAwarenessRealTimeMonitoringInputDto invocationData) {
        sewSelfAwarenessService.invokeSelfAwarenessRealTimeMonitoringAlgorithm(invocationData);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Self-Awareness Real-Time Monitoring algorithm invoked successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all Self-Awareness Real-Time Monitoring Results
     *
     * @return List<SewSelfAwarenessRealTimeMonitoringResultsDto>
     */
    @Operation(summary = "Retrieve all Self-Awareness Real-Time Monitoring Results", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Self-Awareness Real-Time Monitoring results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/real-time-monitoring/results")
    public ResponseEntity<BaseResponse<List<SewSelfAwarenessRealTimeMonitoringResultsDto>>> retrieveAllSelfAwarenessRealTimeMonitoringResults() {
        return new ResponseEntity<>(
                BaseResponse.success(
                        sewSelfAwarenessService.retrieveAllSelfAwarenessRealTimeMonitoringResults(),
                        "All Self-Awareness Real-Time Monitoring results retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all Self-Awareness Real-Time Monitoring Results by Module ID
     *
     * @param moduleId : Module ID
     * @return List<SewSelfAwarenessRealTimeMonitoringResultsDto>
     */
    @Operation(summary = "Retrieve all Self-Awareness Real-Time Monitoring Results by Module ID", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Self-Awareness Real-Time Monitoring results for Module retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/real-time-monitoring/results/{moduleId}")
    public ResponseEntity<BaseResponse<List<SewSelfAwarenessRealTimeMonitoringResultsDto>>> retrieveAllSelfAwarenessRealTimeMonitoringResultsByModuleId(
            @PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(
                        sewSelfAwarenessService.retrieveAllSelfAwarenessRealTimeMonitoringResultsByModuleId(moduleId),
                        "All Self-Awareness Real-Time Monitoring results for Module " + moduleId + " retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Upload Components List of MODAPTO Module for Self-Awareness Operations
     *
     * @param componentsData : Input components data
     * @return Message of success
     */
    @Operation(summary = "Upload Components List of MODAPTO Module for Self-Awareness Operations", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Self-Awareness component list for module 'moduleId' has been successfully uploaded"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/sew/component-list/upload")
    public ResponseEntity<BaseResponse<String>> uploadSelfAwarenessComponentList(@Valid @RequestBody SewMonitorKpisComponentsDto componentsData) {
        sewSelfAwarenessService.uploadModuleComponentsList(componentsData);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Self-Awareness component list for module '" + componentsData.getModuleId() + "' has been successfully uploaded"),
                HttpStatus.CREATED);
    }

    /**
     * Retrieve Self-Awareness component list for a specific MODAPTO Module
     *
     * @param moduleId : Module ID
     * @return SewMonitorKpisComponentsDto
     */
    @Operation(summary = "Retrieve Self-Awareness component list for a specific MODAPTO Module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Self-Awareness component list for module retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Component list for module not found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/pilots/sew/modules/{moduleId}/component-list")
    public ResponseEntity<BaseResponse<SewMonitorKpisComponentsDto>> retrieveModuleComponentList(@PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId) {
        return new ResponseEntity<>(
                BaseResponse.success(sewSelfAwarenessService.retrieveSelfAwarenessComponentListByModuleId(moduleId),
                        "Self-Awareness component list for module '" + moduleId + "' retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Delete Self-Awareness component list for a specific MODAPTO Module
     *
     * @param moduleId : Module ID
     * @return Message of success
     */
    @Operation(summary = "Delete Self-Awareness component list for a specific MODAPTO Module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Self-Awareness component list for module deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Component list for module not found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @DeleteMapping("/pilots/sew/modules/{moduleId}/component-list")
    public ResponseEntity<BaseResponse<String>> deleteSelfAwarenessComponentListByModuleId(
            @PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId) {
        sewSelfAwarenessService.deleteSelfAwarenessComponentListByModuleId(moduleId);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Self-Awareness component list for module '" + moduleId + "' deleted successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve filtering options from Local Analytics Service [SEW]
     *
     * @param request : Smart Service ID and Modapto Module ID
     * @return SewFilteringOptionsDto
     */
    @Operation(summary = "Retrieve filtering options from Local Analytics Service [SEW]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtering options retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/sew/analytics/filtering-options")
    public ResponseEntity<BaseResponse<SewFilteringOptionsDto>> generateFilteringOptionsForLocalAnalytics(@RequestBody @Valid GlobalRequestDto request) {
        SewFilteringOptionsDto filteringOptions = sewSelfAwarenessService.retrieveFilteringOptionsForLocalAnalytics(request);
        return new ResponseEntity<>(
                BaseResponse.success(filteringOptions, "Filtering options retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Generate Histogram of Comparison of Modules from Local Analytics
     *
     * @param request : Filtering Options
     * @return Encoded image of Histogram
     */
    @Operation(summary = "Generate Histogram of Comparison of Modules from Local Analytics [SEW]", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histogram from Local Analytics generated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/sew/analytics/generate-histogram")
    public ResponseEntity<BaseResponse<String>> generateHistogramForComparingModules(@Valid @RequestBody GlobalRequestDto<SewLocalAnalyticsInputDto> request) {
        String encodedImage = sewSelfAwarenessService.generateHistogramForComparingModules(request);
        return new ResponseEntity<>(
                BaseResponse.success(encodedImage, "Histogram from Local Analytics generated successfully"),
                HttpStatus.OK);
    }

    /*
     *--------------------------------- CRF --------------------------------
     */
    /**
     * Invoke CRF Self-Awareness operation with Events data
     *
     * @param eventsFile : Events CSV Data
     * @param parameters : Input parameters
     * @return Message of success
     */
    @Operation(summary = "Invoke CRF Self-Awareness operation with Events data", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Self-Awareness algorithm for Kit-Holder has been successfully initialized"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "400", description = "File handling error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/crf/invoke")
    public ResponseEntity<BaseResponse<String>> invokeCrfKhSelfAwareness(@RequestPart("file") MultipartFile eventsFile, @Valid @RequestPart("parameters") CrfSelfAwarenessParametersDto parameters) throws IOException {
        crfSelfAwarenessService.invokeKhSelfAwareness(eventsFile, parameters);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Self-Awareness algorithm for Kit-Holder has been successfully initialized"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all KH Events from Self-Awareness analysis (paginated)
     *
     * @param page : Page Size
     * @param size : Size of elements per page
     * @param sortAttribute : Sort Attribute of Process Drift
     * @param isAscending : Order of sorting
     * @return SewThresholdBasedPredictiveMaintenanceOutputDto
     */
    @Operation(summary = "Retrieve all KH Events from Self-Awareness analysis (paginated)", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KH Events retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination sort attributes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Invalid pagination sort attributes")
    })
    @GetMapping("/pilots/crf/kh-events")
    public ResponseEntity<BaseResponse<PaginatedResultsDto<CrfKitHolderEventDto>>> retrievePaginatedKhEventResultsByModuleId(@RequestParam(required = false, defaultValue = "0") int page,
                                                                                                                                  @RequestParam(required = false, defaultValue = "10") int size,
                                                                                                                                  @RequestParam(required = false, defaultValue = "timestamp") String sortAttribute,
                                                                                                                                  @RequestParam(required = false, defaultValue = "false") boolean isAscending) {

        // Fix the pagination parameters
        Pageable pageable = PaginationUtils.createPaginationParameters(page, size, sortAttribute, isAscending, CrfKitHolderEventDto.class);
        if (pageable == null)
            return new ResponseEntity<>(BaseResponse.error("Invalid pagination sort attributes"), HttpStatus.BAD_REQUEST);

        // Retrieve stored results in pages
        Page<CrfKitHolderEventDto> output = crfSelfAwarenessService.retrievePaginatedKhEventResultsPaginated(pageable);

        return new ResponseEntity<>(
                BaseResponse.success(PaginationUtils.formulatePaginatedResults(output), "KH Events retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve KH Events by Module ID (paginated)
     *
     * @param moduleId : Module ID
     * @param page : Page Size
     * @param size : Size of elements per page
     * @param sortAttribute : Sort Attribute
     * @param isAscending : Order of sorting
     * @return PaginatedResultsDto<CrfKitHolderEventDto>
     */
    @Operation(summary = "Retrieve KH Events by Module ID (paginated)", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KH Events for module retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination sort attributes"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Invalid pagination sort attributes")
    })
    @GetMapping("/pilots/crf/kh-events/modules/{moduleId}")
    public ResponseEntity<BaseResponse<PaginatedResultsDto<CrfKitHolderEventDto>>> retrievePaginatedKhEventResultsByModule(
            @PathVariable @NotBlank(message = "Module ID cannot be empty") String moduleId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "timestamp") String sortAttribute,
            @RequestParam(required = false, defaultValue = "false") boolean isAscending) {

        Pageable pageable = PaginationUtils.createPaginationParameters(page, size, sortAttribute, isAscending, CrfKitHolderEventDto.class);
        if (pageable == null)
            return new ResponseEntity<>(BaseResponse.error("Invalid pagination sort attributes"), HttpStatus.BAD_REQUEST);

        Page<CrfKitHolderEventDto> output = crfSelfAwarenessService.retrievePaginatedKhEventResultsByModule(moduleId, pageable);

        return new ResponseEntity<>(
                BaseResponse.success(PaginationUtils.formulatePaginatedResults(output), "KH Events for module " + moduleId + " retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Registration of Kit Holder event by Workers
     *
     * @param event : Registration event
     * @return Message of success
     */
    @Operation(summary = "Registration of Kit Holder event by Workers", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Kit Holder event registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error | Throws if file is not proper or data are missing"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/pilots/crf/register-event")
    public ResponseEntity<BaseResponse<String>> registerKitHolderEventByCrfWorkers(@RequestBody @Valid CrfKitHolderEventDto event){
        crfSelfAwarenessService.registerKitHolderEvent(event);
        return new ResponseEntity<>(
                BaseResponse.success(null, "Kit Holder event registered successfully"),
                HttpStatus.CREATED);
    }
}

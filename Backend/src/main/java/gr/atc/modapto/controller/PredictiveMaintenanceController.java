package gr.atc.modapto.controller;

import gr.atc.modapto.dto.files.MaintenanceDataDto;
import gr.atc.modapto.service.interfaces.IPredictiveMaintenanceService;
import gr.atc.modapto.validation.ValidExcelFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}

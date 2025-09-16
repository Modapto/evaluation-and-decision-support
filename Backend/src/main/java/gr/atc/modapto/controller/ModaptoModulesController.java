package gr.atc.modapto.controller;

import gr.atc.modapto.dto.ModaptoModuleDto;
import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.dto.sew.DeclarationOfWorkDto;
import gr.atc.modapto.service.interfaces.IModaptoModuleService;
import gr.atc.modapto.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eds/modules")
@Tag(name = "MODAPTO Modules Controller", description = "API Controller for managing and monitoring MODAPTO modules")
public class ModaptoModulesController {

    private final IModaptoModuleService modaptoModuleService;

    public ModaptoModulesController(IModaptoModuleService modaptoModuleService) {
        this.modaptoModuleService = modaptoModuleService;
    }

    /**
     * Retrieve all MODAPTO modules with pagination
     *
     * @param page          Page number (default 0)
     * @param size          Page size (default 10)
     * @param sortBy        Sort field (default "timestamp")
     * @param sortDirection Sort direction (default "desc")
     * @return Paginated list of MODAPTO modules
     */
    @Operation(summary = "Retrieve all MODAPTO modules with pagination", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MODAPTO modules retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<PaginatedResultsDto<ModaptoModuleDto>>> retrieveAllModulesPaginated(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "timestamp_dt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ModaptoModuleDto> modulePage = modaptoModuleService.retrieveAllModulesPaginated(pageable);

        PaginatedResultsDto<ModaptoModuleDto> results = new PaginatedResultsDto<>(
                modulePage.getContent(),
                modulePage.getTotalPages(),
                (int) modulePage.getTotalElements(),
                modulePage.isLast());

        return new ResponseEntity<>(
                BaseResponse.success(results, "MODAPTO modules retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all MODAPTO modules without pagination
     *
     * @return List of all MODAPTO modules
     */
    @Operation(summary = "Retrieve all MODAPTO modules", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MODAPTO modules retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<ModaptoModuleDto>>> retrieveAllModules() {
        List<ModaptoModuleDto> modules = modaptoModuleService.retrieveAllModules();

        return new ResponseEntity<>(
                BaseResponse.success(modules, "MODAPTO modules retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve a specific MODAPTO module by moduleId
     *
     * @param moduleId The module identifier
     * @return MODAPTO module details
     */
    @Operation(summary = "Retrieve MODAPTO module by MODAPTO Module ID", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MODAPTO module retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Module not found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/{moduleId}")
    public ResponseEntity<BaseResponse<ModaptoModuleDto>> retrieveModuleById(
            @Parameter(description = "Module identifier") @PathVariable String moduleId) {

        ModaptoModuleDto module = modaptoModuleService.retrieveModuleByModuleId(moduleId);

        return new ResponseEntity<>(
                BaseResponse.success(module, "MODAPTO module retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve smart services for a specific MODAPTO module
     *
     * @param moduleId The module identifier
     * @return List of smart services for the module
     */
    @Operation(summary = "Retrieve smart services for a MODAPTO module", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Smart services retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Module not found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/{moduleId}/smart-services")
    public ResponseEntity<BaseResponse<List<ModaptoModuleDto.SmartServiceDto>>> retrieveSmartServicesByModuleId(
            @Parameter(description = "Module identifier") @PathVariable String moduleId) {

        List<ModaptoModuleDto.SmartServiceDto> smartServices = modaptoModuleService.retrieveSmartServicesByModuleId(moduleId);

        return new ResponseEntity<>(
                BaseResponse.success(smartServices, "Smart services retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Retrieve all MODAPTO modules a worker is working on with pagination
     *
     * @param page          Page number (default 0)
     * @param size          Page size (default 10)
     * @param sortBy        Sort field (default "timestamp")
     * @param sortDirection Sort direction (default "desc")
     * @return Paginated list of MODAPTO modules a worker is working
     */
    @Operation(summary = "Retrieve all MODAPTO modules a worker is working on with pagination", security = @SecurityRequirement(name = "bearerToken"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MODAPTO modules retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @GetMapping("/working-modules/users/{workerName}")
    public ResponseEntity<BaseResponse<PaginatedResultsDto<ModaptoModuleDto>>> retrieveModulesByWorkerPaginated(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "timestamp_dt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection,
            @PathVariable @NotBlank(message = "Worker name can not be blank") String workerName) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ModaptoModuleDto> modulePage = modaptoModuleService.retrieveModulesByWorkerPaginated(workerName, pageable);

        PaginatedResultsDto<ModaptoModuleDto> results = new PaginatedResultsDto<>(
                modulePage.getContent(),
                modulePage.getTotalPages(),
                (int) modulePage.getTotalElements(),
                modulePage.isLast());

        return new ResponseEntity<>(
                BaseResponse.success(results, "MODAPTO modules the authenticated worker is working on retrieved successfully"),
                HttpStatus.OK);
    }

    /**
     * Declare that one or multiple workers are working on a specific MODAPTO module
     *
     * @param workData Declaration of Work input data
     * @return Updated MODAPTO module
     */
    @Operation(
            summary = "Declare that one or multiple workers are working on a specific MODAPTO module",
            security = @SecurityRequirement(name = "bearerToken")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workers successfully submitted working on MODAPTO Module"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Module not found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/declare-work")
    public ResponseEntity<BaseResponse<ModaptoModuleDto>> declareWorkOnModule(
           @RequestBody DeclarationOfWorkDto workData) {

        ModaptoModuleDto updatedModule =
                modaptoModuleService.declareWorkOnModule(workData);

        return new ResponseEntity<>(
                BaseResponse.success(updatedModule, "Workers successfully submitted working on MODAPTO Module"),
                HttpStatus.OK
        );
    }


    /**
     * Undeclare that the authenticated worker is working on a specific MODAPTO module.
     *
     * @param workerName Worker Name
     * @return Updated MODAPTO module
     */
    @Operation(
            summary = "Undeclare the authenticated worker is working on a specific MODAPTO module",
            security = @SecurityRequirement(name = "bearerToken")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Worker successfully undeclared on module"),
            @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
            @ApiResponse(responseCode = "404", description = "Module not found"),
            @ApiResponse(responseCode = "500", description = "Internal mapping exception")
    })
    @PostMapping("/{moduleId}/undeclare-work/users/{workerName}")
    public ResponseEntity<BaseResponse<ModaptoModuleDto>> undeclareWorkOnModule(
            @Parameter(description = "Module identifier") @PathVariable String moduleId,
            @Parameter(description = "Worker Full Name") @PathVariable String workerName) {

        ModaptoModuleDto updatedModule = modaptoModuleService.undeclareWorkOnModule(moduleId, workerName);

        return new ResponseEntity<>(
                BaseResponse.success(updatedModule, "Worker successfully undeclared on module"),
                HttpStatus.OK
        );
    }
}

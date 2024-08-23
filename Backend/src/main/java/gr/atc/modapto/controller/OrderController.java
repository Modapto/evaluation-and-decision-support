package gr.atc.modapto.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.atc.modapto.dto.OrderDto;
import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.enums.PilotCode;
import gr.atc.modapto.exception.CustomExceptions.PaginationException;
import gr.atc.modapto.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/eds")
public class OrderController {

    private final OrderService orderService;

    private static final String PILOT_ERROR = "Invalid pilot code! Only CRF, ILTAR, FFT and SEW are acceptable!";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Create a new Order
     * 
     * @param order
     * @return message of success
     */
    @PostMapping("createOrder")
    public ResponseEntity<ApiResponseInfo<String>> createNewOrder(@RequestBody OrderDto order) {
        if (orderService.saveNewOrder(order))
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseInfo.success("Order created successfully!"));
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseInfo.error("Unable to create new order in DB!"));
    }

    @PostMapping("createOrders")
    public ResponseEntity<ApiResponseInfo<String>> createMultipleOrders(@RequestBody List<OrderDto> orders) {
        if (orderService.saveListOfOrders(orders))
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseInfo.success("Orders created successfully!"));
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseInfo.error("Unable to create new orders in DB!"));
    }

    /**
     * Retrieve an order by ID
     * 
     * @param id
     * @return OrderDto
     */
    @Operation(summary = "Retrieve specific order per pilot code", description = "Valid pilot code must be given!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class)) }),
            @ApiResponse(responseCode = "400", description = PILOT_ERROR)
    })
    @GetMapping("pilot/{customer}/orders/{id}")
    public ResponseEntity<ApiResponseInfo<OrderDto>> retrieveOrderById(@PathVariable String customer,
            @PathVariable String id) {
        if (!EnumUtils.isValidEnum(PilotCode.class, customer.toUpperCase()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseInfo.error(null, PILOT_ERROR));
        OrderDto retrievedOrder = orderService.retrieveOrderById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseInfo.success(retrievedOrder, "Order retrieved successfully"));
    }

    /**
     * Retrive pagineted orders per Pilot Code and optionally filtering by dates
     * 
     * @param pilotCode
     * @param startDate -> Optional
     * @param endDate -> Optional
     * @param pageableElem
     * @return PaginatedResultsDto<OrderDto>>
     */
    @Operation(summary = "Retrieve orders per pilot with pagination and optionally filtering by dates", description = "Valid pagination parameters and pilot code must be given!")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResultsDto.class)) }),
            @ApiResponse(responseCode = "400", description = PILOT_ERROR),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters were given"),
            @ApiResponse(responseCode = "500", description = "Unable to retrieve orders from DB!")
    })
    @GetMapping("/pilot/{customer}/orders")
    public ResponseEntity<ApiResponseInfo<PaginatedResultsDto<OrderDto>>> retrievePaginatedOrders(
            @PathVariable String customer, Pageable pageableElem,
            @RequestParam(required = false) Optional<String> startDate,
            @RequestParam(required = false) Optional<String> endDate) {
        if (!EnumUtils.isValidEnum(PilotCode.class, customer.toUpperCase()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseInfo.error(null, PILOT_ERROR));

        dateFormat.setLenient(false);

        try {
            // Implement Date validation
            if (startDate.isPresent() && endDate.isPresent()){
                // Parse dates
                Date start = dateFormat.parse(startDate.get());
                Date end = dateFormat.parse(endDate.get());
                
                if (start.after(end)) 
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseInfo.error(null, "Start date must be before or equal to end date!"));
            }
        } catch (ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseInfo.error(null, "Invalid date format!"));
        }

        // Retrieve stored results in pages
        try {
            Page<OrderDto> page;

            // Retrieve data based on the inserted filters
            page = orderService.retrieveOrdersByCustomerFilteredByDates(customer, pageableElem,
                startDate.isPresent() ? dateFormat.parse(startDate.get()) : null, 
                endDate.isPresent() ? dateFormat.parse(endDate.get()) : null);

            // Fix the pagination class object
            PaginatedResultsDto<OrderDto> results = new PaginatedResultsDto<>(page.getContent(), page.getTotalPages(),
                    (int) page.getTotalElements(), page.isLast());

            return ResponseEntity.status(HttpStatus.OK).cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                    .body(ApiResponseInfo.success(results, "Orders retrieved successfully"));

        } catch (PaginationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseInfo.error(null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseInfo.error(null, "Unable to retrieve orders from DB!"));
        }
    }

}

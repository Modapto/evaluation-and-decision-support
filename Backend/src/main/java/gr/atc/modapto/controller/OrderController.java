package gr.atc.modapto.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import gr.atc.modapto.util.JwtUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.atc.modapto.dto.OrderDto;
import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.exception.CustomExceptions.PaginationException;
import gr.atc.modapto.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/eds/orders")
@Tag(name = "Orders Controller", description = "API Controller for managing Orders")
public class OrderController {

        private final OrderService orderService;

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /**
         * Create a new Order
         * 
         * @param order : Order Information
         * @return message of success
         */
        @Operation(summary = "Create a new Order", security = @SecurityRequirement(name = "bearerToken"))
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Order created successfully"),
                @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
                @ApiResponse(responseCode = "500", description = "Unable to create new order in DB!")
        })
        @PostMapping("/createOrder")
        public ResponseEntity<BaseResponse<String>> createNewOrder(@RequestBody OrderDto order) {
                if (orderService.saveNewOrder(order))
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(BaseResponse.success(null,"Order created successfully"));
                else
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(BaseResponse.error("Unable to create new order in DB"));
        }

        /**
         * Create multiple orders
         *
         * @param orders : List of orders Information
         * @return message of success
         */
        @Operation(summary = "Create a batch of Orders", security = @SecurityRequirement(name = "bearerToken"))
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Orders created successfully"),
                @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
                @ApiResponse(responseCode = "500", description = "Unable to create new orders in DB")
        })
        @PostMapping("/createOrders")
        public ResponseEntity<BaseResponse<String>> createMultipleOrders(@RequestBody List<OrderDto> orders) {
                if (orderService.saveListOfOrders(orders))
                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(BaseResponse.success(null,"Orders created successfully"));
                else
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(BaseResponse.error("Unable to create new orders in DB"));
        }

        /**
         * Retrieve an order by ID
         * 
         * @param id : ID of Order
         * @return OrderDto
         */
        @Operation(summary = "Retrieve specific order per pilot code", description = "Valid pilot code must be given", security = @SecurityRequirement(name = "bearerToken"))
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Order retrieved successfully", content = {
                        @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class)) }),
                @ApiResponse(responseCode = "400", description = "Invalid Pilot Code"),
                @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
                @ApiResponse(responseCode = "403", description = "You can only retrieve information within your organization"),
        })
        @GetMapping("/{id}/pilot/{pilotCode}")
        public ResponseEntity<BaseResponse<OrderDto>> retrieveOrderById(
                        @AuthenticationPrincipal Jwt jwt,
                        @PathVariable String pilotCode,
                        @PathVariable String id) {

                // Check Pilot Code
                String pilot = JwtUtils.extractPilotCode(jwt);
                if (!pilot.equalsIgnoreCase(pilotCode) && !pilot.equalsIgnoreCase("ALL"))
                        return new ResponseEntity<>(BaseResponse.error("You can only retrieve information within your organization", "Invalid Pilot Code"), HttpStatus.BAD_REQUEST);

                // Retrieve Order
                OrderDto retrievedOrder = orderService.retrieveOrderById(id);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(BaseResponse.success(retrievedOrder, "Order retrieved successfully"));
        }

        /**
         * Retrieve paginated orders per Pilot Code and optionally filtering by dates
         * 
         * @param pilotCode : Pilot Code
         * @param startDate : Start Date for Searching -> Optional
         * @param endDate   : End Date for Searching -> Optional
         * @param pageableElem  : Pagination
         * @return PaginatedResultsDto<OrderDto>>
         */
        @Operation(summary = "Retrieve orders per pilot with pagination and optionally filtering by dates", description = "Valid pagination parameters and pilot code must be given!", security = @SecurityRequirement(name = "bearerToken"))
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = {
                        @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResultsDto.class)) }),
                @ApiResponse(responseCode = "200", description = "No orders found for the given search parameters"),
                @ApiResponse(responseCode = "400", description = "Invalid Pilot Code"),
                @ApiResponse(responseCode = "400", description = "Invalid pagination parameters were given"),
                @ApiResponse(responseCode = "400", description = "Invalid dates provided"),
                @ApiResponse(responseCode = "400", description = "Unable to parse given dates"),
                @ApiResponse(responseCode = "401", description = "Unauthorized request. Check token and try again."),
                @ApiResponse(responseCode = "403", description = "You can only retrieve information within your organization"),
                @ApiResponse(responseCode = "500", description = "Unable to retrieve orders from DB!")
        })
        @GetMapping("/pilot/{pilotCode}")
        public ResponseEntity<BaseResponse<PaginatedResultsDto<OrderDto>>> retrievePaginatedOrders(@AuthenticationPrincipal Jwt jwt,
                        @PathVariable String pilotCode, Pageable pageableElem,
                        @RequestParam(name = "startDate", required = false) String startDate,
                        @RequestParam(name = "endDate", required = false) String endDate) {

                // Check Pilot Code Origin
                String pilot = JwtUtils.extractPilotCode(jwt);
                if (!pilot.equalsIgnoreCase(pilotCode) && !pilot.equalsIgnoreCase("ALL"))
                        return new ResponseEntity<>(BaseResponse.error("You can only retrieve information within your organization", "Invalid Pilot Code"), HttpStatus.FORBIDDEN);

                try {
                        // Implement Date validation
                        if (startDate != null && endDate != null) {
                                // Parse dates
                                Date start = dateFormat.parse(startDate);
                                Date end = dateFormat.parse(endDate);

                                // Check if dates are valid
                                if (start.after(end))
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                        .body(BaseResponse.error("Invalid dates provided",
                                                                        "Start date must be before or equal to end date!"));
                        }


                        // Retrieve stored results in pages
                        Page<OrderDto> page;

                        // Retrieve data based on the inserted filters
                        page = orderService.retrieveOrdersByCustomerFilteredByDates(pilotCode, pageableElem,
                                        startDate, endDate);

                        if (page == null || page.isEmpty()) {
                                return ResponseEntity.status(HttpStatus.OK)
                                                .body(BaseResponse.success(
                                                                new PaginatedResultsDto<>(Collections.emptyList(),
                                                                                0, 0, true),
                                                                "No orders found for the given search parameters"));
                        }

                        // Fix the pagination class object
                        PaginatedResultsDto<OrderDto> results = new PaginatedResultsDto<>(page.getContent(),
                                        page.getTotalPages(),
                                        (int) page.getTotalElements(), page.isLast());

                        return ResponseEntity.status(HttpStatus.OK)
                                        .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                                        .body(BaseResponse.success(results, "Orders retrieved successfully"));

                } catch (PaginationException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(BaseResponse.error("Invalid pagination parameters were given", e.getMessage()));
                } catch (ParseException | DateTimeParseException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(BaseResponse.error("Unable to parse given dates", "Invalid date format!"));
                }
        }

}

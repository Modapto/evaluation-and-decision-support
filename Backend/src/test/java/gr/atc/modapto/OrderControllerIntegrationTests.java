package gr.atc.modapto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import gr.atc.modapto.controller.ApiResponseInfo;
import gr.atc.modapto.dto.OrderDto;
import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.enums.PilotCode;
import gr.atc.modapto.service.OrderService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class OrderControllerIntegrationTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private OrderService orderService;

    @LocalServerPort
    private Integer port;

    /*
     * Creation of new Orders
     */
    @DisplayName("Create a new Order: Success")
    @Test
    void createNewOrder_Success() {
        Mockito.when(orderService.saveNewOrder(any(OrderDto.class))).thenReturn(true);

        // Set request Body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        OrderDto request = OrderDto.builder()
                .customer(PilotCode.CRF)
                .documentNumber("Test")
                .comments("Test Object")
                .build();

        HttpEntity<OrderDto> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ApiResponseInfo<String>> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/eds/createOrder",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponseInfo<String>>() {
                });

        // Assert Response
        assertNotNull(responseEntity, "The response entity should not be null");
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        // Assert Message
        assertNotNull(responseEntity.getBody(), "The response body should not be null");
        assertTrue(responseEntity.getBody().getSuccess(), "Success flag should be true");
        assertEquals("Order created successfully!", responseEntity.getBody().getData());
    }

    @DisplayName("Create a new Order: Error on Server")
    @Test
    void createNewOrder_ErrorOnServer() {
        Mockito.when(orderService.saveNewOrder(any(OrderDto.class))).thenReturn(false);

        // Set request Body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        OrderDto request = OrderDto.builder()
                .customer(PilotCode.CRF)
                .documentNumber("Test")
                .comments("Test Object")
                .build();

        HttpEntity<OrderDto> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ApiResponseInfo<String>> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/eds/createOrder",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponseInfo<String>>() {
                });

        // Assert Response
        assertNotNull(responseEntity, "The response entity should not be null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        // Assert Message
        assertNotNull(responseEntity.getBody(), "The response body should not be null");
        assertFalse(responseEntity.getBody().getSuccess(), "Success flag should be true");

    }

    @DisplayName("Create multiple Orders: Success")
    @Test
    void createMultipleOrders_Success() {
        Mockito.when(orderService.saveListOfOrders(anyList())).thenReturn(true);

        // Set request Body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        OrderDto request = OrderDto.builder()
                .customer(PilotCode.CRF)
                .documentNumber("Test")
                .comments("Test Object")
                .build();
        List<OrderDto> requestList = List.of(request);

        HttpEntity<List<OrderDto>> requestEntity = new HttpEntity<>(requestList, headers);

        ResponseEntity<ApiResponseInfo<String>> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/eds/createOrders",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponseInfo<String>>() {
                });

        // Assert Response
        assertNotNull(responseEntity, "The response entity should not be null");
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        // Assert Message
        assertNotNull(responseEntity.getBody(), "The response body should not be null");
        assertTrue(responseEntity.getBody().getSuccess(), "Success flag should be true");
        assertEquals("Orders created successfully!", responseEntity.getBody().getData());
    }

    /*
     * Retrieval of Orders
     */
    @DisplayName("Retrieve an order By ID: Success")
    @Test
    void retrieveOrderById_Success() {
        OrderDto mockOrderDto = OrderDto.builder()
                .customer(PilotCode.CRF)
                .documentNumber("Test")
                .comments("Test Object")
                .build();

        Mockito.when(orderService.retrieveOrderById(anyString())).thenReturn(mockOrderDto);

        ResponseEntity<ApiResponseInfo<OrderDto>> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/eds/pilot/CRF/orders/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseInfo<OrderDto>>() {
                });

        // Assert Response
        assertNotNull(responseEntity, "The response entity should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Assert Message
        assertNotNull(responseEntity.getBody(), "The response body should not be null");
        assertTrue(responseEntity.getBody().getSuccess(), "Success flag should be true");
        assertEquals(mockOrderDto.getDocumentNumber(), responseEntity.getBody().getData().getDocumentNumber());
    }

    @DisplayName("Retrieve an order By ID: Invalid Pilot Code")
    @Test
    void retrieveOrderById_InvalidPilotCode() {
        ResponseEntity<ApiResponseInfo<OrderDto>> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/eds/pilot/INVALID/orders/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseInfo<OrderDto>>() {
                });

        // Assert Response
        assertNotNull(responseEntity, "The response entity should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // Assert Message
        assertNotNull(responseEntity.getBody(), "The response body should not be null");
        assertFalse(responseEntity.getBody().getSuccess(), "Success flag should be true");
        assertEquals("Invalid pilot code! Only CRF, ILTAR, FFT and SEW are acceptable!",
                responseEntity.getBody().getErrors());
    }

    @DisplayName("Retrieve paginated orders with Filters: Success")
    @Test
    void retrievePaginatedOrders_Success() {
        @SuppressWarnings("unchecked")
        Page<OrderDto> mockPage = Mockito.mock(Page.class);
        Mockito.when(orderService.retrieveOrdersByCustomerFilteredByDates(anyString(), any(), any(), any()))
                .thenReturn(mockPage);

        // Request Parameters
        String pageParam = "0";
        String sizeParam = "10";
        String startDateParam = "2024-01-01";
        String endDateParam = "2024-02-01";

        // URL
        String url = "http://localhost:" + port + "/api/eds/pilot/CRF/orders" +
                "?page=" + pageParam +
                "&size=" + sizeParam +
                "&startDate=" + startDateParam +
                "&endDate=" + endDateParam;

        ResponseEntity<ApiResponseInfo<PaginatedResultsDto<OrderDto>>> responseEntity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseInfo<PaginatedResultsDto<OrderDto>>>() {
                });

        // Assert Response
        assertNotNull(responseEntity, "The response entity should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // Assert Message
        assertNotNull(responseEntity.getBody(), "The response body should not be null!");
        assertTrue(responseEntity.getBody().getSuccess(), "Success flag should be true!");
        assertNotNull(responseEntity.getBody().getData(), "Data should not be null!");
    }

    @DisplayName("Retrieve paginated orders with Filters: Invalid Dates")
    @Test
    void retrievePaginatedOrders_InvalidDates() {
        // Request Parameters
        String pageParam = "0";
        String sizeParam = "10";
        String startDateParam = "2024-02-01";
        String endDateParam = "2024-01-01";

        // URL
        String url = "http://localhost:" + port + "/api/eds/pilot/CRF/orders" +
                "?page=" + pageParam +
                "&size=" + sizeParam +
                "&startDate=" + startDateParam +
                "&endDate=" + endDateParam;

        ResponseEntity<ApiResponseInfo<PaginatedResultsDto<OrderDto>>> responseEntity = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseInfo<PaginatedResultsDto<OrderDto>>>() {
                });

        // Assert Response
        assertNotNull(responseEntity, "The response entity should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // Assert Message
        assertNotNull(responseEntity.getBody(), "The response body should not be null!");
        assertFalse(responseEntity.getBody().getSuccess(), "Success flag should be true!");
        assertEquals("Start date must be before or equal to end date!", responseEntity.getBody().getErrors());
    }
}

package gr.atc.modapto.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.util.JwtUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;

import gr.atc.modapto.dto.OrderDto;
import gr.atc.modapto.service.OrderService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = OrderController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration.class
    })
@ActiveProfiles("test")
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private static Jwt jwt;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ElasticsearchOperations elasticsearchTemplate() {
            return mock(ElasticsearchOperations.class);
        }

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @BeforeAll
    @SuppressWarnings("unused")
    static void setup() {
        String tokenValue = "mock.jwt.token";
        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", Map.of("roles", List.of("SUPER_ADMIN")));
        claims.put("resource_access", Map.of("modapto", Map.of("roles", List.of("SUPER_ADMIN"))));
        claims.put("sid", "user");
        claims.put("pilot_code", List.of("CRF"));
        claims.put("user_role", List.of("TEST"));
        claims.put("pilot_role", List.of("SUPER_ADMIN"));

        jwt = Jwt.withTokenValue(tokenValue).headers(header -> header.put("alg", "HS256")).claims(claim -> claim.putAll(claims)).build();
    }

    /*
     * Creation of new Orders
     */
    @DisplayName("Create a new Order: Success")
    @WithMockUser(roles = "USER")
    @Test
    void givenValidJwt_whenCreateNewOrder_thenSuccess() throws Exception {
        //Given
        OrderDto request = OrderDto.builder().customer("CRF").documentNumber("Test").comments("Test Object").build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

        when(orderService.saveNewOrder(any(OrderDto.class))).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/eds/orders/createOrder").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isCreated()).andExpect(jsonPath("$.success", is(true))).andExpect(jsonPath("$.message", is("Order created successfully")));

        verify(orderService, times(1)).saveNewOrder(any(OrderDto.class));
    }

    @DisplayName("Create a new Order: Error on Server")
    @WithMockUser(roles = "USER")
    @Test
    void givenValidJwt_whenCreateNewOrder_thenErrorOnServer() throws Exception {
        OrderDto request = OrderDto.builder().customer("CRF").documentNumber("Test").comments("Test Object").build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

        when(orderService.saveNewOrder(any(OrderDto.class))).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/api/eds/orders/createOrder").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.success", is(false))).andExpect(jsonPath("$.message", is("Unable to create new order in DB")));

        verify(orderService, times(1)).saveNewOrder(any(OrderDto.class));
    }

    @DisplayName("Create multiple Orders: Success")
    @WithMockUser(roles = "USER")
    @Test
    void givenValidJwt_whenCreateMultipleOrders_thenSuccess() throws Exception {
        OrderDto request = OrderDto.builder().customer("CRF").documentNumber("Test").comments("Test Object").build();
        List<OrderDto> requestList = List.of(request);

        when(orderService.saveListOfOrders(anyList())).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/eds/orders/createOrders").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)).content(objectMapper.writeValueAsString(requestList)).contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isCreated()).andExpect(jsonPath("$.success", is(true))).andExpect(jsonPath("$.message", is("Orders created successfully")));

        verify(orderService, times(1)).saveListOfOrders(anyList());
    }

    @DisplayName("Retrieve an order By ID: Success")
    @WithMockUser(roles = "USER")
    @Test
    void givenValidJwt_whenRetrieveOrderById_thenSuccess() throws Exception {
        OrderDto mockOrderDto = OrderDto.builder().customer("CRF").documentNumber("Test").comments("Test Object").build();

        when(orderService.retrieveOrderById(anyString())).thenReturn(mockOrderDto);

        ResultActions response = mockMvc.perform(get("/api/eds/orders/1/pilot/CRF").with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)).contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk()).andExpect(jsonPath("$.success", is(true))).andExpect(jsonPath("$.data.documentNumber", is(mockOrderDto.getDocumentNumber())));

        verify(orderService, times(1)).retrieveOrderById(anyString());
    }

    @DisplayName("Retrieve paginated orders: Success")
    @WithMockUser(roles = "USER")
    @Test
    void givenPaginationAndJwtToken_whenRetrievePaginatedOrders_thenSuccess() throws Exception {
        try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
            String pilotCode = "TEST_PILOT";
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderDto> orderPage = new PageImpl<>(List.of(new OrderDto()), pageable, 1);

            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

            when(JwtUtils.extractPilotCode(any(Jwt.class))).thenReturn(pilotCode);
            when(orderService.retrieveOrdersByCustomerFilteredByDates(anyString(), any(), any(), any())).thenReturn(orderPage);

            mockMvc.perform(get("/api/eds/orders/pilot/{pilotCode}", pilotCode)
                            .param("startDate", "2025-01-01").param("endDate", "2025-01-31")
                            .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Orders retrieved successfully"));

        }
    }

    @DisplayName("Retrieve paginated orders: Invalid Pilot Code request/Forbidden")
    @WithMockUser(roles = "USER")
    @Test
    void givenPaginationAndJwtToken_whenRetrievePaginatedOrders_thenForbidden() throws Exception {
        try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
            mockedJwtUtils.when(() -> JwtUtils.extractPilotCode(any(Jwt.class))).thenReturn("DIFFERENT_PILOT");

            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

            // Perform request and validate response
            mockMvc.perform(get("/api/eds/orders/pilot/{pilotCode}", "TEST_PILOT")
                            .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("You can only retrieve information within your organization"));
        }
    }


    @DisplayName("Retrieve paginated orders: Invalid Date Format")
    @WithMockUser(roles = "USER")
    @Test
    void givenPaginationAndJwtToken_whenRetrievePaginatedOrders_thenInvalidDataFormat() throws Exception {
        try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
            mockedJwtUtils.when(() -> JwtUtils.extractPilotCode(any(Jwt.class))).thenReturn("TEST_PILOT");

            mockMvc.perform(get("/api/eds/orders/pilot/{pilotCode}", "TEST_PILOT")
                    .param("startDate", "invalid-date")
                    .param("endDate", "2025-01-31")
                    .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Unable to parse given dates"));
        }
    }

    @DisplayName("Retrieve paginated orders: No Orders Found")
    @WithMockUser(roles = "USER")
    @Test
    void givenPaginationAndJwtToken_whenRetrievePaginatedOrders_noOrdersFound() throws Exception {
        try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
            String pilotCode = "TEST_PILOT";
            Pageable pageable = PageRequest.of(0, 10);

            mockedJwtUtils.when(() -> JwtUtils.extractPilotCode(any(Jwt.class))).thenReturn(pilotCode);
            when(orderService.retrieveOrdersByCustomerFilteredByDates(eq(pilotCode), any(), any(), any()))
                    .thenReturn(Page.empty(pageable));

            mockMvc.perform(get("/api/eds/orders/pilot/{pilotCode}", pilotCode)
                            .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("No orders found for the given search parameters"));
        }
    }
}

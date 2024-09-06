package gr.atc.modapto.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;

import gr.atc.modapto.dto.OrderDto;
import gr.atc.modapto.enums.PilotCode;
import gr.atc.modapto.exception.CustomExceptions.OrderNotFoundException;
import gr.atc.modapto.model.Assembly;
import gr.atc.modapto.model.Component;
import gr.atc.modapto.model.Order;
import gr.atc.modapto.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private OrderService orderService;

    private static final String TEST_PILOT = "CRF";
    
    private Order order;

    private OrderDto orderDto;

    @BeforeEach
    void setup(){
        orderService = new OrderService(orderRepository, modelMapper, elasticsearchOperations);

        orderDto = OrderDto.builder().documentNumber("testNumber").build();

        Assembly assembly = Assembly.builder()
            .partNumber("PN1")
            .type("assembly")
            .quantity(1)
            .expectedDeliveryDate("2024-01-01")
            .build();
        
        Component component = Component.builder()
            .partNumber("PN2")
            .type("component")
            .quantity(1)
            .expectedDeliveryDate("2024-01-01")
            .build();

        order = Order.builder()
            .assemblies(Collections.singletonList(assembly))
            .components(Collections.singletonList(component))
            .customer(PilotCode.valueOf(TEST_PILOT))
            .documentNumber("123456")
            .comments("Test comments")
            .build();
    }

    @DisplayName("Save new order: Success")
    @Test
    void givenValidOrderDto_whenSaveNewOrder_thenReturnTrue() {
        // Given
        BDDMockito.given(modelMapper.map(orderDto, Order.class)).willReturn(order);
        BDDMockito.given(orderRepository.save(order)).willReturn(order);

        // When
        boolean isSuccess = orderService.saveNewOrder(orderDto);

        // Then
        Assertions.assertThat(isSuccess).isTrue();
    }

    @DisplayName("Save new order: Error")
    @Test
    void givenOrderRepositorySaveFailure_whenSaveNewOrder_thenReturnFalse() {
        // Given
        BDDMockito.given(modelMapper.map(orderDto, Order.class)).willReturn(order);
        BDDMockito.given(orderRepository.save(order)).willReturn(null);

        // When
        boolean isSuccess = orderService.saveNewOrder(orderDto);

        // Then
        Assertions.assertThat(isSuccess).isFalse();
    }

    @DisplayName("Retrieve an order by ID: Success")
    @Test
    void givenValidOrderId_whenRetrieveOrderById_thenReturnOrderDto() throws OrderNotFoundException {
        // Given
        String orderId = "123";
        BDDMockito.given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        BDDMockito.given(modelMapper.map(order, OrderDto.class)).willReturn(orderDto);

        // When
        OrderDto foundOrderDto = orderService.retrieveOrderById(orderId);

        // Then
        Assertions.assertThat(foundOrderDto).isNotNull().isEqualTo(orderDto);
    }

    @DisplayName("Retrieve an order by ID: Error - Not Found")
    @Test
    void givenInvalidOrderId_whenRetrieveOrderById_thenThrowOrderNotFoundException() {
        // Given
        String orderId = "invalidId";
        BDDMockito.given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        // When / Then
        Assertions.assertThatThrownBy(() -> orderService.retrieveOrderById(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(orderId);
    }

    @DisplayName("Retrieve an orders by Customer: Success")
    @Test
    void givenValidCustomerAndDateRange_whenRetrieveOrdersByCustomerFilteredByDates_thenReturnOrderDtoPage() {
        // Given
        String customer = "CRF";
        LocalDate localStartDate = LocalDate.of(2024, 1, 1);
        Date startDate = Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate localEndDate = LocalDate.of(2024, 1, 2);
        Date endDate = Date.from(localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Pageable pageable = PageRequest.of(0, 10);

        // Mock SearchHits and SearchHit
        SearchHits<Order> searchHits = mock(SearchHits.class);
        SearchHit<Order> searchHit = mock(SearchHit.class);

        // Mock search hits
        BDDMockito.given(searchHit.getContent()).willReturn(order);
        BDDMockito.given(searchHits.getSearchHits()).willReturn(Collections.singletonList(searchHit));
        BDDMockito.given(searchHits.getTotalHits()).willReturn(1L);

        // Mock elasticsearchOperations
        BDDMockito.given(elasticsearchOperations.search(any(CriteriaQuery.class), eq(Order.class))).willReturn(searchHits);

        BDDMockito. given(modelMapper.map(order, OrderDto.class)).willReturn(orderDto);

        // When
        Page<OrderDto> resultPage = orderService.retrieveOrdersByCustomerFilteredByDates(customer, pageable, startDate, endDate);

        // Then
        Assertions.assertThat(resultPage.getContent()).isEqualTo(Collections.singletonList(orderDto));
        Assertions.assertThat(resultPage.getTotalElements()).isEqualTo(1L);
    }

}

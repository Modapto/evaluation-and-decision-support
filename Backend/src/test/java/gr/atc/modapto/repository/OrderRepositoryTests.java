package gr.atc.modapto.repository;

import java.time.LocalDate;
import java.util.Collections;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import gr.atc.modapto.model.Assembly;
import gr.atc.modapto.model.Component;
import gr.atc.modapto.model.Order;
import org.springframework.test.context.ActiveProfiles;

@DataElasticsearchTest
@ActiveProfiles("test")
class OrderRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private OrderRepository orderRepository;

    private static final String TEST_PILOT = "CRF";
    
    private Order order;

    @BeforeEach
    void setup(){
        Assembly assembly = Assembly.builder()
            .partNumber("PN1")
            .type("assembly")
            .quantity(1)
            .expectedDeliveryDate(LocalDate.of(2024, 01, 01))
            .build();
        
        Component component = Component.builder()
            .partNumber("PN2")
            .type("component")
            .quantity(1)
            .expectedDeliveryDate(LocalDate.of(2024, 01, 01))
            .build();

        this.order = Order.builder()
            .assemblies(Collections.singletonList(assembly))
            .components(Collections.singletonList(component))
            .customer(TEST_PILOT)
            .documentNumber("123456")
            .comments("Test comments")
            .build();
    }

    @DisplayName("Find order by customer: Success")
    @Test
    void givenCustomerAndPaginationSetting_whenFindByCustomer_returnPageOfOrders(){
        Order savedOrder = orderRepository.save(order);

        Page<Order> orders = orderRepository.findByCustomer(TEST_PILOT, Pageable.ofSize(1));

        Assertions.assertThat(orders).isNotNull();
        Assertions.assertThat(orders.getPageable().getPageSize()).isEqualTo(1);

        orderRepository.delete(savedOrder);

    }

}

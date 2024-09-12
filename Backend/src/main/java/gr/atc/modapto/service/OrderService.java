package gr.atc.modapto.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import gr.atc.modapto.dto.OrderDto;
import gr.atc.modapto.exception.CustomExceptions.OrderNotFoundException;
import gr.atc.modapto.exception.CustomExceptions.PaginationException;
import gr.atc.modapto.model.Order;
import gr.atc.modapto.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final ModelMapper modelMapper;

    private final ElasticsearchOperations elasticSearchOperations;

    private static final String CUSTOMER_STRING = "customer";
    private static final String ORDER_OF_EXPECTED_DELIVERY_DATE_STRING = "orderof.expectedDeliveryDate";


    /**
     * Save a new Order in DB
     * 
     * @param orderDto
     * @return true on success, false on error
     */
    public boolean saveNewOrder(OrderDto orderDto) {
        Order order = modelMapper.map(orderDto, Order.class);
        Order savedOrder = orderRepository.save(order);

        return savedOrder != null;
    }

    /**
     * Save a list of new Orders in DB
     * 
     * @param List<OrderDto>
     * @return true on success, false on error
     */
    public boolean saveListOfOrders(List<OrderDto> orders) {
        List<Order> newOrders = orders.stream().map(orderDto -> modelMapper.map(orderDto, Order.class)).toList();
        List<Order> savedOrders = newOrders.stream()
                .map(orderRepository::save)
                .toList();
        return !savedOrders.isEmpty();
    }

    /**
     * Retrieve an order from DB by ID
     * 
     * @param List<OrderDto>
     * @return true on success, false on error
     * @throws OrderNotFoundException if the order with the given ID is not found
     * @throws MappingException if there's an error mapping Order to OrderDto
     */
    public OrderDto retrieveOrderById(String id) throws OrderNotFoundException{
        try {
            Optional<Order> optionalOrder = orderRepository.findById(id);
            if (optionalOrder.isPresent()) {
                return modelMapper.map(optionalOrder.get(), OrderDto.class);
            } else {
                log.warn("Order with id {} not found", id);
                throw new OrderNotFoundException(id);
            }
        } catch (MappingException e) {
            log.error("Unable to map Order to OrderDto! Error - {}", e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve all paginated results for orders per customer given the pagination
     * settings and optionally the date ranges
     * 
     * @param pilotCode
     * @param pageableElem
     * @param endDate
     */
    public Page<OrderDto> retrieveOrdersByCustomerFilteredByDates(String customer, Pageable pageable, String startDate, String endDate) {
        try {
            // Set the criteria for filtering the data
            Criteria criteria;
            if (startDate == null && endDate == null)
                criteria = new Criteria(CUSTOMER_STRING).is(customer);
            else if (startDate == null)
                criteria = new Criteria(CUSTOMER_STRING).is(customer)
                    .and(ORDER_OF_EXPECTED_DELIVERY_DATE_STRING).lessThanEqual(endDate);
            else if (endDate == null)
                criteria = new Criteria(CUSTOMER_STRING).is(customer)
                    .and(ORDER_OF_EXPECTED_DELIVERY_DATE_STRING).greaterThanEqual(startDate);
            else
                criteria = new Criteria(CUSTOMER_STRING).is(customer)
                    .and(ORDER_OF_EXPECTED_DELIVERY_DATE_STRING).between(startDate, endDate);

            // Define the sorting in pagination
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
  
            // Implement the query with the pagination in DB
            CriteriaQuery query = new CriteriaQuery(criteria).setPageable(sortedPageable);
            SearchHits<Order> searchHits = elasticSearchOperations.search(query, Order.class);

            // Check if searchHits are empty
            if (searchHits == null || searchHits.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            // Retrieve the orders from the Search Hits of DB
            List<OrderDto> orderDtos = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(order -> modelMapper.map(order, OrderDto.class))
                .toList();

            return new PageImpl<>(orderDtos, pageable, searchHits.getTotalHits());
        } catch (MappingException e) {
            log.error("Unable to map Order to OrderDto! Error - {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.error("Invalid pagination parameters. Error: {}", e.getMessage());
            throw new PaginationException(e.getCause());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving orders. Error: {}", e.getCause());
            return null;
        }
    }

}

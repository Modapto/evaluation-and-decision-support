package gr.atc.modapto.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import gr.atc.modapto.model.Order;

public interface OrderRepository extends ElasticsearchRepository<Order, String> {
    Page<Order> findByCustomer(String customer, Pageable pageable); 
}

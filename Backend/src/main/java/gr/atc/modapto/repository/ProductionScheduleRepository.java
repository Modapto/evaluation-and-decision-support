package gr.atc.modapto.repository;

import gr.atc.modapto.model.ProductionSchedule;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionScheduleRepository extends ElasticsearchRepository<ProductionSchedule, String> {
}

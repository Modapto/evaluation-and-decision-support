package gr.atc.modapto.repository;

import gr.atc.modapto.model.ScheduledTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledTaskRepository extends ElasticsearchRepository<ScheduledTask, String> {

    Page<ScheduledTask> findBySmartServiceType(String smartServiceType, Pageable pageable);
}

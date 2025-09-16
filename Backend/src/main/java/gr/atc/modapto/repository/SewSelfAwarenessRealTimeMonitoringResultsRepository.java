package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.SewSelfAwarenessRealTimeMonitoringResults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SewSelfAwarenessRealTimeMonitoringResultsRepository extends ElasticsearchRepository<SewSelfAwarenessRealTimeMonitoringResults, String> {
    Page<SewSelfAwarenessRealTimeMonitoringResults> findByModuleId(String moduleId, Pageable pageable);
}
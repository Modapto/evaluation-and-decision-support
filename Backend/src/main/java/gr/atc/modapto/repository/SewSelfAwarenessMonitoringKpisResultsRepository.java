package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.SewSelfAwarenessMonitoringKpisResults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SewSelfAwarenessMonitoringKpisResultsRepository extends ElasticsearchRepository<SewSelfAwarenessMonitoringKpisResults, String> {

    Optional<SewSelfAwarenessMonitoringKpisResults> findFirstByOrderByTimestampDesc();

    Optional<SewSelfAwarenessMonitoringKpisResults> findFirstByModuleIdOrderByTimestampDesc(String moduleId);

    Page<SewSelfAwarenessMonitoringKpisResults> findByModuleId(String moduleId, Pageable pageable);
}

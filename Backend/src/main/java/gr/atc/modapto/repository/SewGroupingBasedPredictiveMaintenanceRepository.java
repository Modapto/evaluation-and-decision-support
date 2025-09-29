package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.SewGroupingPredictiveMaintenanceResult;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SewGroupingBasedPredictiveMaintenanceRepository extends ElasticsearchRepository<SewGroupingPredictiveMaintenanceResult, String> {

    Optional<SewGroupingPredictiveMaintenanceResult> findFirstByModuleIdOrderByTimestampDesc(String moduleId);
}
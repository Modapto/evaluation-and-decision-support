package gr.atc.modapto.repository;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import gr.atc.modapto.model.serviceResults.SewThresholdBasedPredictiveMaintenanceResult;

@Repository
public interface SewThresholdBasedPredictiveMaintenanceRepository extends ElasticsearchRepository<SewThresholdBasedPredictiveMaintenanceResult, String> {

    Optional<SewThresholdBasedPredictiveMaintenanceResult> findFirstByModuleIdOrderByTimestampDesc(String moduleId);
}
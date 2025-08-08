package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.SewThresholdBasedPredictiveMaintenanceResult;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SewThresholdBasedPredictiveMaintenanceRepository extends ElasticsearchRepository<SewThresholdBasedPredictiveMaintenanceResult, String> {

    Optional<SewThresholdBasedPredictiveMaintenanceResult> findFirstByModuleId(String moduleId);
}
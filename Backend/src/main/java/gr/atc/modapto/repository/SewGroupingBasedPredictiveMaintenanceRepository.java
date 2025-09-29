package gr.atc.modapto.repository;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import gr.atc.modapto.model.serviceResults.SewGroupingPredictiveMaintenanceResult;

@Repository
public interface SewGroupingBasedPredictiveMaintenanceRepository extends ElasticsearchRepository<SewGroupingPredictiveMaintenanceResult, String> {

    Optional<SewGroupingPredictiveMaintenanceResult> findFirstByModuleIdOrderByTimestampDesc(String moduleId);
}
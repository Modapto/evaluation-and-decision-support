package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.SewOptimizationResults;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface SewOptimizationResultsRepository extends ElasticsearchRepository<SewOptimizationResults, String> {
    Optional<SewOptimizationResults> findFirstByOrderByTimestampDesc();

    Optional<SewOptimizationResults> findFirstByProductionModuleOrderByTimestampDesc(String productionModule);
}

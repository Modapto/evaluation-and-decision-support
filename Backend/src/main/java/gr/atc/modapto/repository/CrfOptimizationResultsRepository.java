package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.CrfOptimizationResults;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface CrfOptimizationResultsRepository extends ElasticsearchRepository<CrfOptimizationResults, String> {
    Optional<CrfOptimizationResults> findFirstByOrderByTimestampDesc();

    Optional<CrfOptimizationResults> findFirstByProductionModuleOrderByTimestampDesc(String productionModule);
}

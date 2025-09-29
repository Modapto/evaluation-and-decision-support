package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.CrfOptimizationResults;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CrfOptimizationResultsRepository extends ElasticsearchRepository<CrfOptimizationResults, String> {
    Optional<CrfOptimizationResults> findFirstByOrderByTimestampDesc();

    Optional<CrfOptimizationResults> findFirstByModuleIdOrderByTimestampDesc(String moduleId);
}

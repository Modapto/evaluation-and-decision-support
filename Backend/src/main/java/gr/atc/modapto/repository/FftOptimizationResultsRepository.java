package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.FftOptimizationResults;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FftOptimizationResultsRepository extends ElasticsearchRepository<FftOptimizationResults, String> {
    Optional<FftOptimizationResults> findFirstByOrderByTimestampDesc();

    Optional<FftOptimizationResults> findFirstByModuleOrderByTimestampDesc(String module);
}

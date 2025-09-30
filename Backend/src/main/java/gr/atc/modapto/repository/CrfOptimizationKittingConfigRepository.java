package gr.atc.modapto.repository;

import gr.atc.modapto.model.crf.CrfOptimizationKittingConfig;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrfOptimizationKittingConfigRepository extends ElasticsearchRepository<CrfOptimizationKittingConfig, String> {
}

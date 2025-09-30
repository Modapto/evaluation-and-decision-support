package gr.atc.modapto.repository;

import gr.atc.modapto.model.crf.CrfSimulationKittingConfig;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrfSimulationKittingConfigRepository extends ElasticsearchRepository<CrfSimulationKittingConfig, String> {
}
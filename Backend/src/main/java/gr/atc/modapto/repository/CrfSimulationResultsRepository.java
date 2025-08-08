package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.CrfSimulationResults;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface CrfSimulationResultsRepository extends ElasticsearchRepository<CrfSimulationResults, String> {
    Optional<CrfSimulationResults> findFirstByOrderByTimestampDesc();

    Optional<CrfSimulationResults> findFirstByProductionModuleOrderByTimestampDesc(String productionModule);
}

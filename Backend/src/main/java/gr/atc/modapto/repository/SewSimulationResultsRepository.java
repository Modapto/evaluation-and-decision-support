package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.SewSimulationResults;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface SewSimulationResultsRepository extends ElasticsearchRepository<SewSimulationResults, String> {
    Optional<SewSimulationResults> findFirstByOrderByTimestampDesc();

    Optional<SewSimulationResults> findFirstByProductionModuleOrderByTimestampDesc(String productionModule);
}

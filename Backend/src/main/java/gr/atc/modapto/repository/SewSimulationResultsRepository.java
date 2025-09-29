package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.SewSimulationResults;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface SewSimulationResultsRepository extends ElasticsearchRepository<SewSimulationResults, String> {
    Optional<SewSimulationResults> findFirstByOrderByTimestampDesc();

    Optional<SewSimulationResults> findFirstByModuleIdOrderByTimestampDesc(String moduleId);
}

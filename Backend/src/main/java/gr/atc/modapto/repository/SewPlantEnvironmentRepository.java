package gr.atc.modapto.repository;

import gr.atc.modapto.model.sew.SewPlantEnvironment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SewPlantEnvironmentRepository extends ElasticsearchRepository<SewPlantEnvironment, String> {
    Optional<SewPlantEnvironment> findFirstByOrderByTimestampCreatedDesc();
}

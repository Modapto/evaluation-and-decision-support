package gr.atc.modapto.repository;

import gr.atc.modapto.model.sew.SewMonitorKpisComponents;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SewMonitorKpisComponentsRepository extends ElasticsearchRepository<SewMonitorKpisComponents, String> {
    Optional<SewMonitorKpisComponents> findByModuleId(String moduleId);
    void deleteByModuleId(String moduleId);
}
package gr.atc.modapto.repository;

import gr.atc.modapto.model.ModaptoModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModaptoModuleRepository extends ElasticsearchRepository<ModaptoModule, String> {
    
    Optional<ModaptoModule> findByModuleId(String moduleId);

    Page<ModaptoModule> findByWorkers(String worker, Pageable pageable);
}

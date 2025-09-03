package gr.atc.modapto.repository;

import gr.atc.modapto.model.sew.SewComponentInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SewComponentInfoRepository extends ElasticsearchRepository<SewComponentInfo, String> {
    List<SewComponentInfo> findByStageAndCellAndModuleAndModuleId(String stage, String cell, String module, String moduleId);
}

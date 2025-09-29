package gr.atc.modapto.repository;

import gr.atc.modapto.model.serviceResults.CrfKitHolderEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrfKitHolderEventRepository extends ElasticsearchRepository<CrfKitHolderEvent, String> {
}

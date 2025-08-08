package gr.atc.modapto.repository;

import gr.atc.modapto.model.MaintenanceData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceDataRepository extends ElasticsearchRepository<MaintenanceData, String> {
    List<MaintenanceData> findByTsRequestCreationBetween(String startDate, String endDate);

    List<MaintenanceData> findByTsRequestCreationGreaterThanEqual(String startDate);

    List<MaintenanceData> findByTsRequestCreationLessThanEqual(String endDate);
}

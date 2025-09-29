package gr.atc.modapto.repository;

import gr.atc.modapto.model.MaintenanceData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceDataRepository extends ElasticsearchRepository<MaintenanceData, String> {
    List<MaintenanceData> findByTsRequestCreationBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<MaintenanceData> findByTsRequestCreationGreaterThanEqual(LocalDateTime startDate);

    List<MaintenanceData> findByTsRequestCreationLessThanEqual(LocalDateTime endDate);
}

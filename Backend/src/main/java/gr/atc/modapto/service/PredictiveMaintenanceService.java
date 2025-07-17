package gr.atc.modapto.service;

import gr.atc.modapto.dto.files.MaintenanceDataDto;
import gr.atc.modapto.model.MaintenanceData;
import gr.atc.modapto.repository.MaintenanceDataRepository;
import gr.atc.modapto.service.interfaces.IPredictiveMaintenanceService;
import gr.atc.modapto.util.ExcelFilesUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import gr.atc.modapto.exception.CustomExceptions.*;

@Service
public class PredictiveMaintenanceService implements IPredictiveMaintenanceService {

    private final Logger logger = LoggerFactory.getLogger(PredictiveMaintenanceService.class);

    private final MaintenanceDataRepository maintenanceDataRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    private final ModelMapper modelMapper;

    private static final int BATCH_SIZE = 1000; // Batch Size

    public PredictiveMaintenanceService(MaintenanceDataRepository maintenanceDataRepository, ModelMapper modelMapper, ElasticsearchOperations elasticsearchOperations) {
        this.maintenanceDataRepository = maintenanceDataRepository;
        this.modelMapper = modelMapper;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public void storeCorimData(MultipartFile file) {
        try {
            // Extract all data at once using parallel processing
            List<MaintenanceDataDto> allData = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            logger.info("-----------------------------------------------");
            logger.info("Extracted {} records from CORIM file", allData.size());
            logger.info("-----------------------------------------------");

            // Store in database batches
            storeBatch(allData);

        } catch (Exception e) {
            logger.error("Error processing Excel file", e);
            throw new FileHandlingException("Error processing Excel file");
        }
    }


    /**
     * Store a chunk of data in database batches
     */
    private void storeBatch(List<MaintenanceDataDto> chunkData) {
        try {
            for (int i = 0; i < chunkData.size(); i += BATCH_SIZE) {
                List<MaintenanceDataDto> dtoBatch = chunkData.subList(i, Math.min(i + BATCH_SIZE, chunkData.size()));
                List<MaintenanceData> batch = dtoBatch.stream().map(maintenanceDataDto -> modelMapper.map(maintenanceDataDto, MaintenanceData.class)).toList();
                maintenanceDataRepository.saveAll(batch);
            }
        } catch (ModelMappingException e){
            throw new ModelMappingException("Unable to parse DTO Maintenance Data to Model - Error: " + e.getMessage());
        }
    }

    /**
     * Retrieve all maintenance data for SEW plant, optionally filtered by date range
     *
     * @param startDate : Start date of the maintenance
     * @param endDate : Finish data of the maintenance
     * @return List<MaintenanceData>
     */
    @Override
    public List<MaintenanceDataDto> retrieveMaintenanceDataByDateRange(String startDate, String endDate){
            CriteriaQuery query;

            if (startDate != null && endDate != null) {
                query = new CriteriaQuery(
                        new Criteria("tsInterventionStarted").greaterThanEqual(startDate)
                                .and(new Criteria("tsInterventionFinished").lessThanEqual(endDate)), Pageable.unpaged()
                );
            } else if (startDate != null) {
                query = new CriteriaQuery(
                        new Criteria("tsInterventionStarted").greaterThanEqual(startDate), Pageable.unpaged()
                );
            } else if (endDate != null) {
                query = new CriteriaQuery(
                        new Criteria("tsInterventionFinished").lessThanEqual(endDate), Pageable.unpaged()
                );
            } else {
                return maintenanceDataRepository.findAll(Pageable.unpaged())
                        .getContent()
                        .stream()
                        .map(data -> modelMapper.map(data,MaintenanceDataDto.class))
                        .toList();
            }

            SearchHits<MaintenanceData> searchHits = elasticsearchOperations.search(query, MaintenanceData.class);
            try {
                return searchHits.getSearchHits()
                        .stream()
                        .map(SearchHit::getContent)
                        .map(data -> modelMapper.map(data,MaintenanceDataDto.class))
                        .toList();
            } catch (ModelMappingException e){
                throw new ModelMappingException("Unable to parse Maintenance Data to DTO - Error: " + e.getMessage());
            }
        }
}

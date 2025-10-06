package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessMonitoringKpisResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataElasticsearchTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("SewSelfAwarenessMonitoringKpisResultsRepository Tests")
class SewSelfAwarenessMonitoringKpisResultsRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private SewSelfAwarenessMonitoringKpisResultsRepository repository;

    private SewSelfAwarenessMonitoringKpisResults sampleResult1;
    private SewSelfAwarenessMonitoringKpisResults sampleResult2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        sampleResult1 = new SewSelfAwarenessMonitoringKpisResults();
        sampleResult1.setModuleId("TEST_MODULE_1");
        sampleResult1.setSmartServiceId("SELF_AWARENESS_SERVICE");
        sampleResult1.setTimestamp(LocalDateTime.now());

        sampleResult2 = new SewSelfAwarenessMonitoringKpisResults();
        sampleResult2.setModuleId("TEST_MODULE_2");
        sampleResult2.setSmartServiceId("SELF_AWARENESS_SERVICE");
        sampleResult2.setTimestamp(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Save result : Success")
        void givenValidResult_whenSave_thenPersistsSuccessfully() {
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(saved.getSmartServiceId()).isEqualTo("SELF_AWARENESS_SERVICE");
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedResult_whenFindById_thenReturnsResult() {
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);

            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getSmartServiceId()).isEqualTo("SELF_AWARENESS_SERVICE");
        }

        @Test
        @DisplayName("Delete result : Success")
        void givenSavedResult_whenDelete_thenRemovesResult() {
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);

            repository.delete(saved);

            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find all : Success")
        void givenMultipleResults_whenFindAll_thenReturnsAllResults() {
            repository.saveAll(Arrays.asList(sampleResult1, sampleResult2));

            Iterable<SewSelfAwarenessMonitoringKpisResults> results = repository.findAll();

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("Find first by order by timestamp desc : Success")
        void givenMultipleResults_whenFindFirstByOrderByTimestampDesc_thenReturnsLatest() {
            SewSelfAwarenessMonitoringKpisResults older = new SewSelfAwarenessMonitoringKpisResults();
            older.setModuleId("SHARED_MODULE");
            older.setSmartServiceId("SERVICE_1");
            older.setTimestamp(LocalDateTime.now().minusHours(1));

            SewSelfAwarenessMonitoringKpisResults newer = new SewSelfAwarenessMonitoringKpisResults();
            newer.setModuleId("SHARED_MODULE");
            newer.setSmartServiceId("SERVICE_2");
            newer.setTimestamp(LocalDateTime.now());

            repository.saveAll(Arrays.asList(older, newer));

            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findFirstByOrderByTimestampDesc();

            assertThat(found).isPresent();
            assertThat(found.get().getSmartServiceId()).isEqualTo("SERVICE_2");
        }

        @Test
        @DisplayName("Find first by order by timestamp desc : No results")
        void givenNoResults_whenFindFirstByOrderByTimestampDesc_thenReturnsEmpty() {
            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findFirstByOrderByTimestampDesc();

            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find first by module ID order by timestamp desc : Success")
        void givenMultipleResultsForModule_whenFindFirstByModuleIdOrderByTimestampDesc_thenReturnsLatestForModule() {
            SewSelfAwarenessMonitoringKpisResults older = new SewSelfAwarenessMonitoringKpisResults();
            older.setModuleId("TARGET_MODULE");
            older.setSmartServiceId("SERVICE_1");
            older.setTimestamp(LocalDateTime.now().minusHours(2));

            SewSelfAwarenessMonitoringKpisResults newer = new SewSelfAwarenessMonitoringKpisResults();
            newer.setModuleId("TARGET_MODULE");
            newer.setSmartServiceId("SERVICE_2");
            newer.setTimestamp(LocalDateTime.now().minusHours(1));

            SewSelfAwarenessMonitoringKpisResults differentModule = new SewSelfAwarenessMonitoringKpisResults();
            differentModule.setModuleId("DIFFERENT_MODULE");
            differentModule.setSmartServiceId("SERVICE_3");
            differentModule.setTimestamp(LocalDateTime.now());

            repository.saveAll(Arrays.asList(older, newer, differentModule));

            Optional<SewSelfAwarenessMonitoringKpisResults> found = 
                repository.findFirstByModuleIdOrderByTimestampDesc("TARGET_MODULE");

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TARGET_MODULE");
            assertThat(found.get().getSmartServiceId()).isEqualTo("SERVICE_2");
        }

        @Test
        @DisplayName("Find first by module ID order by timestamp desc : No results for module")
        void givenNoResultsForModule_whenFindFirstByModuleIdOrderByTimestampDesc_thenReturnsEmpty() {
            repository.save(sampleResult1);

            Optional<SewSelfAwarenessMonitoringKpisResults> found = 
                repository.findFirstByModuleIdOrderByTimestampDesc("NON_EXISTENT_MODULE");

            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find by module ID : Success")
        void givenResultsForModule_whenFindByModuleId_thenReturnsAllResultsForModule() {
            SewSelfAwarenessMonitoringKpisResults result1 = new SewSelfAwarenessMonitoringKpisResults();
            result1.setModuleId("SHARED_MODULE");
            result1.setSmartServiceId("SERVICE_1");
            result1.setTimestamp(LocalDateTime.now().minusHours(2));

            SewSelfAwarenessMonitoringKpisResults result2 = new SewSelfAwarenessMonitoringKpisResults();
            result2.setModuleId("SHARED_MODULE");
            result2.setSmartServiceId("SERVICE_2");
            result2.setTimestamp(LocalDateTime.now().minusHours(1));

            SewSelfAwarenessMonitoringKpisResults differentModule = new SewSelfAwarenessMonitoringKpisResults();
            differentModule.setModuleId("DIFFERENT_MODULE");
            differentModule.setSmartServiceId("SERVICE_3");
            differentModule.setTimestamp(LocalDateTime.now());

            repository.saveAll(Arrays.asList(result1, result2, differentModule));

            Pageable pageable = PageRequest.of(0, 10);
            Page<SewSelfAwarenessMonitoringKpisResults> found = repository.findByModuleId("SHARED_MODULE", pageable);

            assertThat(found).isNotNull();
            assertThat(found.getContent()).hasSize(2);
            assertThat(found.getContent()).allMatch(result -> "SHARED_MODULE".equals(result.getModuleId()));
        }

        @Test
        @DisplayName("Find by module ID : Empty page")
        void givenNoResultsForModule_whenFindByModuleId_thenReturnsEmptyPage() {
            repository.save(sampleResult1);

            Pageable pageable = PageRequest.of(0, 10);
            Page<SewSelfAwarenessMonitoringKpisResults> found = repository.findByModuleId("NON_EXISTENT_MODULE", pageable);

            assertThat(found).isNotNull();
            assertThat(found.getContent()).isEmpty();
            assertThat(found.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Find by module ID : Pagination")
        void givenMultipleResultsForModule_whenFindByModuleIdWithPagination_thenReturnsPagedResults() {
            List<SewSelfAwarenessMonitoringKpisResults> results = Arrays.asList(
                    createResult("SHARED_MODULE", "SERVICE_1", LocalDateTime.now().minusHours(4)),
                    createResult("SHARED_MODULE", "SERVICE_2", LocalDateTime.now().minusHours(3)),
                    createResult("SHARED_MODULE", "SERVICE_3", LocalDateTime.now().minusHours(2)),
                    createResult("SHARED_MODULE", "SERVICE_4", LocalDateTime.now().minusHours(1)),
                    createResult("SHARED_MODULE", "SERVICE_5", LocalDateTime.now())
            );
            repository.saveAll(results);

            Pageable firstPage = PageRequest.of(0, 2);
            Page<SewSelfAwarenessMonitoringKpisResults> page1 = repository.findByModuleId("SHARED_MODULE", firstPage);

            Pageable secondPage = PageRequest.of(1, 2);
            Page<SewSelfAwarenessMonitoringKpisResults> page2 = repository.findByModuleId("SHARED_MODULE", secondPage);

            assertThat(page1.getContent()).hasSize(2);
            assertThat(page1.getTotalElements()).isEqualTo(5);
            assertThat(page1.getTotalPages()).isEqualTo(3);
            assertThat(page1.isFirst()).isTrue();

            assertThat(page2.getContent()).hasSize(2);
            assertThat(page2.getTotalElements()).isEqualTo(5);
            assertThat(page2.getTotalPages()).isEqualTo(3);
            assertThat(page2.isLast()).isFalse();
        }
    }

    @Nested
    @DisplayName("Data Integrity and Edge Cases")
    class DataIntegrityAndEdgeCases {

        @Test
        @DisplayName("Save result with null timestamp : Success")
        void givenResultWithNullTimestamp_whenSave_thenPersistsSuccessfully() {
            SewSelfAwarenessMonitoringKpisResults resultWithNullTimestamp = new SewSelfAwarenessMonitoringKpisResults();
            resultWithNullTimestamp.setModuleId("TEST_MODULE");
            resultWithNullTimestamp.setSmartServiceId("SELF_AWARENESS_SERVICE");
            resultWithNullTimestamp.setTimestamp(null);

            SewSelfAwarenessMonitoringKpisResults saved = repository.save(resultWithNullTimestamp);

            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE");
            assertThat(saved.getTimestamp()).isNull();
        }

        @Test
        @DisplayName("Update existing result : Success")
        void givenExistingResult_whenUpdate_thenUpdatesSuccessfully() {
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);
            String originalId = saved.getId();

            saved.setSmartServiceId("UPDATED_SERVICE");
            saved.setTimestamp(LocalDateTime.now());
            SewSelfAwarenessMonitoringKpisResults updated = repository.save(saved);

            assertThat(updated.getId()).isEqualTo(originalId);
            assertThat(updated.getSmartServiceId()).isEqualTo("UPDATED_SERVICE");
            assertThat(updated.getModuleId()).isEqualTo("TEST_MODULE_1");
        }


        @Test
        @DisplayName("Find first by timestamp desc : Order verification")
        void givenResultsWithDifferentTimestamps_whenFindFirstByOrderByTimestampDesc_thenReturnsCorrectOrder() {
            SewSelfAwarenessMonitoringKpisResults oldest = createResult("MODULE_1", "SERVICE_1", LocalDateTime.now().minusHours(2));
            SewSelfAwarenessMonitoringKpisResults middle = createResult("MODULE_2", "SERVICE_2", LocalDateTime.now().minusHours(1));
            SewSelfAwarenessMonitoringKpisResults newest = createResult("MODULE_3", "SERVICE_3", LocalDateTime.now());

            repository.saveAll(Arrays.asList(oldest, middle, newest));

            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findFirstByOrderByTimestampDesc();

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("MODULE_3");
        }
    }

    private SewSelfAwarenessMonitoringKpisResults createResult(String moduleId, String smartServiceId, LocalDateTime timestamp) {
        SewSelfAwarenessMonitoringKpisResults result = new SewSelfAwarenessMonitoringKpisResults();
        result.setModuleId(moduleId);
        result.setSmartServiceId(smartServiceId);
        result.setTimestamp(timestamp);
        return result;
    }
}
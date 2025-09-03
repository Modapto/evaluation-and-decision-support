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
import java.time.format.DateTimeFormatter;
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
        sampleResult1.setTimestamp("2024-01-15T14:30:45Z");

        sampleResult2 = new SewSelfAwarenessMonitoringKpisResults();
        sampleResult2.setModuleId("TEST_MODULE_2");
        sampleResult2.setSmartServiceId("SELF_AWARENESS_SERVICE");
        sampleResult2.setTimestamp("2024-01-16T10:15:30Z");
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Save result : Success")
        void givenValidResult_whenSave_thenPersistsSuccessfully() {
            // When
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(saved.getSmartServiceId()).isEqualTo("SELF_AWARENESS_SERVICE");
            assertThat(saved.getTimestamp()).isEqualTo("2024-01-15T14:30:45Z");
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedResult_whenFindById_thenReturnsResult() {
            // Given
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);

            // When
            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getSmartServiceId()).isEqualTo("SELF_AWARENESS_SERVICE");
        }

        @Test
        @DisplayName("Delete result : Success")
        void givenSavedResult_whenDelete_thenRemovesResult() {
            // Given
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);

            // When
            repository.delete(saved);

            // Then
            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find all : Success")
        void givenMultipleResults_whenFindAll_thenReturnsAllResults() {
            // Given
            repository.saveAll(Arrays.asList(sampleResult1, sampleResult2));

            // When
            Iterable<SewSelfAwarenessMonitoringKpisResults> results = repository.findAll();

            // Then
            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("Find first by order by timestamp desc : Success")
        void givenMultipleResults_whenFindFirstByOrderByTimestampDesc_thenReturnsLatest() {
            // Given
            SewSelfAwarenessMonitoringKpisResults older = new SewSelfAwarenessMonitoringKpisResults();
            older.setModuleId("SHARED_MODULE");
            older.setSmartServiceId("SERVICE_1");
            older.setTimestamp("2024-01-15T08:00:00Z");

            SewSelfAwarenessMonitoringKpisResults newer = new SewSelfAwarenessMonitoringKpisResults();
            newer.setModuleId("SHARED_MODULE");
            newer.setSmartServiceId("SERVICE_2");
            newer.setTimestamp("2024-01-15T09:00:00Z");

            repository.saveAll(Arrays.asList(older, newer));

            // When
            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findFirstByOrderByTimestampDesc();

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getSmartServiceId()).isEqualTo("SERVICE_2");
            assertThat(found.get().getTimestamp()).isEqualTo("2024-01-15T09:00:00Z");
        }

        @Test
        @DisplayName("Find first by order by timestamp desc : No results")
        void givenNoResults_whenFindFirstByOrderByTimestampDesc_thenReturnsEmpty() {
            // When
            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findFirstByOrderByTimestampDesc();

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find first by module ID order by timestamp desc : Success")
        void givenMultipleResultsForModule_whenFindFirstByModuleIdOrderByTimestampDesc_thenReturnsLatestForModule() {
            // Given
            SewSelfAwarenessMonitoringKpisResults older = new SewSelfAwarenessMonitoringKpisResults();
            older.setModuleId("TARGET_MODULE");
            older.setSmartServiceId("SERVICE_1");
            older.setTimestamp(LocalDateTime.parse("2024-01-15T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME).minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");

            SewSelfAwarenessMonitoringKpisResults newer = new SewSelfAwarenessMonitoringKpisResults();
            newer.setModuleId("TARGET_MODULE");
            newer.setSmartServiceId("SERVICE_2");
            newer.setTimestamp(LocalDateTime.parse("2024-01-15T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME).minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");

            SewSelfAwarenessMonitoringKpisResults differentModule = new SewSelfAwarenessMonitoringKpisResults();
            differentModule.setModuleId("DIFFERENT_MODULE");
            differentModule.setSmartServiceId("SERVICE_3");
            differentModule.setTimestamp("2024-01-15T10:00:00Z");

            repository.saveAll(Arrays.asList(older, newer, differentModule));

            // When
            Optional<SewSelfAwarenessMonitoringKpisResults> found = 
                repository.findFirstByModuleIdOrderByTimestampDesc("TARGET_MODULE");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TARGET_MODULE");
            assertThat(found.get().getSmartServiceId()).isEqualTo("SERVICE_2");
            assertThat(found.get().getTimestamp()).isEqualTo("2024-01-15T09:00:00Z");
        }

        @Test
        @DisplayName("Find first by module ID order by timestamp desc : No results for module")
        void givenNoResultsForModule_whenFindFirstByModuleIdOrderByTimestampDesc_thenReturnsEmpty() {
            // Given
            repository.save(sampleResult1);

            // When
            Optional<SewSelfAwarenessMonitoringKpisResults> found = 
                repository.findFirstByModuleIdOrderByTimestampDesc("NON_EXISTENT_MODULE");

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find by module ID : Success")
        void givenResultsForModule_whenFindByModuleId_thenReturnsAllResultsForModule() {
            // Given
            SewSelfAwarenessMonitoringKpisResults result1 = new SewSelfAwarenessMonitoringKpisResults();
            result1.setModuleId("SHARED_MODULE");
            result1.setSmartServiceId("SERVICE_1");
            result1.setTimestamp(LocalDateTime.parse("2024-01-15T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME).minusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");

            SewSelfAwarenessMonitoringKpisResults result2 = new SewSelfAwarenessMonitoringKpisResults();
            result2.setModuleId("SHARED_MODULE");
            result2.setSmartServiceId("SERVICE_2");
            result2.setTimestamp(LocalDateTime.parse("2024-01-15T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME).minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");

            SewSelfAwarenessMonitoringKpisResults differentModule = new SewSelfAwarenessMonitoringKpisResults();
            differentModule.setModuleId("DIFFERENT_MODULE");
            differentModule.setSmartServiceId("SERVICE_3");
            differentModule.setTimestamp("2024-01-15T10:00:00Z");

            repository.saveAll(Arrays.asList(result1, result2, differentModule));

            // When
            Pageable pageable = PageRequest.of(0, 10);
            Page<SewSelfAwarenessMonitoringKpisResults> found = repository.findByModuleId("SHARED_MODULE", pageable);

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getContent()).hasSize(2);
            assertThat(found.getContent()).allMatch(result -> "SHARED_MODULE".equals(result.getModuleId()));
        }

        @Test
        @DisplayName("Find by module ID : Empty page")
        void givenNoResultsForModule_whenFindByModuleId_thenReturnsEmptyPage() {
            // Given
            repository.save(sampleResult1);

            // When
            Pageable pageable = PageRequest.of(0, 10);
            Page<SewSelfAwarenessMonitoringKpisResults> found = repository.findByModuleId("NON_EXISTENT_MODULE", pageable);

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getContent()).isEmpty();
            assertThat(found.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Find by module ID : Pagination")
        void givenMultipleResultsForModule_whenFindByModuleIdWithPagination_thenReturnsPagedResults() {
            // Given
            List<SewSelfAwarenessMonitoringKpisResults> results = Arrays.asList(
                    createResult("SHARED_MODULE", "SERVICE_1", "2024-01-15T05:00:00Z"),
                    createResult("SHARED_MODULE", "SERVICE_2", "2024-01-15T06:00:00Z"),
                    createResult("SHARED_MODULE", "SERVICE_3", "2024-01-15T07:00:00Z"),
                    createResult("SHARED_MODULE", "SERVICE_4", "2024-01-15T08:00:00Z"),
                    createResult("SHARED_MODULE", "SERVICE_5", "2024-01-15T09:00:00Z")
            );
            repository.saveAll(results);

            // When
            Pageable firstPage = PageRequest.of(0, 2);
            Page<SewSelfAwarenessMonitoringKpisResults> page1 = repository.findByModuleId("SHARED_MODULE", firstPage);

            Pageable secondPage = PageRequest.of(1, 2);
            Page<SewSelfAwarenessMonitoringKpisResults> page2 = repository.findByModuleId("SHARED_MODULE", secondPage);

            // Then
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
            // Given
            SewSelfAwarenessMonitoringKpisResults resultWithNullTimestamp = new SewSelfAwarenessMonitoringKpisResults();
            resultWithNullTimestamp.setModuleId("TEST_MODULE");
            resultWithNullTimestamp.setSmartServiceId("SELF_AWARENESS_SERVICE");
            resultWithNullTimestamp.setTimestamp(null);

            // When
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(resultWithNullTimestamp);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE");
            assertThat(saved.getTimestamp()).isNull();
        }

        @Test
        @DisplayName("Update existing result : Success")
        void givenExistingResult_whenUpdate_thenUpdatesSuccessfully() {
            // Given
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(sampleResult1);
            String originalId = saved.getId();

            // When
            saved.setSmartServiceId("UPDATED_SERVICE");
            saved.setTimestamp("2024-01-15T11:00:00Z");
            SewSelfAwarenessMonitoringKpisResults updated = repository.save(saved);

            // Then
            assertThat(updated.getId()).isEqualTo(originalId);
            assertThat(updated.getSmartServiceId()).isEqualTo("UPDATED_SERVICE");
            assertThat(updated.getModuleId()).isEqualTo("TEST_MODULE_1");
        }

        @Test
        @DisplayName("Save result with special characters : Success")
        void givenResultWithSpecialCharacters_whenSave_thenPersistsSuccessfully() {
            // Given
            SewSelfAwarenessMonitoringKpisResults specialResult = new SewSelfAwarenessMonitoringKpisResults();
            specialResult.setModuleId("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
            specialResult.setSmartServiceId("SERVICE@#$%^&*()_+-=[]{}|;':\",./<>?");
            specialResult.setTimestamp("2024-01-15T10:00:00Z");

            // When
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(specialResult);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
            assertThat(saved.getSmartServiceId()).isEqualTo("SERVICE@#$%^&*()_+-=[]{}|;':\",./<>?");

            // Verify retrieval
            Optional<SewSelfAwarenessMonitoringKpisResults> found = 
                repository.findFirstByModuleIdOrderByTimestampDesc("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
        }

        @Test
        @DisplayName("Save multiple results for same module : Success")
        void givenMultipleResultsForSameModule_whenSave_thenAllPersistSuccessfully() {
            // Given
            SewSelfAwarenessMonitoringKpisResults result1 = createResult("SHARED_MODULE", "SERVICE_1", "2024-01-15T08:00:00Z");
            SewSelfAwarenessMonitoringKpisResults result2 = createResult("SHARED_MODULE", "SERVICE_2", "2024-01-15T09:00:00Z");

            // When
            repository.saveAll(Arrays.asList(result1, result2));

            // Then
            Optional<SewSelfAwarenessMonitoringKpisResults> latest = 
                repository.findFirstByModuleIdOrderByTimestampDesc("SHARED_MODULE");
            assertThat(latest).isPresent();

            long count = repository.count();
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Save result with empty strings : Success")
        void givenResultWithEmptyStrings_whenSave_thenPersistsSuccessfully() {
            // Given
            SewSelfAwarenessMonitoringKpisResults emptyStringResult = new SewSelfAwarenessMonitoringKpisResults();
            emptyStringResult.setModuleId("");
            emptyStringResult.setSmartServiceId("");
            emptyStringResult.setTimestamp("2024-01-15T10:00:00Z");

            // When
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(emptyStringResult);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEmpty();
            assertThat(saved.getSmartServiceId()).isEmpty();
        }

        @Test
        @DisplayName("Save result with long string fields : Success")
        void givenResultWithLongStringFields_whenSave_thenPersistsSuccessfully() {
            // Given
            String longModuleId = "MODULE_" + "A".repeat(500);
            String longSmartServiceId = "SERVICE_" + "B".repeat(500);

            SewSelfAwarenessMonitoringKpisResults longResult = new SewSelfAwarenessMonitoringKpisResults();
            longResult.setModuleId(longModuleId);
            longResult.setSmartServiceId(longSmartServiceId);
            longResult.setTimestamp("2024-01-15T10:00:00Z");

            // When
            SewSelfAwarenessMonitoringKpisResults saved = repository.save(longResult);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).hasSize(507);
            assertThat(saved.getSmartServiceId()).hasSize(508);
        }

        @Test
        @DisplayName("Find first by timestamp desc : Order verification")
        void givenResultsWithDifferentTimestamps_whenFindFirstByOrderByTimestampDesc_thenReturnsCorrectOrder() {
            // Given
            SewSelfAwarenessMonitoringKpisResults oldest = createResult("MODULE_1", "SERVICE_1", "2024-01-01T10:00:00Z");
            SewSelfAwarenessMonitoringKpisResults middle = createResult("MODULE_2", "SERVICE_2", "2024-01-02T10:00:00Z");
            SewSelfAwarenessMonitoringKpisResults newest = createResult("MODULE_3", "SERVICE_3", "2024-01-03T10:00:00Z");

            repository.saveAll(Arrays.asList(oldest, middle, newest));

            // When
            Optional<SewSelfAwarenessMonitoringKpisResults> found = repository.findFirstByOrderByTimestampDesc();

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("MODULE_3");
            assertThat(found.get().getTimestamp()).isEqualTo("2024-01-03T10:00:00Z");
        }
    }

    private SewSelfAwarenessMonitoringKpisResults createResult(String moduleId, String smartServiceId, String timestamp) {
        SewSelfAwarenessMonitoringKpisResults result = new SewSelfAwarenessMonitoringKpisResults();
        result.setModuleId(moduleId);
        result.setSmartServiceId(smartServiceId);
        result.setTimestamp(timestamp);
        return result;
    }
}
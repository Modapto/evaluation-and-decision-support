package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.serviceResults.SewGroupingPredictiveMaintenanceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataElasticsearchTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("SewGroupingBasedPredictiveMaintenanceRepository Tests")
class SewGroupingBasedPredictiveMaintenanceRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private SewGroupingBasedPredictiveMaintenanceRepository repository;

    private SewGroupingPredictiveMaintenanceResult sampleResult1;
    private SewGroupingPredictiveMaintenanceResult sampleResult2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        sampleResult1 = new SewGroupingPredictiveMaintenanceResult();
        sampleResult1.setModuleId("TEST_MODULE_1");
        sampleResult1.setSmartServiceId("GROUPING_SERVICE");
        sampleResult1.setTimestamp(LocalDateTime.of(2024, 1, 15, 14, 30, 45));

        sampleResult2 = new SewGroupingPredictiveMaintenanceResult();
        sampleResult2.setModuleId("TEST_MODULE_2");
        sampleResult2.setSmartServiceId("GROUPING_SERVICE");
        sampleResult2.setTimestamp(LocalDateTime.of(2024, 1, 16, 10, 15, 30));
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Save result : Success")
        void givenValidResult_whenSave_thenPersistsSuccessfully() {
            SewGroupingPredictiveMaintenanceResult saved = repository.save(sampleResult1);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(saved.getSmartServiceId()).isEqualTo("GROUPING_SERVICE");
            assertThat(saved.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30, 45));
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedResult_whenFindById_thenReturnsResult() {
            SewGroupingPredictiveMaintenanceResult saved = repository.save(sampleResult1);

            Optional<SewGroupingPredictiveMaintenanceResult> found = repository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getSmartServiceId()).isEqualTo("GROUPING_SERVICE");
        }

        @Test
        @DisplayName("Delete result : Success")
        void givenSavedResult_whenDelete_thenRemovesResult() {
            SewGroupingPredictiveMaintenanceResult saved = repository.save(sampleResult1);

            repository.delete(saved);

            Optional<SewGroupingPredictiveMaintenanceResult> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("Find first by module ID : Success")
        void givenMultipleResults_whenFindFirstByModuleId_thenReturnsFirstResult() {
            SewGroupingPredictiveMaintenanceResult result1 = new SewGroupingPredictiveMaintenanceResult();
            result1.setModuleId("SHARED_MODULE");
            result1.setSmartServiceId("SERVICE_1");
            result1.setTimestamp(LocalDateTime.now().minusHours(2));

            SewGroupingPredictiveMaintenanceResult result2 = new SewGroupingPredictiveMaintenanceResult();
            result2.setModuleId("SHARED_MODULE");
            result2.setSmartServiceId("SERVICE_2");
            result2.setTimestamp(LocalDateTime.now().minusHours(1));

            repository.saveAll(Arrays.asList(result1, result2));

            Optional<SewGroupingPredictiveMaintenanceResult> found = repository.findFirstByModuleId("SHARED_MODULE");

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("SHARED_MODULE");
            assertThat(found.get().getSmartServiceId()).isIn("SERVICE_1", "SERVICE_2");
        }

        @Test
        @DisplayName("Find first by module ID : No results")
        void givenNoMatchingResults_whenFindFirstByModuleId_thenReturnsEmpty() {
            repository.save(sampleResult1);

            Optional<SewGroupingPredictiveMaintenanceResult> found = repository.findFirstByModuleId("NON_EXISTENT_MODULE");

            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find first by module ID : Single result")
        void givenSingleResult_whenFindFirstByModuleId_thenReturnsResult() {
            repository.save(sampleResult1);

            Optional<SewGroupingPredictiveMaintenanceResult> found = repository.findFirstByModuleId("TEST_MODULE_1");

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getSmartServiceId()).isEqualTo("GROUPING_SERVICE");
        }
    }

    @Nested
    @DisplayName("Data Integrity and Edge Cases")
    class DataIntegrityAndEdgeCases {

        @Test
        @DisplayName("Save result with null timestamp : Success")
        void givenResultWithNullTimestamp_whenSave_thenPersistsSuccessfully() {
            SewGroupingPredictiveMaintenanceResult resultWithNullTimestamp = new SewGroupingPredictiveMaintenanceResult();
            resultWithNullTimestamp.setModuleId("TEST_MODULE");
            resultWithNullTimestamp.setSmartServiceId("GROUPING_SERVICE");
            resultWithNullTimestamp.setTimestamp(null);

            SewGroupingPredictiveMaintenanceResult saved = repository.save(resultWithNullTimestamp);

            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE");
            assertThat(saved.getTimestamp()).isNull();
        }

        @Test
        @DisplayName("Update existing result : Success")
        void givenExistingResult_whenUpdate_thenUpdatesSuccessfully() {
            SewGroupingPredictiveMaintenanceResult saved = repository.save(sampleResult1);
            String originalId = saved.getId();

            saved.setSmartServiceId("UPDATED_SERVICE");
            saved.setTimestamp(LocalDateTime.now());
            SewGroupingPredictiveMaintenanceResult updated = repository.save(saved);

            assertThat(updated.getId()).isEqualTo(originalId);
            assertThat(updated.getSmartServiceId()).isEqualTo("UPDATED_SERVICE");
            assertThat(updated.getModuleId()).isEqualTo("TEST_MODULE_1"); // Unchanged
        }

        @Test
        @DisplayName("Find by module ID with special characters : Success")
        void givenModuleIdWithSpecialCharacters_whenFindByModuleId_thenFindsCorrectly() {
            SewGroupingPredictiveMaintenanceResult specialResult = new SewGroupingPredictiveMaintenanceResult();
            specialResult.setModuleId("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
            specialResult.setSmartServiceId("GROUPING_SERVICE");
            specialResult.setTimestamp(LocalDateTime.now());

            repository.save(specialResult);

            Optional<SewGroupingPredictiveMaintenanceResult> found = 
                    repository.findFirstByModuleId("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
            assertThat(found.get().getSmartServiceId()).isEqualTo("GROUPING_SERVICE");
        }

        @Test
        @DisplayName("Save multiple results for same module : Success")
        void givenMultipleResultsForSameModule_whenSave_thenAllPersistSuccessfully() {
            SewGroupingPredictiveMaintenanceResult result1 = new SewGroupingPredictiveMaintenanceResult();
            result1.setModuleId("SHARED_MODULE");
            result1.setSmartServiceId("SERVICE_1");
            result1.setTimestamp(LocalDateTime.now().minusHours(2));

            SewGroupingPredictiveMaintenanceResult result2 = new SewGroupingPredictiveMaintenanceResult();
            result2.setModuleId("SHARED_MODULE");
            result2.setSmartServiceId("SERVICE_2");
            result2.setTimestamp(LocalDateTime.now().minusHours(1));

            repository.saveAll(Arrays.asList(result1, result2));

            Optional<SewGroupingPredictiveMaintenanceResult> found = repository.findFirstByModuleId("SHARED_MODULE");
            assertThat(found).isPresent();

            long count = repository.count();
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Save result with empty strings : Success")
        void givenResultWithEmptyStrings_whenSave_thenPersistsSuccessfully() {
            SewGroupingPredictiveMaintenanceResult emptyStringResult = new SewGroupingPredictiveMaintenanceResult();
            emptyStringResult.setModuleId("");
            emptyStringResult.setSmartServiceId("");
            emptyStringResult.setTimestamp(LocalDateTime.now());

            SewGroupingPredictiveMaintenanceResult saved = repository.save(emptyStringResult);

            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEmpty();
            assertThat(saved.getSmartServiceId()).isEmpty();
        }

        @Test
        @DisplayName("Save result with long string fields : Success")
        void givenResultWithLongStringFields_whenSave_thenPersistsSuccessfully() {
            String longModuleId = "MODULE_" + "A".repeat(500);
            String longSmartServiceId = "SERVICE_" + "B".repeat(500);

            SewGroupingPredictiveMaintenanceResult longResult = new SewGroupingPredictiveMaintenanceResult();
            longResult.setModuleId(longModuleId);
            longResult.setSmartServiceId(longSmartServiceId);
            longResult.setTimestamp(LocalDateTime.now());

            SewGroupingPredictiveMaintenanceResult saved = repository.save(longResult);

            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).hasSize(507); // "MODULE_" + 500 'A's
            assertThat(saved.getSmartServiceId()).hasSize(508); // "SERVICE_" + 500 'B's
        }
    }
}
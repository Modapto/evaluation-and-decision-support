package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.serviceResults.SewThresholdBasedPredictiveMaintenanceResult;
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
@DisplayName("SewThresholdBasedPredictiveMaintenanceRepository Tests")
class SewThresholdBasedPredictiveMaintenanceRepositoryTests extends SetupTestContainersEnvironment{

    @Autowired
    private SewThresholdBasedPredictiveMaintenanceRepository repository;

    private SewThresholdBasedPredictiveMaintenanceResult sampleResult1;
    private SewThresholdBasedPredictiveMaintenanceResult sampleResult2;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        sampleResult1 = new SewThresholdBasedPredictiveMaintenanceResult();
        sampleResult1.setModuleId("TEST_MODULE_1");
        sampleResult1.setSmartServiceId("THRESHOLD_SERVICE");
        sampleResult1.setRecommendation("Replace bearing in motor unit within 72 hours");
        sampleResult1.setDetails("Vibration levels exceeded threshold of 2.5mm/s RMS");
        sampleResult1.setTimestamp(LocalDateTime.of(2024, 1, 15, 14, 30, 45));

        sampleResult2 = new SewThresholdBasedPredictiveMaintenanceResult();
        sampleResult2.setModuleId("TEST_MODULE_2");
        sampleResult2.setSmartServiceId("THRESHOLD_SERVICE");
        sampleResult2.setRecommendation("Schedule inspection within 48 hours");
        sampleResult2.setDetails("Temperature trending upward beyond normal range");
        sampleResult2.setTimestamp(LocalDateTime.of(2024, 1, 16, 10, 15, 30));
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Save result : Success")
        void givenValidResult_whenSave_thenPersistsSuccessfully() {
            SewThresholdBasedPredictiveMaintenanceResult saved = repository.save(sampleResult1);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(saved.getSmartServiceId()).isEqualTo("THRESHOLD_SERVICE");
            assertThat(saved.getRecommendation()).isEqualTo("Replace bearing in motor unit within 72 hours");
            assertThat(saved.getDetails()).isEqualTo("Vibration levels exceeded threshold of 2.5mm/s RMS");
            assertThat(saved.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30, 45));
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedResult_whenFindById_thenReturnsResult() {
            SewThresholdBasedPredictiveMaintenanceResult saved = repository.save(sampleResult1);

            Optional<SewThresholdBasedPredictiveMaintenanceResult> found = repository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getRecommendation()).isEqualTo("Replace bearing in motor unit within 72 hours");
        }

        @Test
        @DisplayName("Delete result : Success")
        void givenSavedResult_whenDelete_thenRemovesResult() {
            SewThresholdBasedPredictiveMaintenanceResult saved = repository.save(sampleResult1);

            repository.delete(saved);

            Optional<SewThresholdBasedPredictiveMaintenanceResult> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("Find first by module ID : Success")
        void givenMultipleResults_whenFindFirstByModuleId_thenReturnsFirstResult() {
            SewThresholdBasedPredictiveMaintenanceResult result1 = new SewThresholdBasedPredictiveMaintenanceResult();
            result1.setModuleId("SHARED_MODULE");
            result1.setSmartServiceId("SERVICE_1");
            result1.setRecommendation("First recommendation");
            result1.setTimestamp(LocalDateTime.now().minusHours(2));

            SewThresholdBasedPredictiveMaintenanceResult result2 = new SewThresholdBasedPredictiveMaintenanceResult();
            result2.setModuleId("SHARED_MODULE");
            result2.setSmartServiceId("SERVICE_2");
            result2.setRecommendation("Second recommendation");
            result2.setTimestamp(LocalDateTime.now().minusHours(1));

            repository.saveAll(Arrays.asList(result1, result2));

            Optional<SewThresholdBasedPredictiveMaintenanceResult> found = repository.findFirstByModuleId("SHARED_MODULE");

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("SHARED_MODULE");
            assertThat(found.get().getRecommendation()).isIn("First recommendation", "Second recommendation");
        }

        @Test
        @DisplayName("Find first by module ID : No results")
        void givenNoMatchingResults_whenFindFirstByModuleId_thenReturnsEmpty() {
            repository.save(sampleResult1);

            Optional<SewThresholdBasedPredictiveMaintenanceResult> found = repository.findFirstByModuleId("NON_EXISTENT_MODULE");

            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find first by module ID : Single result")
        void givenSingleResult_whenFindFirstByModuleId_thenReturnsResult() {
            repository.save(sampleResult1);

            Optional<SewThresholdBasedPredictiveMaintenanceResult> found = repository.findFirstByModuleId("TEST_MODULE_1");

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getRecommendation()).isEqualTo("Replace bearing in motor unit within 72 hours");
        }
    }

    @Nested
    @DisplayName("Data Integrity and Edge Cases")
    class DataIntegrityAndEdgeCases {

        @Test
        @DisplayName("Save result with null optional fields : Success")
        void givenResultWithNullOptionalFields_whenSave_thenPersistsSuccessfully() {
            SewThresholdBasedPredictiveMaintenanceResult resultWithNulls = new SewThresholdBasedPredictiveMaintenanceResult();
            resultWithNulls.setModuleId("TEST_MODULE");
            resultWithNulls.setSmartServiceId("THRESHOLD_SERVICE");
            resultWithNulls.setRecommendation(null);
            resultWithNulls.setDetails(null);
            resultWithNulls.setTimestamp(LocalDateTime.now());

            SewThresholdBasedPredictiveMaintenanceResult saved = repository.save(resultWithNulls);

            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE");
            assertThat(saved.getRecommendation()).isNull();
            assertThat(saved.getDetails()).isNull();
        }

        @Test
        @DisplayName("Update existing result : Success")
        void givenExistingResult_whenUpdate_thenUpdatesSuccessfully() {
            SewThresholdBasedPredictiveMaintenanceResult saved = repository.save(sampleResult1);
            String originalId = saved.getId();

            saved.setRecommendation("Updated recommendation");
            saved.setDetails("Updated details");
            SewThresholdBasedPredictiveMaintenanceResult updated = repository.save(saved);

            assertThat(updated.getId()).isEqualTo(originalId);
            assertThat(updated.getRecommendation()).isEqualTo("Updated recommendation");
            assertThat(updated.getDetails()).isEqualTo("Updated details");
            assertThat(updated.getModuleId()).isEqualTo("TEST_MODULE_1"); // Unchanged
        }

        @Test
        @DisplayName("Save result with long text fields : Success")
        void givenResultWithLongTextFields_whenSave_thenPersistsSuccessfully() {
            String longRecommendation = "A".repeat(1000);
            String longDetails = "B".repeat(2000);

            SewThresholdBasedPredictiveMaintenanceResult longResult = new SewThresholdBasedPredictiveMaintenanceResult();
            longResult.setModuleId("LONG_TEXT_MODULE");
            longResult.setSmartServiceId("THRESHOLD_SERVICE");
            longResult.setRecommendation(longRecommendation);
            longResult.setDetails(longDetails);
            longResult.setTimestamp(LocalDateTime.now());

            SewThresholdBasedPredictiveMaintenanceResult saved = repository.save(longResult);

            assertThat(saved).isNotNull();
            assertThat(saved.getRecommendation()).hasSize(1000);
            assertThat(saved.getDetails()).hasSize(2000);
        }

        @Test
        @DisplayName("Find by module ID with special characters : Success")
        void givenModuleIdWithSpecialCharacters_whenFindByModuleId_thenFindsCorrectly() {
            SewThresholdBasedPredictiveMaintenanceResult specialResult = new SewThresholdBasedPredictiveMaintenanceResult();
            specialResult.setModuleId("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
            specialResult.setSmartServiceId("THRESHOLD_SERVICE");
            specialResult.setRecommendation("Special characters test");
            specialResult.setTimestamp(LocalDateTime.now());

            repository.save(specialResult);

            Optional<SewThresholdBasedPredictiveMaintenanceResult> found = 
                    repository.findFirstByModuleId("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");

            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("MODULE@#$%^&*()_+-=[]{}|;':\",./<>?");
            assertThat(found.get().getRecommendation()).isEqualTo("Special characters test");
        }

        @Test
        @DisplayName("Save multiple results for same module : Success")
        void givenMultipleResultsForSameModule_whenSave_thenAllPersistSuccessfully() {
            SewThresholdBasedPredictiveMaintenanceResult result1 = new SewThresholdBasedPredictiveMaintenanceResult();
            result1.setModuleId("SHARED_MODULE");
            result1.setSmartServiceId("SERVICE_1");
            result1.setRecommendation("First result");
            result1.setTimestamp(LocalDateTime.now().minusHours(2));

            SewThresholdBasedPredictiveMaintenanceResult result2 = new SewThresholdBasedPredictiveMaintenanceResult();
            result2.setModuleId("SHARED_MODULE");
            result2.setSmartServiceId("SERVICE_2");
            result2.setRecommendation("Second result");
            result2.setTimestamp(LocalDateTime.now().minusHours(1));

            repository.saveAll(Arrays.asList(result1, result2));

            Optional<SewThresholdBasedPredictiveMaintenanceResult> found = repository.findFirstByModuleId("SHARED_MODULE");
            assertThat(found).isPresent();

            long count = repository.count();
            assertThat(count).isEqualTo(2);
        }
    }
}
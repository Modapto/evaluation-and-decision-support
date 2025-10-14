package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessRealTimeMonitoringResults;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataElasticsearchTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("SewSelfAwarenessRealTimeMonitoringResultsRepository Tests")
class SewSelfAwarenessRealTimeMonitoringResultsRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private SewSelfAwarenessRealTimeMonitoringResultsRepository realTimeMonitoringResultsRepository;

    private SewSelfAwarenessRealTimeMonitoringResults testResult;
    private SewSelfAwarenessRealTimeMonitoringResults anotherResult;
    private SewSelfAwarenessRealTimeMonitoringResults differentModuleResult;

    @BeforeEach
    void setUp() {
        realTimeMonitoringResultsRepository.deleteAll();

        testResult = SewSelfAwarenessRealTimeMonitoringResults.builder()
                .id("TEST_RESULT_1")
                .timestamp(LocalDateTime.now())
                .module("TEST_MODULE")
                .moduleId("TEST_MODULE")
                .component("COMPONENT_1")
                .highThreshold(20.0)
                .lowThreshold(3.0)
                .deviationPercentage(20.2)
                .value("Test_Temp")
                .property("TEMPERATURE")
                .build();

        anotherResult = SewSelfAwarenessRealTimeMonitoringResults.builder()
                .id("TEST_RESULT_2")
                .timestamp(LocalDateTime.now().plusHours(1))
                .module("TEST_MODULE")
                .moduleId("TEST_MODULE")
                .component("COMPONENT_2")
                .property("PRESSURE")
                .value("10.5")
                .highThreshold(15.0)
                .lowThreshold(5.0)
                .deviationPercentage(10.0)
                .build();

        differentModuleResult = SewSelfAwarenessRealTimeMonitoringResults.builder()
                .id("TEST_RESULT_3")
                .timestamp(LocalDateTime.now().plusHours(2))
                .module("DIFFERENT_MODULE")
                .moduleId("DIFFERENT_MODULE")
                .component("COMPONENT_3")
                .property("VIBRATION")
                .value("0.15")
                .highThreshold(0.3)
                .lowThreshold(0.0)
                .deviationPercentage(5.0)
                .build();
    }

    @Nested
    @DisplayName("When saving and retrieving results")
    class SavingAndRetrieving {

        @Test
        @DisplayName("Save result : Success")
        void givenValidResult_whenSave_thenReturnSavedResult() {
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(testResult);

            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("TEST_MODULE", saved.getModuleId());
            assertEquals("TEMPERATURE", saved.getProperty());
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedResult_whenFindById_thenReturnResult() {
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(testResult);

            Optional<SewSelfAwarenessRealTimeMonitoringResults> found = realTimeMonitoringResultsRepository.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals("TEST_MODULE", found.get().getModuleId());
            assertEquals("COMPONENT_1", found.get().getComponent());
        }

        @Test
        @DisplayName("Find by non-existent ID : Return empty")
        void givenNonExistentId_whenFindById_thenReturnEmpty() {
            Optional<SewSelfAwarenessRealTimeMonitoringResults> found = realTimeMonitoringResultsRepository.findById("NON_EXISTENT_ID");

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find all : Return all saved results")
        void givenMultipleResults_whenFindAll_thenReturnAllResults() {
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            Iterable<SewSelfAwarenessRealTimeMonitoringResults> allResults = realTimeMonitoringResultsRepository.findAll();

            assertNotNull(allResults);
            List<SewSelfAwarenessRealTimeMonitoringResults> resultList = new ArrayList<>();
            allResults.forEach(resultList::add);
            assertEquals(3, resultList.size());
        }
    }

    @Nested
    @DisplayName("When finding by module ID with pagination")
    class FindByModuleIdWithPagination {

        @Test
        @DisplayName("Find by module ID with pagination : Success")
        void givenSavedResults_whenFindByModuleIdWithPagination_thenReturnPagedResults() {
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            Pageable pageable = PageRequest.of(0, 10);

            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);

            assertNotNull(results);
            assertEquals(2, results.getContent().size());
            assertEquals(2, results.getTotalElements());
            assertTrue(results.getContent().stream().allMatch(result -> "TEST_MODULE".equals(result.getModuleId())));
        }

        @Test
        @DisplayName("Find by module ID with limited page size : Return correct page")
        void givenMultipleResults_whenFindByModuleIdWithLimitedPageSize_thenReturnCorrectPage() {
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            Pageable pageable = PageRequest.of(0, 1);

            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);

            assertNotNull(results);
            assertEquals(1, results.getContent().size());
            assertEquals(2, results.getTotalElements());
            assertEquals(2, results.getTotalPages());
            assertTrue(results.hasNext());
        }

        @Test
        @DisplayName("Find by module ID second page : Return remaining results")
        void givenMultipleResults_whenFindByModuleIdSecondPage_thenReturnRemainingResults() {
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            Pageable pageable = PageRequest.of(1, 1);

            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);

            assertNotNull(results);
            assertEquals(1, results.getContent().size());
            assertEquals(2, results.getTotalElements());
            assertFalse(results.hasNext());
            assertTrue(results.hasPrevious());
        }

        @Test
        @DisplayName("Find by non-existent module ID : Return empty page")
        void givenNonExistentModuleId_whenFindByModuleId_thenReturnEmptyPage() {
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);

            Pageable pageable = PageRequest.of(0, 10);

            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("NON_EXISTENT_MODULE", pageable);

            assertNotNull(results);
            assertTrue(results.getContent().isEmpty());
            assertEquals(0, results.getTotalElements());
        }

        @Test
        @DisplayName("Find by null module ID : Return empty page")
        void givenNullModuleId_whenFindByModuleId_thenReturnEmptyPage() {
            realTimeMonitoringResultsRepository.save(testResult);

            Pageable pageable = PageRequest.of(0, 10);

            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId(null, pageable);

            assertNotNull(results);
            assertTrue(results.getContent().isEmpty());
        }

        @Test
        @DisplayName("Find by empty module ID : Return empty page")
        void givenEmptyModuleId_whenFindByModuleId_thenReturnEmptyPage() {
            realTimeMonitoringResultsRepository.save(testResult);

            Pageable pageable = PageRequest.of(0, 10);

            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("", pageable);

            assertNotNull(results);
            assertTrue(results.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("When handling complex data structures")
    class ComplexDataStructures {

        @Test
        @DisplayName("Save result with large data array : Success")
        void givenResultWithLargeDataArray_whenSave_thenPreserveAllData() {
            List<Double> largeDataArray = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeDataArray.add(Math.random() * 100);
            }

            SewSelfAwarenessRealTimeMonitoringResults resultWithLargeData = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("LARGE_DATA_MODULE")
                    .module("LARGE_MODULE")
                    .timestamp(LocalDateTime.now())
                    .component("LARGE_COMPONENT")
                    .property("LARGE_PROPERTY")
                    .value("LARGE_VALUE")
                    .highThreshold(100.0)
                    .lowThreshold(0.0)
                    .deviationPercentage(15.0)
                    .build();

            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithLargeData);

            assertNotNull(saved);
            assertEquals("LARGE_DATA_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Save result with empty data array : Success")
        void givenResultWithEmptyDataArray_whenSave_thenSaveSuccessfully() {
            SewSelfAwarenessRealTimeMonitoringResults resultWithEmptyData = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("EMPTY_DATA_MODULE")
                    .module("EMPTY_MODULE")
                    .timestamp(LocalDateTime.now())
                    .component("EMPTY_COMPONENT")
                    .property("EMPTY_PROPERTY")
                    .value("0")
                    .highThreshold(10.0)
                    .lowThreshold(0.0)
                    .deviationPercentage(0.0)
                    .build();

            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithEmptyData);

            assertNotNull(saved);
            assertEquals("EMPTY_DATA_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Save result with null data array : Success")
        void givenResultWithNullDataArray_whenSave_thenSaveSuccessfully() {
            SewSelfAwarenessRealTimeMonitoringResults resultWithNullData = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("NULL_DATA_MODULE")
                    .module("NULL_MODULE")
                    .timestamp(LocalDateTime.now())
                    .component("NULL_COMPONENT")
                    .property("NULL_PROPERTY")
                    .value(null)
                    .highThreshold(null)
                    .lowThreshold(null)
                    .deviationPercentage(null)
                    .build();

            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithNullData);

            assertNotNull(saved);
            assertNull(saved.getValue());
            assertEquals("NULL_DATA_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Save result with special characters in fields : Success")
        void givenResultWithSpecialCharacters_whenSave_thenPreserveSpecialCharacters() {
            SewSelfAwarenessRealTimeMonitoringResults resultWithSpecialChars = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("MODULE_WITH_SPECIAL_CHARS_@#$%")
                    .module("MODULE-WITH-DASHES")
                    .timestamp(LocalDateTime.now())
                    .component("COMPONENT WITH SPACES")
                    .property("PROPERTY_WITH_UNDERSCORES")
                    .value("1.5")
                    .highThreshold(5.0)
                    .lowThreshold(0.5)
                    .deviationPercentage(10.0)
                    .build();

            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithSpecialChars);

            assertNotNull(saved);
            assertEquals("MODULE_WITH_SPECIAL_CHARS_@#$%", saved.getModuleId());
            assertEquals("MODULE-WITH-DASHES", saved.getModule());
            assertEquals("COMPONENT WITH SPACES", saved.getComponent());
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Save result with null ID : Generate ID automatically")
        void givenResultWithNullId_whenSave_thenGenerateIdAutomatically() {
            SewSelfAwarenessRealTimeMonitoringResults resultWithoutId = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .id(null)
                    .moduleId("AUTO_ID_MODULE")
                    .module("AUTO_MODULE")
                    .timestamp(LocalDateTime.now())
                    .component("AUTO_COMPONENT")
                    .property("AUTO_PROPERTY")
                    .value("1.5")
                    .highThreshold(5.0)
                    .lowThreshold(0.0)
                    .deviationPercentage(5.0)
                    .build();

            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithoutId);

            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("AUTO_ID_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Count results : Return correct count")
        void givenMultipleResults_whenCount_thenReturnCorrectCount() {
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            long count = realTimeMonitoringResultsRepository.count();

            assertEquals(3, count);
        }

        @Test
        @DisplayName("Delete all results : Remove all results")
        void givenMultipleResults_whenDeleteAll_thenRemoveAllResults() {
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            assertEquals(2, realTimeMonitoringResultsRepository.count());

            realTimeMonitoringResultsRepository.deleteAll();

            assertEquals(0, realTimeMonitoringResultsRepository.count());
        }

        @Test
        @DisplayName("Exists by ID : Return correct existence status")
        void givenSavedResult_whenExistsById_thenReturnTrue() {
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(testResult);

            boolean exists = realTimeMonitoringResultsRepository.existsById(saved.getId());

            assertTrue(exists);
        }

        @Test
        @DisplayName("Exists by non-existent ID : Return false")
        void givenNonExistentId_whenExistsById_thenReturnFalse() {
            boolean exists = realTimeMonitoringResultsRepository.existsById("NON_EXISTENT_ID");

            assertFalse(exists);
        }

        @Test
        @DisplayName("Save multiple results for same module : All saved correctly")
        void givenMultipleResultsForSameModule_whenSaveAll_thenAllSavedCorrectly() {
            List<SewSelfAwarenessRealTimeMonitoringResults> results = List.of(testResult, anotherResult);

            Iterable<SewSelfAwarenessRealTimeMonitoringResults> savedResults = realTimeMonitoringResultsRepository.saveAll(results);

            assertNotNull(savedResults);
            List<SewSelfAwarenessRealTimeMonitoringResults> savedList = new ArrayList<>();
            savedResults.forEach(savedList::add);
            assertEquals(2, savedList.size());

            Pageable pageable = PageRequest.of(0, 10);
            Page<SewSelfAwarenessRealTimeMonitoringResults> foundResults = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);
            assertEquals(2, foundResults.getTotalElements());
        }
    }
}
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
                .smartServiceId("SERVICE_1")
                .moduleId("TEST_MODULE")
                .ligne("LINE_1")
                .component("COMPONENT_1")
                .variable("TEMPERATURE")
                .startingDate("2025-01-01T00:00:00")
                .endingDate("2025-01-01T23:59:59")
                .dataSource("SENSOR_1")
                .bucket("BUCKET_1")
                .data(List.of(25.5, 26.0, 25.8, 26.2))
                .build();

        anotherResult = SewSelfAwarenessRealTimeMonitoringResults.builder()
                .id("TEST_RESULT_2")
                .timestamp(LocalDateTime.now().plusHours(1))
                .smartServiceId("SERVICE_2")
                .moduleId("TEST_MODULE")
                .ligne("LINE_2")
                .component("COMPONENT_2")
                .variable("PRESSURE")
                .startingDate("2025-01-02T00:00:00")
                .endingDate("2025-01-02T23:59:59")
                .dataSource("SENSOR_2")
                .bucket("BUCKET_2")
                .data(List.of(10.1, 10.5, 9.8, 10.3))
                .build();

        differentModuleResult = SewSelfAwarenessRealTimeMonitoringResults.builder()
                .id("TEST_RESULT_3")
                .timestamp(LocalDateTime.now().plusHours(2))
                .smartServiceId("SERVICE_3")
                .moduleId("DIFFERENT_MODULE")
                .ligne("LINE_3")
                .component("COMPONENT_3")
                .variable("VIBRATION")
                .startingDate("2025-01-03T00:00:00")
                .endingDate("2025-01-03T23:59:59")
                .dataSource("SENSOR_3")
                .bucket("BUCKET_3")
                .data(List.of(0.1, 0.2, 0.15, 0.18))
                .build();
    }

    @Nested
    @DisplayName("When saving and retrieving results")
    class SavingAndRetrieving {

        @Test
        @DisplayName("Save result : Success")
        void givenValidResult_whenSave_thenReturnSavedResult() {
            // When
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(testResult);

            // Then
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("TEST_MODULE", saved.getModuleId());
            assertEquals("TEMPERATURE", saved.getVariable());
            assertEquals(4, saved.getData().size());
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedResult_whenFindById_thenReturnResult() {
            // Given
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(testResult);

            // When
            Optional<SewSelfAwarenessRealTimeMonitoringResults> found = realTimeMonitoringResultsRepository.findById(saved.getId());

            // Then
            assertTrue(found.isPresent());
            assertEquals("TEST_MODULE", found.get().getModuleId());
            assertEquals("COMPONENT_1", found.get().getComponent());
        }

        @Test
        @DisplayName("Find by non-existent ID : Return empty")
        void givenNonExistentId_whenFindById_thenReturnEmpty() {
            // When
            Optional<SewSelfAwarenessRealTimeMonitoringResults> found = realTimeMonitoringResultsRepository.findById("NON_EXISTENT_ID");

            // Then
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find all : Return all saved results")
        void givenMultipleResults_whenFindAll_thenReturnAllResults() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            // When
            Iterable<SewSelfAwarenessRealTimeMonitoringResults> allResults = realTimeMonitoringResultsRepository.findAll();

            // Then
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
            // Given
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);

            // Then
            assertNotNull(results);
            assertEquals(2, results.getContent().size());
            assertEquals(2, results.getTotalElements());
            assertTrue(results.getContent().stream().allMatch(result -> "TEST_MODULE".equals(result.getModuleId())));
        }

        @Test
        @DisplayName("Find by module ID with limited page size : Return correct page")
        void givenMultipleResults_whenFindByModuleIdWithLimitedPageSize_thenReturnCorrectPage() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            Pageable pageable = PageRequest.of(0, 1);

            // When
            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);

            // Then
            assertNotNull(results);
            assertEquals(1, results.getContent().size());
            assertEquals(2, results.getTotalElements());
            assertEquals(2, results.getTotalPages());
            assertTrue(results.hasNext());
        }

        @Test
        @DisplayName("Find by module ID second page : Return remaining results")
        void givenMultipleResults_whenFindByModuleIdSecondPage_thenReturnRemainingResults() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            Pageable pageable = PageRequest.of(1, 1);

            // When
            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);

            // Then
            assertNotNull(results);
            assertEquals(1, results.getContent().size());
            assertEquals(2, results.getTotalElements());
            assertFalse(results.hasNext());
            assertTrue(results.hasPrevious());
        }

        @Test
        @DisplayName("Find by non-existent module ID : Return empty page")
        void givenNonExistentModuleId_whenFindByModuleId_thenReturnEmptyPage() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("NON_EXISTENT_MODULE", pageable);

            // Then
            assertNotNull(results);
            assertTrue(results.getContent().isEmpty());
            assertEquals(0, results.getTotalElements());
        }

        @Test
        @DisplayName("Find by null module ID : Return empty page")
        void givenNullModuleId_whenFindByModuleId_thenReturnEmptyPage() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId(null, pageable);

            // Then
            assertNotNull(results);
            assertTrue(results.getContent().isEmpty());
        }

        @Test
        @DisplayName("Find by empty module ID : Return empty page")
        void givenEmptyModuleId_whenFindByModuleId_thenReturnEmptyPage() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<SewSelfAwarenessRealTimeMonitoringResults> results = realTimeMonitoringResultsRepository.findByModuleId("", pageable);

            // Then
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
            // Given
            List<Double> largeDataArray = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                largeDataArray.add(Math.random() * 100);
            }

            SewSelfAwarenessRealTimeMonitoringResults resultWithLargeData = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("LARGE_DATA_MODULE")
                    .timestamp(LocalDateTime.now())
                    .smartServiceId("LARGE_SERVICE")
                    .ligne("LARGE_LINE")
                    .component("LARGE_COMPONENT")
                    .variable("LARGE_VARIABLE")
                    .data(largeDataArray)
                    .build();

            // When
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithLargeData);

            // Then
            assertNotNull(saved);
            assertEquals(1000, saved.getData().size());
            assertEquals("LARGE_DATA_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Save result with empty data array : Success")
        void givenResultWithEmptyDataArray_whenSave_thenSaveSuccessfully() {
            // Given
            SewSelfAwarenessRealTimeMonitoringResults resultWithEmptyData = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("EMPTY_DATA_MODULE")
                    .timestamp(LocalDateTime.now())
                    .smartServiceId("EMPTY_SERVICE")
                    .data(new ArrayList<>())
                    .build();

            // When
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithEmptyData);

            // Then
            assertNotNull(saved);
            assertTrue(saved.getData().isEmpty());
            assertEquals("EMPTY_DATA_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Save result with null data array : Success")
        void givenResultWithNullDataArray_whenSave_thenSaveSuccessfully() {
            // Given
            SewSelfAwarenessRealTimeMonitoringResults resultWithNullData = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("NULL_DATA_MODULE")
                    .timestamp(LocalDateTime.now())
                    .smartServiceId("NULL_SERVICE")
                    .data(null)
                    .build();

            // When
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithNullData);

            // Then
            assertNotNull(saved);
            assertNull(saved.getData());
            assertEquals("NULL_DATA_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Save result with special characters in fields : Success")
        void givenResultWithSpecialCharacters_whenSave_thenPreserveSpecialCharacters() {
            // Given
            SewSelfAwarenessRealTimeMonitoringResults resultWithSpecialChars = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .moduleId("MODULE_WITH_SPECIAL_CHARS_@#$%")
                    .timestamp(LocalDateTime.now())
                    .smartServiceId("SERVICE-WITH-DASHES")
                    .ligne("LINE/WITH/SLASHES")
                    .component("COMPONENT WITH SPACES")
                    .variable("VARIABLE_WITH_UNDERSCORES")
                    .dataSource("SOURCE.WITH.DOTS")
                    .bucket("BUCKET&WITH&AMPERSANDS")
                    .data(List.of(1.1, 2.2, 3.3))
                    .build();

            // When
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithSpecialChars);

            // Then
            assertNotNull(saved);
            assertEquals("MODULE_WITH_SPECIAL_CHARS_@#$%", saved.getModuleId());
            assertEquals("SERVICE-WITH-DASHES", saved.getSmartServiceId());
            assertEquals("LINE/WITH/SLASHES", saved.getLigne());
            assertEquals("COMPONENT WITH SPACES", saved.getComponent());
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Save result with null ID : Generate ID automatically")
        void givenResultWithNullId_whenSave_thenGenerateIdAutomatically() {
            // Given
            SewSelfAwarenessRealTimeMonitoringResults resultWithoutId = SewSelfAwarenessRealTimeMonitoringResults.builder()
                    .id(null)
                    .moduleId("AUTO_ID_MODULE")
                    .timestamp(LocalDateTime.now())
                    .smartServiceId("AUTO_SERVICE")
                    .data(List.of(1.0, 2.0))
                    .build();

            // When
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(resultWithoutId);

            // Then
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("AUTO_ID_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Count results : Return correct count")
        void givenMultipleResults_whenCount_thenReturnCorrectCount() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            realTimeMonitoringResultsRepository.save(differentModuleResult);

            // When
            long count = realTimeMonitoringResultsRepository.count();

            // Then
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Delete all results : Remove all results")
        void givenMultipleResults_whenDeleteAll_thenRemoveAllResults() {
            // Given
            realTimeMonitoringResultsRepository.save(testResult);
            realTimeMonitoringResultsRepository.save(anotherResult);
            assertEquals(2, realTimeMonitoringResultsRepository.count());

            // When
            realTimeMonitoringResultsRepository.deleteAll();

            // Then
            assertEquals(0, realTimeMonitoringResultsRepository.count());
        }

        @Test
        @DisplayName("Exists by ID : Return correct existence status")
        void givenSavedResult_whenExistsById_thenReturnTrue() {
            // Given
            SewSelfAwarenessRealTimeMonitoringResults saved = realTimeMonitoringResultsRepository.save(testResult);

            // When
            boolean exists = realTimeMonitoringResultsRepository.existsById(saved.getId());

            // Then
            assertTrue(exists);
        }

        @Test
        @DisplayName("Exists by non-existent ID : Return false")
        void givenNonExistentId_whenExistsById_thenReturnFalse() {
            // When
            boolean exists = realTimeMonitoringResultsRepository.existsById("NON_EXISTENT_ID");

            // Then
            assertFalse(exists);
        }

        @Test
        @DisplayName("Save multiple results for same module : All saved correctly")
        void givenMultipleResultsForSameModule_whenSaveAll_thenAllSavedCorrectly() {
            // Given
            List<SewSelfAwarenessRealTimeMonitoringResults> results = List.of(testResult, anotherResult);

            // When
            Iterable<SewSelfAwarenessRealTimeMonitoringResults> savedResults = realTimeMonitoringResultsRepository.saveAll(results);

            // Then
            assertNotNull(savedResults);
            List<SewSelfAwarenessRealTimeMonitoringResults> savedList = new ArrayList<>();
            savedResults.forEach(savedList::add);
            assertEquals(2, savedList.size());

            // Verify both results can be found by module ID
            Pageable pageable = PageRequest.of(0, 10);
            Page<SewSelfAwarenessRealTimeMonitoringResults> foundResults = realTimeMonitoringResultsRepository.findByModuleId("TEST_MODULE", pageable);
            assertEquals(2, foundResults.getTotalElements());
        }
    }
}
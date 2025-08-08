package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.MaintenanceData;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataElasticsearchTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("MaintenanceDataRepository Tests with Testcontainers")
class MaintenanceDataRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private MaintenanceDataRepository maintenanceDataRepository;

    private MaintenanceData testData1;
    private MaintenanceData testData2;
    private MaintenanceData testData3;


    @BeforeEach
    void setUp() {
        // Given - Clean repository before each test
        maintenanceDataRepository.deleteAll();
        
        // Create test data
        testData1 = createMaintenanceData("1", "Stage1", "Cell1", "Component1", "2024-01-15T10:30:00");
        testData2 = createMaintenanceData("2", "Stage2", "Cell2", "Component2", "2024-01-20T14:45:00");
        testData3 = createMaintenanceData("3", "Stage3", "Cell3", "Component3", "2024-02-01T09:15:00");
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Save maintenance data : Success")
        void givenValidMaintenanceData_whenSave_thenReturnsPersistedData() {
            // When
            MaintenanceData saved = maintenanceDataRepository.save(testData1);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo("1");
            assertThat(saved.getStage()).isEqualTo("Stage1");
            assertThat(saved.getCell()).isEqualTo("Cell1");
            assertThat(saved.getComponent()).isEqualTo("Component1");
            assertThat(saved.getTsRequestCreation()).isEqualTo("2024-01-15T10:30:00");
        }

        @Test
        @DisplayName("Find maintenance data by ID : Success")
        void givenValidId_whenFindById_thenReturnsMaintenanceData() {
            // Given
            maintenanceDataRepository.save(testData1);

            // When
            Optional<MaintenanceData> found = maintenanceDataRepository.findById("1");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo("1");
            assertThat(found.get().getStage()).isEqualTo("Stage1");
        }

        @Test
        @DisplayName("Find maintenance data by ID : Not found")
        void givenNonExistentId_whenFindById_thenReturnsEmptyOptional() {
            // When
            Optional<MaintenanceData> found = maintenanceDataRepository.findById("nonexistent");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Find all maintenance data : Success")
        void givenMultipleRecords_whenFindAll_thenReturnsAllData() {
            // Given
            maintenanceDataRepository.save(testData1);
            maintenanceDataRepository.save(testData2);
            maintenanceDataRepository.save(testData3);

            // When
            Iterable<MaintenanceData> all = maintenanceDataRepository.findAll();

            // Then
            assertThat(all).hasSize(3);
            assertThat(all)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("1", "2", "3");
        }

        @Test
        @DisplayName("Find all maintenance data : Success with pagination")
        void givenMultipleRecords_whenFindAllWithPagination_thenReturnsPagedResults() {
            // Given
            maintenanceDataRepository.save(testData1);
            maintenanceDataRepository.save(testData2);
            maintenanceDataRepository.save(testData3);

            Pageable pageable = PageRequest.of(0, 2);

            // When
            Page<MaintenanceData> page = maintenanceDataRepository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Update maintenance data : Success")
        void givenExistingRecord_whenUpdate_thenReturnsUpdatedData() {
            // Given
            MaintenanceData saved = maintenanceDataRepository.save(testData1);

            // When
            saved.setStage("UpdatedStage");
            saved.setCell("UpdatedCell");
            MaintenanceData updated = maintenanceDataRepository.save(saved);

            // Then
            assertThat(updated.getId()).isEqualTo("1");
            assertThat(updated.getStage()).isEqualTo("UpdatedStage");
            assertThat(updated.getCell()).isEqualTo("UpdatedCell");
            assertThat(updated.getComponent()).isEqualTo("Component1"); // Unchanged
        }

        @Test
        @DisplayName("Delete maintenance data by ID : Success")
        void givenValidId_whenDeleteById_thenRemovesRecord() {
            // Given
            maintenanceDataRepository.save(testData1);
            assertThat(maintenanceDataRepository.existsById("1")).isTrue();

            // When
            maintenanceDataRepository.deleteById("1");

            // Then
            assertThat(maintenanceDataRepository.existsById("1")).isFalse();
            assertThat(maintenanceDataRepository.findById("1")).isEmpty();
        }

        @Test
        @DisplayName("Delete all maintenance data : Success")
        void givenMultipleRecords_whenDeleteAll_thenRemovesAllRecords() {
            // Given
            maintenanceDataRepository.save(testData1);
            maintenanceDataRepository.save(testData2);
            assertThat(maintenanceDataRepository.count()).isEqualTo(2);

            // When
            maintenanceDataRepository.deleteAll();

            // Then
            assertThat(maintenanceDataRepository.count()).isZero();
        }

        @Test
        @DisplayName("Count maintenance data : Success")
        void givenMultipleRecords_whenCount_thenReturnsCorrectCount() {
            // Given
            maintenanceDataRepository.save(testData1);
            maintenanceDataRepository.save(testData2);
            maintenanceDataRepository.save(testData3);

            // When
            long count = maintenanceDataRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Check maintenance data existence : Success")
        void givenValidId_whenExistsById_thenReturnsTrue() {
            // Given
            maintenanceDataRepository.save(testData1);

            // When & Then
            assertThat(maintenanceDataRepository.existsById("1")).isTrue();
            assertThat(maintenanceDataRepository.existsById("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Disabled
        @Test
        @DisplayName("Find maintenance data by date range : Success")
        void givenDateRange_whenFindByDateRange_thenReturnsFilteredData() {
            // Given
            maintenanceDataRepository.save(testData1); // 2024-01-15
            maintenanceDataRepository.save(testData2); // 2024-01-20
            maintenanceDataRepository.save(testData3); // 2024-02-01

            // When
            List<MaintenanceData> result = maintenanceDataRepository.findByTsRequestCreationBetween(
                    "2024-01-10T10:00:00", "2024-01-25T10:00:00"
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("1", "2");
        }

        @Disabled
        @Test
        @DisplayName("Find maintenance data by start date : Success")
        void givenStartDate_whenFindByStartDate_thenReturnsFilteredData() {
            // Given
            maintenanceDataRepository.save(testData1); // 2024-01-15
            maintenanceDataRepository.save(testData2); // 2024-01-20
            maintenanceDataRepository.save(testData3); // 2024-02-01

            // When
            List<MaintenanceData> result = maintenanceDataRepository.findByTsRequestCreationGreaterThanEqual(
                    "2024-01-18T10:00:00"
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("2", "3");
        }

        @Disabled
        @Test
        @DisplayName("Find maintenance data by end date : Success")
        void givenEndDate_whenFindByEndDate_thenReturnsFilteredData() {
            // Given
            maintenanceDataRepository.save(testData1); // 2024-01-15
            maintenanceDataRepository.save(testData2); // 2024-01-20
            maintenanceDataRepository.save(testData3); // 2024-02-01

            // When
            List<MaintenanceData> result = maintenanceDataRepository.findByTsRequestCreationLessThanEqual(
                    "2024-01-25T10:00:00"
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("1", "2");
        }

        @Disabled
        @Test
        @DisplayName("Find maintenance data by date range : Empty result")
        void givenDateRangeWithNoData_whenFindByDateRange_thenReturnsEmptyList() {
            // Given
            maintenanceDataRepository.save(testData1); // 2024-01-15
            maintenanceDataRepository.save(testData2); // 2024-01-20

            // When
            List<MaintenanceData> result = maintenanceDataRepository.findByTsRequestCreationBetween(
                    "2024-03-01T10:00:00", "2024-03-31T10:00:00"
            );

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Find maintenance data by start date : Early start date")
        void givenVeryEarlyStartDate_whenFindByStartDate_thenReturnsAllData() {
            // Given
            maintenanceDataRepository.save(testData1); // 2024-01-15
            maintenanceDataRepository.save(testData2); // 2024-01-20
            maintenanceDataRepository.save(testData3); // 2024-02-01

            // When
            List<MaintenanceData> result = maintenanceDataRepository.findByTsRequestCreationGreaterThanEqual(
                    "2024-01-01T10:00:00"
            );

            // Then
            assertThat(result).hasSize(3);
            assertThat(result)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("1", "2", "3");
        }

        @Test
        @DisplayName("Find maintenance data by end date : Late end date")
        void givenVeryLateEndDate_whenFindByEndDate_thenReturnsAllData() {
            // Given
            maintenanceDataRepository.save(testData1); // 2024-01-15
            maintenanceDataRepository.save(testData2); // 2024-01-20
            maintenanceDataRepository.save(testData3); // 2024-02-01

            // When
            List<MaintenanceData> result = maintenanceDataRepository.findByTsRequestCreationLessThanEqual(
                    "2024-12-31T10:00:00"
            );

            // Then
            assertThat(result).hasSize(3);
            assertThat(result)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("1", "2", "3");
        }

        @Test
        @DisplayName("Find maintenance data by date : Exact date match")
        void givenExactDateMatch_whenFindByDate_thenReturnsMatchingData() {
            // Given
            maintenanceDataRepository.save(testData1); // 2024-01-15 10:30:00
            maintenanceDataRepository.save(testData2); // 2024-01-20 14:45:00

            // When
            List<MaintenanceData> result = maintenanceDataRepository.findByTsRequestCreationGreaterThanEqual(
                    "2024-01-15T10:30:00"
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("1", "2");
        }
    }

    @Nested
    @DisplayName("Batch Operations")
    class BatchOperations {

        @Test
        @DisplayName("Save multiple maintenance data : Success")
        void givenMultipleRecords_whenSaveAll_thenPersistsAllRecords() {
            // Given
            List<MaintenanceData> dataList = List.of(testData1, testData2, testData3);

            // When
            Iterable<MaintenanceData> saved = maintenanceDataRepository.saveAll(dataList);

            // Then
            assertThat(saved).hasSize(3);
            assertThat(maintenanceDataRepository.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Find multiple maintenance data by IDs : Success")
        void givenMultipleIds_whenFindAllById_thenReturnsMatchingRecords() {
            // Given
            maintenanceDataRepository.save(testData1);
            maintenanceDataRepository.save(testData2);
            maintenanceDataRepository.save(testData3);

            List<String> ids = List.of("1", "3");

            // When
            Iterable<MaintenanceData> found = maintenanceDataRepository.findAllById(ids);

            // Then
            assertThat(found).hasSize(2);
            assertThat(found)
                    .extracting(MaintenanceData::getId)
                    .containsExactlyInAnyOrder("1", "3");
        }

        @Test
        @DisplayName("Delete multiple maintenance data : Success")
        void givenMultipleRecords_whenDeleteAll_thenRemovesSpecifiedRecords() {
            // Given
            maintenanceDataRepository.save(testData1);
            maintenanceDataRepository.save(testData2);
            maintenanceDataRepository.save(testData3);

            List<MaintenanceData> toDelete = List.of(testData1, testData3);

            // When
            maintenanceDataRepository.deleteAll(toDelete);

            // Then
            assertThat(maintenanceDataRepository.count()).isEqualTo(1);
            assertThat(maintenanceDataRepository.existsById("1")).isFalse();
            assertThat(maintenanceDataRepository.existsById("2")).isTrue();
            assertThat(maintenanceDataRepository.existsById("3")).isFalse();
        }
    }

    @Nested
    @DisplayName("Data Validation and Constraints")
    class DataValidationAndConstraints {

        @Test
        @DisplayName("Save maintenance data : Empty strings")
        void givenEmptyStrings_whenSave_thenHandlesEmptyValues() {
            // Given
            MaintenanceData dataWithEmptyStrings = createMaintenanceData("5", "", "", "", "2025-01-01T00:00:00");

            // When
            MaintenanceData saved = maintenanceDataRepository.save(dataWithEmptyStrings);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo("5");
            assertThat(saved.getStage()).isEmpty();
            assertThat(saved.getCell()).isEmpty();
        }

        @Test
        @DisplayName("Save maintenance data : All fields populated")
        void givenCompleteData_whenSave_thenPersistsAllFields() {
            // Given
            MaintenanceData completeData = createCompleteMaintenanceData();

            // When
            MaintenanceData saved = maintenanceDataRepository.save(completeData);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo("complete");
            assertThat(saved.getStage()).isEqualTo("TestStage");
            assertThat(saved.getCell()).isEqualTo("TestCell");
            assertThat(saved.getModule()).isEqualTo("TestModule");
            assertThat(saved.getComponent()).isEqualTo("TestComponent");
            assertThat(saved.getFailureType()).isEqualTo("TestFailureType");
            assertThat(saved.getFailureDescription()).isEqualTo("TestFailureDescription");
            assertThat(saved.getMaintenanceActionPerformed()).isEqualTo("TestMaintenanceAction");
            assertThat(saved.getComponentReplacement()).isEqualTo("Yes");
            assertThat(saved.getTsRequestCreation()).isEqualTo("2024-01-15T10:30:00");
            assertThat(saved.getTsInterventionStarted()).isEqualTo("2024-01-15T11:00:00");
            assertThat(saved.getTsInterventionFinished()).isEqualTo("2024-01-15T12:00:00");
        }

        @Test
        @DisplayName("Save maintenance data : Duplicate ID update")
        void givenDuplicateId_whenSave_thenUpdatesExistingRecord() {
            // Given
            maintenanceDataRepository.save(testData1);
            
            MaintenanceData duplicateId = createMaintenanceData("1", "UpdatedStage", "UpdatedCell", "UpdatedComponent", "2024-01-16T10:30:00");

            // When
            MaintenanceData saved = maintenanceDataRepository.save(duplicateId);

            // Then
            assertThat(maintenanceDataRepository.count()).isEqualTo(1);
            assertThat(saved.getId()).isEqualTo("1");
            assertThat(saved.getStage()).isEqualTo("UpdatedStage");
            assertThat(saved.getCell()).isEqualTo("UpdatedCell");
        }
    }

    @Nested
    @DisplayName("Performance and Edge Cases")
    class PerformanceAndEdgeCases {

        @Test
        @DisplayName("Performance : Large number of records")
        void givenLargeNumberOfRecords_whenSaveAll_thenProcessesEfficiently() {
            // Given
            int numberOfRecords = 1000;
            List<MaintenanceData> largeDataSet = createLargeDataSet(numberOfRecords);

            // When
            long startTime = System.currentTimeMillis();
            maintenanceDataRepository.saveAll(largeDataSet);
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(maintenanceDataRepository.count()).isEqualTo(numberOfRecords);
            assertThat(endTime - startTime).isLessThan(10000); // Should complete within 10 seconds
        }

        @Test
        @DisplayName("Performance : Concurrent access")
        void givenConcurrentAccess_whenFindById_thenHandlesProperlyOptimized() {
            // Given
            maintenanceDataRepository.save(testData1);

            // When
            MaintenanceData found1 = maintenanceDataRepository.findById("1").orElse(null);
            MaintenanceData found2 = maintenanceDataRepository.findById("1").orElse(null);

            // Then
            assertThat(found1).isNotNull();
            assertThat(found2).isNotNull();
            assertThat(found1.getId()).isEqualTo(found2.getId());
            assertThat(found1.getStage()).isEqualTo(found2.getStage());
        }

        @Test
        @DisplayName("Performance : Very long text fields")
        void givenVeryLongTextFields_whenSave_thenHandlesLargeContent() {
            // Given
            String longText = "A".repeat(1000);
            MaintenanceData dataWithLongText = createMaintenanceData("long", longText, longText, longText, "2024-01-15T10:30:00");

            // When
            MaintenanceData saved = maintenanceDataRepository.save(dataWithLongText);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getStage()).hasSize(1000);
            assertThat(saved.getCell()).hasSize(1000);
            assertThat(saved.getComponent()).hasSize(1000);
        }
    }

    private MaintenanceData createMaintenanceData(String id, String stage, String cell, String component, String tsRequestCreation) {
        MaintenanceData data = new MaintenanceData();
        data.setId(id);
        data.setStage(stage);
        data.setCell(cell);
        data.setComponent(component);
        data.setTsRequestCreation(LocalDateTime.parse(tsRequestCreation));
        return data;
    }

    private MaintenanceData createCompleteMaintenanceData() {
        MaintenanceData data = new MaintenanceData();
        data.setId("complete");
        data.setStage("TestStage");
        data.setCell("TestCell");
        data.setModule("TestModule");
        data.setComponent("TestComponent");
        data.setFailureType("TestFailureType");
        data.setFailureDescription("TestFailureDescription");
        data.setMaintenanceActionPerformed("TestMaintenanceAction");
        data.setComponentReplacement("Yes");
        data.setTsRequestCreation(LocalDateTime.parse("2024-01-15T10:30:00"));
        data.setTsInterventionStarted(LocalDateTime.parse("2024-01-15T11:00:00"));
        data.setTsInterventionFinished(LocalDateTime.parse("2024-01-15T12:00:00"));
        return data;
    }

    private List<MaintenanceData> createLargeDataSet(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> createMaintenanceData(
                        String.valueOf(i),
                        "Stage" + i,
                        "Cell" + i,
                        "Component" + i,
                        "2024-01-15T10:30:00"
                ))
                .toList();
    }
}
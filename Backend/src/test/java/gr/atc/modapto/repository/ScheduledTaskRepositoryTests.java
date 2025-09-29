package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.enums.FrequencyType;
import gr.atc.modapto.model.ScheduledTask;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataElasticsearchTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("ScheduledTaskRepository Tests")
class ScheduledTaskRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;

    private ScheduledTask sampleTask1;
    private ScheduledTask sampleTask2;
    private ScheduledTask sampleTask3;

    private static final String THRESHOLD_BASED_TYPE = "THRESHOLD_BASED_PREDICTIVE_MAINTENANCE";
    private static final String GROUPING_BASED_TYPE = "GROUPING_BASED_PREDICTIVE_MAINTENANCE";

    @BeforeEach
    void setUp() {
        // Clean repository before each test
        scheduledTaskRepository.deleteAll();

        SewThresholdBasedMaintenanceInputDataDto sampleThresholdData = SewThresholdBasedMaintenanceInputDataDto.builder()
                .moduleId("MODULE_1")
                .smartServiceId("THRESHOLD_SERVICE")
                .build();

        LocalDateTime now = LocalDateTime.now();

        
        sampleTask1 = new ScheduledTask();
        sampleTask1.setId("task-1");
        sampleTask1.setModuleId("MODULE_1");
        sampleTask1.setSmartServiceId("THRESHOLD_SERVICE_1");
        sampleTask1.setSmartServiceType(THRESHOLD_BASED_TYPE);
        sampleTask1.setFrequencyType(FrequencyType.HOURS);
        sampleTask1.setFrequencyValue(24);
        sampleTask1.setRequestBody(sampleThresholdData);
        sampleTask1.setCreatedAt(now);
        sampleTask1.setNextExecutionTime(now.plusHours(24));

        sampleTask2 = new ScheduledTask();
        sampleTask2.setId("task-2");
        sampleTask2.setModuleId("MODULE_2");
        sampleTask2.setSmartServiceId("THRESHOLD_SERVICE_2");
        sampleTask2.setSmartServiceType(THRESHOLD_BASED_TYPE);
        sampleTask2.setFrequencyType(FrequencyType.DAYS);
        sampleTask2.setFrequencyValue(7);
        sampleTask2.setRequestBody(sampleThresholdData);
        sampleTask2.setCreatedAt(now.minusHours(1));
        sampleTask2.setNextExecutionTime(now.plusDays(7));

        sampleTask3 = new ScheduledTask();
        sampleTask3.setId("task-3");
        sampleTask3.setModuleId("MODULE_3");
        sampleTask3.setSmartServiceId("GROUPING_SERVICE");
        sampleTask3.setSmartServiceType(GROUPING_BASED_TYPE);
        sampleTask3.setFrequencyType(FrequencyType.MINUTES);
        sampleTask3.setFrequencyValue(30);
        sampleTask3.setCreatedAt(now.minusHours(2));
        sampleTask3.setNextExecutionTime(now.plusMinutes(30));
    }

    @Nested
    @DisplayName("Find By Smart Service Type")
    class FindBySmartServiceType {

        @Test
        @DisplayName("Find by smart service type : Success with threshold-based tasks")
        void givenThresholdBasedTasks_whenFindBySmartServiceType_thenReturnsMatchingTasks() {
            scheduledTaskRepository.saveAll(List.of(sampleTask1, sampleTask2, sampleTask3));
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            Page<ScheduledTask> result = scheduledTaskRepository.findBySmartServiceType(THRESHOLD_BASED_TYPE, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(ScheduledTask::getSmartServiceType)
                    .allMatch(type -> type.equals(THRESHOLD_BASED_TYPE));
            assertThat(result.getContent())
                    .extracting(ScheduledTask::getId)
                    .containsExactlyInAnyOrder("task-1", "task-2");
        }

        @Test
        @DisplayName("Find by smart service type : Success with grouping-based tasks")
        void givenGroupingBasedTasks_whenFindBySmartServiceType_thenReturnsMatchingTasks() {
            scheduledTaskRepository.saveAll(List.of(sampleTask1, sampleTask2, sampleTask3));
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Pageable pageable = PageRequest.of(0, 10);

            Page<ScheduledTask> result = scheduledTaskRepository.findBySmartServiceType(GROUPING_BASED_TYPE, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getSmartServiceType()).isEqualTo(GROUPING_BASED_TYPE);
            assertThat(result.getContent().getFirst().getId()).isEqualTo("task-3");
        }

        @Test
        @DisplayName("Find by smart service type : No matching tasks")
        void givenNoMatchingTasks_whenFindBySmartServiceType_thenReturnsEmptyPage() {
            scheduledTaskRepository.saveAll(List.of(sampleTask1, sampleTask2, sampleTask3));
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Pageable pageable = PageRequest.of(0, 10);
            String nonExistentType = "NON_EXISTENT_SERVICE_TYPE";

            Page<ScheduledTask> result = scheduledTaskRepository.findBySmartServiceType(nonExistentType, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Find by smart service type : Empty repository")
        void givenEmptyRepository_whenFindBySmartServiceType_thenReturnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<ScheduledTask> result = scheduledTaskRepository.findBySmartServiceType(THRESHOLD_BASED_TYPE, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Find by smart service type : Pagination")
        void givenMultipleTasks_whenFindBySmartServiceTypeWithPagination_thenRespectsPageSize() {
            ScheduledTask additionalTask1 = new ScheduledTask();
            additionalTask1.setId("task-4");
            additionalTask1.setModuleId("MODULE_4");
            additionalTask1.setSmartServiceId("THRESHOLD_SERVICE_4");
            additionalTask1.setSmartServiceType(THRESHOLD_BASED_TYPE);
            additionalTask1.setFrequencyType(FrequencyType.HOURS);
            additionalTask1.setFrequencyValue(12);
            additionalTask1.setCreatedAt(LocalDateTime.now());
            additionalTask1.setNextExecutionTime(LocalDateTime.now().plusHours(12));

            ScheduledTask additionalTask2 = new ScheduledTask();
            additionalTask2.setId("task-5");
            additionalTask2.setModuleId("MODULE_5");
            additionalTask2.setSmartServiceId("THRESHOLD_SERVICE_5");
            additionalTask2.setSmartServiceType(THRESHOLD_BASED_TYPE);
            additionalTask2.setFrequencyType(FrequencyType.HOURS);
            additionalTask2.setFrequencyValue(6);
            additionalTask2.setCreatedAt(LocalDateTime.now());
            additionalTask2.setNextExecutionTime(LocalDateTime.now().plusHours(6));

            scheduledTaskRepository.saveAll(List.of(sampleTask1, sampleTask2, additionalTask1, additionalTask2));
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Pageable firstPage = PageRequest.of(0, 2);
            Pageable secondPage = PageRequest.of(1, 2);

            Page<ScheduledTask> firstPageResult = scheduledTaskRepository.findBySmartServiceType(THRESHOLD_BASED_TYPE, firstPage);
            Page<ScheduledTask> secondPageResult = scheduledTaskRepository.findBySmartServiceType(THRESHOLD_BASED_TYPE, secondPage);

            assertThat(firstPageResult.getContent()).hasSize(2);
            assertThat(secondPageResult.getContent()).hasSize(2);
            assertThat(firstPageResult.getTotalElements()).isEqualTo(4);
            assertThat(secondPageResult.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("Find by smart service type : Sorting")
        void givenMultipleTasks_whenFindBySmartServiceTypeWithSorting_thenReturnsSortedResults() {
            scheduledTaskRepository.saveAll(List.of(sampleTask1, sampleTask2, sampleTask3));
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Pageable pageableAsc = PageRequest.of(0, 10, Sort.by("createdAt").ascending());
            Pageable pageableDesc = PageRequest.of(0, 10, Sort.by("createdAt").descending());

            Page<ScheduledTask> ascendingResult = scheduledTaskRepository.findBySmartServiceType(THRESHOLD_BASED_TYPE, pageableAsc);
            Page<ScheduledTask> descendingResult = scheduledTaskRepository.findBySmartServiceType(THRESHOLD_BASED_TYPE, pageableDesc);

            assertThat(ascendingResult.getContent()).hasSize(2);
            assertThat(descendingResult.getContent()).hasSize(2);
            
            List<LocalDateTime> ascDates = ascendingResult.getContent().stream()
                    .map(ScheduledTask::getCreatedAt)
                    .toList();
            List<LocalDateTime> descDates = descendingResult.getContent().stream()
                    .map(ScheduledTask::getCreatedAt)
                    .toList();
            
            assertThat(ascDates).isSorted();
            assertThat(descDates).isSortedAccordingTo((a, b) -> b.compareTo(a)); // Reverse order
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Save scheduled task : Success")
        void givenValidScheduledTask_whenSave_thenPersistsSuccessfully() {
            ScheduledTask savedTask = scheduledTaskRepository.save(sampleTask1);

            assertThat(savedTask).isNotNull();
            assertThat(savedTask.getId()).isEqualTo("task-1");
            assertThat(savedTask.getModuleId()).isEqualTo("MODULE_1");
            assertThat(savedTask.getSmartServiceType()).isEqualTo(THRESHOLD_BASED_TYPE);
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenExistingTask_whenFindById_thenReturnsTask() {
            scheduledTaskRepository.save(sampleTask1);

            Optional<ScheduledTask> result = scheduledTaskRepository.findById("task-1");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("task-1");
            assertThat(result.get().getModuleId()).isEqualTo("MODULE_1");
            assertThat(result.get().getSmartServiceType()).isEqualTo(THRESHOLD_BASED_TYPE);
        }

        @Test
        @DisplayName("Find by ID : Not found")
        void givenNonExistentTask_whenFindById_thenReturnsEmpty() {
            Optional<ScheduledTask> result = scheduledTaskRepository.findById("non-existent-task");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Delete scheduled task : Success")
        void givenExistingTask_whenDelete_thenRemovesFromRepository() {
            scheduledTaskRepository.save(sampleTask1);
            assertThat(scheduledTaskRepository.findById("task-1")).isPresent();

            scheduledTaskRepository.deleteById("task-1");

            assertThat(scheduledTaskRepository.findById("task-1")).isEmpty();
        }

        @Test
        @DisplayName("Update scheduled task : Success")
        void givenExistingTask_whenUpdate_thenPersistsChanges() {
            ScheduledTask savedTask = scheduledTaskRepository.save(sampleTask1);
            
            savedTask.setFrequencyValue(12); // Change from 24 to 12 hours
            savedTask.setNextExecutionTime(LocalDateTime.now().plusHours(12));
            ScheduledTask updatedTask = scheduledTaskRepository.save(savedTask);

            assertThat(updatedTask.getFrequencyValue()).isEqualTo(12);
            
            Optional<ScheduledTask> retrievedTask = scheduledTaskRepository.findById("task-1");
            assertThat(retrievedTask).isPresent();
            assertThat(retrievedTask.get().getFrequencyValue()).isEqualTo(12);
        }

        @Test
        @Disabled
        @DisplayName("Find all : Success")
        void givenMultipleTasks_whenFindAll_thenReturnsAllTasks() {
            scheduledTaskRepository.saveAll(List.of(sampleTask1, sampleTask2, sampleTask3));
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Iterable<ScheduledTask> result = scheduledTaskRepository.findAll();

            List<ScheduledTask> tasks = (List<ScheduledTask>) result;
            assertThat(tasks).hasSize(3);
            assertThat(tasks).extracting(ScheduledTask::getId)
                    .containsExactlyInAnyOrder("task-1", "task-2", "task-3");
        }

        @Test
        @DisplayName("Count : Success")
        void givenMultipleTasks_whenCount_thenReturnsCorrectCount() {
            scheduledTaskRepository.saveAll(List.of(sampleTask1, sampleTask2, sampleTask3));
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long count = scheduledTaskRepository.count();

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Data Integrity and Edge Cases")
    class DataIntegrityAndEdgeCases {

        @Test
        @DisplayName("Save task with complex threshold data : Success")
        void givenTaskWithComplexThresholdData_whenSave_thenPersistsCorrectly() {
            SewThresholdBasedMaintenanceInputDataDto complexData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("COMPLEX_MODULE")
                    .smartServiceId("COMPLEX_SERVICE")
                    .frequencyType(FrequencyType.HOURS)
                    .frequencyValue(8)
                    .build();

            sampleTask1.setRequestBody(complexData);

            ScheduledTask savedTask = scheduledTaskRepository.save(sampleTask1);

            assertThat(savedTask.getThresholdBasedData()).isNotNull();
            assertThat(savedTask.getThresholdBasedData().getModuleId()).isEqualTo("COMPLEX_MODULE");
            assertThat(savedTask.getThresholdBasedData().getSmartServiceId()).isEqualTo("COMPLEX_SERVICE");
        }

        @Test
        @DisplayName("Save task with null threshold data : Success")
        void givenTaskWithNullThresholdData_whenSave_thenPersistsCorrectly() {
            sampleTask3.setRequestBody(null); // Grouping task might not have threshold data

            ScheduledTask savedTask = scheduledTaskRepository.save(sampleTask3);

            assertThat(savedTask.getThresholdBasedData()).isNull();
            assertThat(savedTask.getSmartServiceType()).isEqualTo(GROUPING_BASED_TYPE);
        }

        @Test
        @DisplayName("Find by smart service type : Case sensitivity")
        void givenMixedCaseTasks_whenFindBySmartServiceType_thenMatchesExactCase() {
            sampleTask1.setSmartServiceType("threshold_based_predictive_maintenance"); // lowercase
            scheduledTaskRepository.save(sampleTask1);
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Pageable pageable = PageRequest.of(0, 10);

            Page<ScheduledTask> upperCaseResult = scheduledTaskRepository.findBySmartServiceType(THRESHOLD_BASED_TYPE, pageable);
            Page<ScheduledTask> lowerCaseResult = scheduledTaskRepository.findBySmartServiceType("threshold_based_predictive_maintenance", pageable);

            assertThat(upperCaseResult.getContent()).isEmpty();
            assertThat(lowerCaseResult.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Find by smart service type with special characters : Success")
        void givenTasksWithSpecialCharacters_whenFindBySmartServiceType_thenHandlesCorrectly() {
            String specialType = "SPECIAL-TYPE_WITH@CHARS";
            sampleTask1.setSmartServiceType(specialType);
            scheduledTaskRepository.save(sampleTask1);
            
            // Allow some time for indexing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Pageable pageable = PageRequest.of(0, 10);

            Page<ScheduledTask> result = scheduledTaskRepository.findBySmartServiceType(specialType, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getSmartServiceType()).isEqualTo(specialType);
        }
    }
}
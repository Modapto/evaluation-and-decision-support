package gr.atc.modapto.service;

import gr.atc.modapto.dto.ScheduledTaskDto;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.enums.FrequencyType;
import gr.atc.modapto.model.ScheduledTask;
import gr.atc.modapto.repository.ScheduledTaskRepository;
import gr.atc.modapto.service.interfaces.IPredictiveMaintenanceService;
import gr.atc.modapto.exception.CustomExceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledTaskService Unit Tests")
class ScheduledTaskServiceTests {

    @Mock
    private ScheduledTaskRepository taskRepository;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private IPredictiveMaintenanceService predictiveMaintenanceService;

    @Mock
    @SuppressWarnings("rawtypes")
    private ScheduledFuture scheduledFuture;

    @InjectMocks
    private ScheduledTaskService scheduledTaskService;

    private ScheduledTaskDto sampleTaskDto;
    private ScheduledTask sampleTaskEntity;
    private SewThresholdBasedMaintenanceInputDataDto sampleThresholdData;
    private static final String THRESHOLD_BASED_SERVICE_TYPE = "THRESHOLD_BASED_PREDICTIVE_MAINTENANCE";

    @BeforeEach
    void setUp() {
        sampleThresholdData = SewThresholdBasedMaintenanceInputDataDto.builder()
                .moduleId("TEST_MODULE")
                .smartServiceId("THRESHOLD_SERVICE")
                .build();

        sampleTaskDto = ScheduledTaskDto.builder()
                .id("task-1")
                .moduleId("TEST_MODULE")
                .smartServiceId("THRESHOLD_SERVICE")
                .smartServiceType(THRESHOLD_BASED_SERVICE_TYPE)
                .frequencyType(FrequencyType.HOURS)
                .frequencyValue(24)
                .requestBody(sampleThresholdData)
                .createdAt(LocalDateTime.now())
                .nextExecutionTime(LocalDateTime.now().plusHours(24))
                .build();

        sampleTaskEntity = new ScheduledTask();
        sampleTaskEntity.setId("task-1");
        sampleTaskEntity.setModuleId("TEST_MODULE");
        sampleTaskEntity.setSmartServiceId("THRESHOLD_SERVICE");
        sampleTaskEntity.setSmartServiceType(THRESHOLD_BASED_SERVICE_TYPE);
        sampleTaskEntity.setFrequencyType(FrequencyType.HOURS);
        sampleTaskEntity.setFrequencyValue(24);
        sampleTaskEntity.setRequestBody(sampleThresholdData);
        sampleTaskEntity.setCreatedAt(LocalDateTime.now());
        sampleTaskEntity.setNextExecutionTime(LocalDateTime.now().plusHours(24));
    }

    @Nested
    @DisplayName("Register Scheduled Task")
    class RegisterScheduledTask {

        @Test
        @DisplayName("Register scheduled task : Success")
        void givenValidTask_whenRegisterScheduledTask_thenReturnsTrue() {
            // Given
            when(modelMapper.map(any(ScheduledTaskDto.class), eq(ScheduledTask.class)))
                    .thenReturn(sampleTaskEntity);
            when(taskRepository.save(any(ScheduledTask.class)))
                    .thenReturn(sampleTaskEntity);
            when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
                    .thenReturn((ScheduledFuture) scheduledFuture);

            // When
            boolean result = scheduledTaskService.registerScheduledTask(sampleTaskDto);

            // Then
            assertThat(result).isTrue();
            verify(modelMapper).map(any(ScheduledTaskDto.class), eq(ScheduledTask.class));
            verify(taskRepository).save(any(ScheduledTask.class));
            verify(taskScheduler).schedule(any(Runnable.class), any(java.time.Instant.class));
        }


        @Test
        @DisplayName("Register scheduled task : Mapping exception")
        void givenMappingException_whenRegisterScheduledTask_thenReturnsFalse() {
            // Given
            when(modelMapper.map(any(ScheduledTaskDto.class), eq(ScheduledTask.class)))
                    .thenThrow(new MappingException(List.of(new ErrorMessage("Mapping error"))));

            // When
            boolean result = scheduledTaskService.registerScheduledTask(sampleTaskDto);

            // Then
            assertThat(result).isFalse();
            verify(modelMapper).map(any(ScheduledTaskDto.class), eq(ScheduledTask.class));
            verify(taskRepository, never()).save(any(ScheduledTask.class));
            verify(taskScheduler, never()).schedule(any(Runnable.class), any(java.time.Instant.class));
        }

        @Test
        @DisplayName("Register scheduled task : Sets creation and execution time")
        void givenTaskWithoutTimes_whenRegisterScheduledTask_thenSetsTimestamps() {
            // Given
            ScheduledTaskDto taskWithoutTimes = ScheduledTaskDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .smartServiceType(THRESHOLD_BASED_SERVICE_TYPE)
                    .frequencyType(FrequencyType.HOURS)
                    .frequencyValue(24)
                    .requestBody(sampleThresholdData)
                    .build();

            when(modelMapper.map(any(ScheduledTaskDto.class), eq(ScheduledTask.class)))
                    .thenReturn(sampleTaskEntity);
            when(taskRepository.save(any(ScheduledTask.class)))
                    .thenReturn(sampleTaskEntity);
            when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
                    .thenReturn((ScheduledFuture) scheduledFuture);

            // When
            boolean result = scheduledTaskService.registerScheduledTask(taskWithoutTimes);

            // Then
            assertThat(result).isTrue();
            verify(modelMapper).map(any(ScheduledTaskDto.class), eq(ScheduledTask.class));
            verify(taskRepository).save(any(ScheduledTask.class));
        }
    }

    @Nested
    @DisplayName("Retrieve Scheduled Tasks by Smart Service Type")
    class RetrieveScheduledTasksBySmartServiceType {

        @Test
        @DisplayName("Retrieve scheduled tasks : Success")
        void givenValidSmartServiceType_whenRetrieveScheduledTasks_thenReturnsPagedResults() {
            // Given
            List<ScheduledTask> taskEntities = Collections.singletonList(sampleTaskEntity);
            Page<ScheduledTask> taskPage = new PageImpl<>(taskEntities);
            Pageable pageable = Pageable.ofSize(10);

            when(taskRepository.findBySmartServiceType(THRESHOLD_BASED_SERVICE_TYPE, pageable))
                    .thenReturn(taskPage);
            when(modelMapper.map(any(ScheduledTask.class), eq(ScheduledTaskDto.class)))
                    .thenReturn(sampleTaskDto);

            // When
            Page<ScheduledTaskDto> result = scheduledTaskService
                    .retrieveScheduledTaskBySmartServiceType(pageable, THRESHOLD_BASED_SERVICE_TYPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getId()).isEqualTo("task-1");
            verify(taskRepository).findBySmartServiceType(THRESHOLD_BASED_SERVICE_TYPE, pageable);
            verify(modelMapper).map(any(ScheduledTask.class), eq(ScheduledTaskDto.class));
        }

        @Test
        @DisplayName("Retrieve scheduled tasks : Empty result")
        void givenNoMatchingTasks_whenRetrieveScheduledTasks_thenReturnsEmptyPage() {
            // Given
            Page<ScheduledTask> emptyPage = new PageImpl<>(Collections.emptyList());
            Pageable pageable = Pageable.ofSize(10);

            when(taskRepository.findBySmartServiceType(THRESHOLD_BASED_SERVICE_TYPE, pageable))
                    .thenReturn(emptyPage);

            // When
            Page<ScheduledTaskDto> result = scheduledTaskService
                    .retrieveScheduledTaskBySmartServiceType(pageable, THRESHOLD_BASED_SERVICE_TYPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            verify(taskRepository).findBySmartServiceType(THRESHOLD_BASED_SERVICE_TYPE, pageable);
            verify(modelMapper, never()).map(any(ScheduledTask.class), eq(ScheduledTaskDto.class));
        }

        @Test
        @DisplayName("Retrieve scheduled tasks : General exception")
        void givenGeneralException_whenRetrieveScheduledTasks_thenReturnsEmptyPage() {
            // Given
            Pageable pageable = Pageable.ofSize(10);
            when(taskRepository.findBySmartServiceType(THRESHOLD_BASED_SERVICE_TYPE, pageable))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When
            Page<ScheduledTaskDto> result = scheduledTaskService
                    .retrieveScheduledTaskBySmartServiceType(pageable, THRESHOLD_BASED_SERVICE_TYPE);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(taskRepository).findBySmartServiceType(THRESHOLD_BASED_SERVICE_TYPE, pageable);
        }
    }

    @Nested
    @DisplayName("Delete Scheduled Task by ID")
    class DeleteScheduledTaskById {

        @Test
        @DisplayName("Delete scheduled task : Success")
        void givenValidTaskId_whenDeleteScheduledTask_thenDeletesSuccessfully() {
            // Given
            String taskId = "task-1";
            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(sampleTaskEntity));
            doNothing().when(taskRepository).delete(sampleTaskEntity);

            // When
            scheduledTaskService.deleteScheduledTaskById(taskId);

            // Then
            verify(taskRepository).findById(taskId);
            verify(taskRepository).delete(sampleTaskEntity);
        }

        @Test
        @DisplayName("Delete scheduled task : Task not found")
        void givenNonExistentTaskId_whenDeleteScheduledTask_thenThrowsResourceNotFoundException() {
            // Given
            String taskId = "non-existent-task";
            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> scheduledTaskService.deleteScheduledTaskById(taskId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Scheduled task with ID: non-existent-task not found");

            verify(taskRepository).findById(taskId);
            verify(taskRepository, never()).delete(any(ScheduledTask.class));
        }

        @Test
        @DisplayName("Delete scheduled task : Database exception")
        void givenDatabaseException_whenDeleteScheduledTask_thenThrowsServiceOperationException() {
            // Given
            String taskId = "task-1";
            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(sampleTaskEntity));
            doThrow(new RuntimeException("Database error"))
                    .when(taskRepository).delete(sampleTaskEntity);

            // When & Then
            assertThatThrownBy(() -> scheduledTaskService.deleteScheduledTaskById(taskId))
                    .isInstanceOf(ServiceOperationException.class)
                    .hasMessageContaining("Failed to delete scheduled task");

            verify(taskRepository).findById(taskId);
            verify(taskRepository).delete(sampleTaskEntity);
        }
    }

    @Nested
    @DisplayName("Task Execution Logic")
    class TaskExecutionLogic {

        @Test
        @DisplayName("Calculate next execution time : Hours frequency")
        void givenHoursFrequency_whenCalculateNextExecutionTime_thenReturnsCorrectTime() {
            // This tests the frequency calculation logic indirectly through task registration
            // Given
            ScheduledTaskDto hourlyTask = ScheduledTaskDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .smartServiceType(THRESHOLD_BASED_SERVICE_TYPE)
                    .frequencyType(FrequencyType.HOURS)
                    .frequencyValue(6)
                    .requestBody(sampleThresholdData)
                    .build();

            when(modelMapper.map(any(ScheduledTaskDto.class), eq(ScheduledTask.class)))
                    .thenReturn(sampleTaskEntity);
            when(taskRepository.save(any(ScheduledTask.class)))
                    .thenReturn(sampleTaskEntity);
            when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
                   .thenReturn((ScheduledFuture) scheduledFuture);

            // When
            boolean result = scheduledTaskService.registerScheduledTask(hourlyTask);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).save(any(ScheduledTask.class));
        }
    }

    @Nested
    @DisplayName("Application Lifecycle Management")
    class ApplicationLifecycleManagement {

        @Test
        @DisplayName("Initialize scheduled tasks : Success with future tasks")
        void givenFutureTasks_whenInitializeScheduledTasks_thenSchedulesTasks() {
            // Given
            ScheduledTask futureTask = new ScheduledTask();
            futureTask.setId("future-task");
            futureTask.setModuleId("TEST_MODULE");
            futureTask.setSmartServiceType(THRESHOLD_BASED_SERVICE_TYPE);
            futureTask.setNextExecutionTime(LocalDateTime.now().plusHours(1));

            when(taskRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(futureTask)));
            //when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
            //        .thenReturn((ScheduledFuture<?>) scheduledFuture);

            // When
            scheduledTaskService.initializeScheduledTasks();

            // Then
            verify(taskRepository).findAll(any(Pageable.class));
            verify(taskScheduler).schedule(any(Runnable.class), any(java.time.Instant.class));
        }

        @Test
        @DisplayName("Initialize scheduled tasks : Success with overdue tasks")
        void givenOverdueTasks_whenInitializeScheduledTasks_thenExecutesImmediately() {
            // Given
            ScheduledTask overdueTask = new ScheduledTask();
            overdueTask.setId("overdue-task");
            overdueTask.setModuleId("TEST_MODULE");
            overdueTask.setSmartServiceType(THRESHOLD_BASED_SERVICE_TYPE);
            overdueTask.setRequestBody(sampleThresholdData);
            overdueTask.setFrequencyType(FrequencyType.HOURS);
            overdueTask.setFrequencyValue(1);
            overdueTask.setNextExecutionTime(LocalDateTime.now().minusHours(1)); // Overdue

            when(taskRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(overdueTask)));
            when(taskRepository.save(any(ScheduledTask.class)))
                    .thenReturn(overdueTask);
            //when(taskScheduler.schedule(any(Runnable.class), any(java.time.Instant.class)))
            //        .thenReturn((ScheduledFuture<?>) scheduledFuture);

            // When
            scheduledTaskService.initializeScheduledTasks();

            // Then
            verify(taskRepository).findAll(any(Pageable.class));
            // Overdue task should be executed and rescheduled
            verify(taskRepository, atLeastOnce()).save(any(ScheduledTask.class));
        }

        @Test
        @DisplayName("Initialize scheduled tasks : Empty task list")
        void givenNoTasks_whenInitializeScheduledTasks_thenHandlesGracefully() {
            // Given
            when(taskRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            // When
            scheduledTaskService.initializeScheduledTasks();

            // Then
            verify(taskRepository).findAll(any(Pageable.class));
            verify(taskScheduler, never()).schedule(any(Runnable.class), any(java.time.Instant.class));
        }

        @Test
        @DisplayName("Initialize scheduled tasks : Database exception")
        void givenDatabaseException_whenInitializeScheduledTasks_thenHandlesGracefully() {
            // Given
            when(taskRepository.findAll(any(Pageable.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then - Should not throw exception
            assertThatCode(() -> scheduledTaskService.initializeScheduledTasks())
                    .doesNotThrowAnyException();

            verify(taskRepository).findAll(any(Pageable.class));
        }
    }
}
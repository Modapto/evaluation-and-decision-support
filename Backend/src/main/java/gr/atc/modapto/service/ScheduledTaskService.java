package gr.atc.modapto.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import gr.atc.modapto.dto.ScheduledTaskDto;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.enums.FrequencyType;
import gr.atc.modapto.model.ScheduledTask;
import gr.atc.modapto.repository.ScheduledTaskRepository;
import gr.atc.modapto.service.interfaces.IPredictiveMaintenanceService;
import gr.atc.modapto.service.interfaces.IScheduledTaskService;
import gr.atc.modapto.exception.CustomExceptions.*;
import jakarta.annotation.PreDestroy;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ScheduledTaskService implements IScheduledTaskService {

    private final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);
    private final ScheduledTaskRepository taskRepository;
    private final TaskScheduler taskScheduler;
    private final ModelMapper modelMapper;
    private final IPredictiveMaintenanceService predictiveMaintenanceService;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    private static final String THRESHOLD_BASED_SERVICE_TYPE = "THRESHOLD_BASED_PREDICTIVE_MAINTENANCE";
    private static final String MAPPING_ERROR = "Unable to map DTO to entity or vice-versa - Error: ";

    public ScheduledTaskService(ScheduledTaskRepository taskRepository, 
                               TaskScheduler taskScheduler, 
                               ModelMapper modelMapper,
                                IPredictiveMaintenanceService predictiveMaintenanceService){
        this.taskRepository = taskRepository;
        this.taskScheduler = taskScheduler;
        this.modelMapper = modelMapper;
        this.predictiveMaintenanceService = predictiveMaintenanceService;
    }

    /*
     * Schedule Active Tasks on Application Start-up
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeScheduledTasks(){
        logger.info("Loading scheduled tasks from database...");

        try {
            // Locate tasks that must be executed
            LocalDateTime now = LocalDateTime.now();
            List<ScheduledTask> activeTasks = taskRepository.findAll(Pageable.unpaged()).getContent();

            logger.info("Found {} scheduled tasks to initialize", activeTasks.size());

            for (ScheduledTask task : activeTasks) {
                try {
                    if (task.getNextExecutionTime().isAfter(now)) {
                        // Schedule task
                        scheduleTask(task);
                        logger.debug("Scheduled task {} for execution at {}", task.getId(), task.getNextExecutionTime());
                    } else {
                        // Task is overdue, so we need execute immediately and reschedule
                        logger.warn("Task {} is overdue (scheduled for {}). Executing immediately.", task.getId(), task.getNextExecutionTime());
                        executeTaskAndReschedule(task);
                    }
                } catch (Exception e) {
                    logger.error("Failed to initialize task {}: {}", task.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Failed to initialize scheduled tasks", e);
        }
    }

    /*
     * Register a task for Scheduling
     */
    @Override
    public boolean registerScheduledTask(ScheduledTaskDto task) {
        try{
            ScheduledTask scheduledTask = modelMapper.map(task, ScheduledTask.class);
            scheduledTask.setCreatedAt(LocalDateTime.now().withNano(0));
            scheduledTask.setNextExecutionTime(calculateNextExecutionTime(scheduledTask.getFrequencyType(), scheduledTask.getFrequencyValue()));

            ScheduledTask savedTask = taskRepository.save(scheduledTask);
            scheduleTask(savedTask);
            
            return true;
        } catch (ElasticsearchException e) {
            logger.error("Exception occurred during Scheduled Task registration - Error: {}", e.getMessage());
            return false;
        } catch (MappingException e){
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            return false;
        }
    }

    @Override
    public Page<ScheduledTaskDto> retrieveScheduledTaskBySmartServiceType(Pageable pageable, String smartServiceType) {
        try{
            Page<ScheduledTask> paginatedTasks = taskRepository.findBySmartServiceType(smartServiceType, pageable);
            List<ScheduledTaskDto> tasksList = paginatedTasks.getContent()
                .stream()
                .map(task -> modelMapper.map(task, ScheduledTaskDto.class))
                .toList();

            return new PageImpl<>(tasksList, pageable, paginatedTasks.getTotalElements());
        } catch (ElasticsearchException e) {
            logger.error("Exception occurred during retrieving Scheduled Tasks for type '{}' - Error: {}", smartServiceType, e.getMessage());
            throw new DatabaseException("Unable to retrieve requested Scheduled Tasks from repository");
        } catch (Exception e) {
            logger.error("Exception occurred during Scheduled Task retrieval - Error: {}", e.getMessage());
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    @Override
    public void deleteScheduledTaskById(String taskId) {
        try {
            ScheduledTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled task with ID: " + taskId + " not found"));
            
            // Cancel the scheduled task
            cancelTask(taskId);
            
            // Delete from repository
            taskRepository.delete(task);
            
            logger.debug("Successfully deleted scheduled task with ID: {}", taskId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting scheduled task with ID: {} - Error: {}", taskId, e.getMessage());
            throw new ServiceOperationException("Failed to delete scheduled task - Error: " +  e.getMessage());
        }
    }

    /*
     * Helper method to schedule a Task
     */
    private void scheduleTask(ScheduledTask task) {
        // Cancel existing schedule if any existent
        cancelTask(task.getId());

        Runnable taskRunnable = () -> {
            try {
                executeTaskAndReschedule(task);
                // Update next execution time
                task.setNextExecutionTime(calculateNextExecutionTime(task.getFrequencyType(), task.getFrequencyValue()));
                taskRepository.save(task);
            } catch (Exception e) {
                logger.error("Error executing scheduled task: {}", task.getId(), e);
            } finally {
                // Clean up the completed future task
                scheduledTasks.remove(task.getId());
            }
        };

        // Schedule the next execution one-time
        Instant nextExecution = task.getNextExecutionTime()
                .atZone(ZoneId.systemDefault())
                .toInstant();
        ScheduledFuture<?> future = taskScheduler.schedule(taskRunnable, nextExecution);

        scheduledTasks.put(task.getId(), future);
        logger.debug("Scheduled task {} to execute at {}", task.getId(), task.getNextExecutionTime());
    }

    /*
     * Helper method to execute a Task and schedule it
     */
    /**
     * Execute task and reschedule for next execution
     */
    private void executeTaskAndReschedule(ScheduledTask task) {
        try {
            // Execute the task
            executeTask(task);

            // Calculate and update next execution time
            LocalDateTime nextExecution = calculateNextExecutionTime(
                    task.getFrequencyType(),
                    task.getFrequencyValue()
            );
            task.setNextExecutionTime(nextExecution);

            taskRepository.save(task);

            // Schedule next execution
            scheduleTask(task);

            logger.debug("Task {} completed and rescheduled for {}", task.getId(), nextExecution);
        } catch (Exception e) {
            logger.error("Error executing scheduled task {}: {}", task.getId(), e.getMessage());
            // On error retry after 5 minutes
            handleTaskExecutionError(task);
        }
    }

    /**
     * Handle task execution errors with retry logic
     */
    private void handleTaskExecutionError(ScheduledTask task) {
        try {
            // Schedule retry after 5 minutes
            LocalDateTime retryTime = LocalDateTime.now().plusMinutes(5);
            task.setNextExecutionTime(retryTime);
            taskRepository.save(task);
            scheduleTask(task);

            logger.warn("Task {} failed, scheduled retry at {}", task.getId(), retryTime);
        } catch (Exception e) {
            logger.error("Failed to schedule retry for task {}: {}", task.getId(), e.getMessage());
        }
    }

    /*
     * Helper method to execute a Task
     */
    private void executeTask(ScheduledTask task) {
        try{
            logger.debug("Executing scheduled task: {} at {}", task.getId(), LocalDateTime.now());

            // Execute the scheduled task based on its type
            if (THRESHOLD_BASED_SERVICE_TYPE.equals(task.getSmartServiceType())) {
                executeThresholdBasedMaintenanceTask(task);
            } else {
                logger.warn("Unknown smart service type: {}", task.getSmartServiceType());
            }
        } catch (Exception e) {
                logger.error("Failed to execute task with ID: {}, for Service Type: {}", task.getId(), task.getSmartServiceType());
        }
    }

    private void cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(false);
            scheduledTasks.remove(taskId);
            logger.debug("Cancelled scheduled task {}: {}", taskId, cancelled ? "success" : "already completed");
        }
    }

    /*
     * Smart Service specific operations
     */
    private void executeThresholdBasedMaintenanceTask(ScheduledTask task) {
        try {
            logger.debug("Executing threshold-based maintenance task for module: {}", task.getModuleId());

            SewThresholdBasedMaintenanceInputDataDto invocationData = task.getThresholdBasedData();
            predictiveMaintenanceService.invokeThresholdBasedPredictiveMaintenance(invocationData);
            invocationData.setEvents(null);

            logger.debug("Successfully executed threshold-based maintenance task for module: {}", task.getModuleId());
        } catch (Exception e) {
            logger.error("Failed to execute threshold-based maintenance task for module: {} - Error: {}", task.getModuleId(), e.getMessage());
        }
    }

    /*
     * Helper methods to calculate next execution times
     */
    private LocalDateTime calculateNextExecutionTime(FrequencyType frequencyType, Integer frequencyValue) {
        LocalDateTime now = LocalDateTime.now();
        return switch (frequencyType) {
            case FrequencyType.MINUTES -> now.plusMinutes(frequencyValue);
            case FrequencyType.HOURS -> now.plusHours(frequencyValue);
            case FrequencyType.DAYS -> now.plusDays(frequencyValue);
            default -> throw new IllegalArgumentException("Unsupported frequency type: " + frequencyType);
        };
    }

    private long calculateDelayInMillis(FrequencyType frequencyType, Integer frequencyValue) {
        return switch (frequencyType) {
            case FrequencyType.MINUTES -> frequencyValue * 60 * 1000L;
            case FrequencyType.HOURS -> frequencyValue * 60 * 60 * 1000L;
            case FrequencyType.DAYS -> frequencyValue * 24 * 60 * 60 * 1000L;
            default -> throw new IllegalArgumentException("Unsupported frequency type: " + frequencyType);
        };
    }

    /**
     * Clean up on application shutdown
     */
    @PreDestroy
    public void shutdown() {
        logger.debug("Shutting down scheduler - cancelling {} active tasks", scheduledTasks.size());

        scheduledTasks.values().forEach(future -> {
            if (!future.isDone()) {
                future.cancel(false);
            }
        });

        scheduledTasks.clear();
        logger.debug("Scheduler shutdown completed");
    }
}
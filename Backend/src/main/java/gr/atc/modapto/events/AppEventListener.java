package gr.atc.modapto.events;

import gr.atc.modapto.dto.ScheduledTaskDto;
import gr.atc.modapto.service.interfaces.IScheduledTaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles application events
 */
@Component
@RequiredArgsConstructor
public class AppEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AppEventListener.class);

    private final IScheduledTaskService scheduledTaskService;

    @EventListener
    @Async(value = "taskExecutor")
    public void handleScheduledTaskRegistrationEvent(ScheduledTaskRegistrationEvent appEvent) {
        ScheduledTaskDto newTask = appEvent.getScheduledTask();
        logger.debug("Received new scheduled task for type '{}' for module '{}' and smart service '{}'", appEvent.getTaskType(), newTask.getModuleId(), newTask.getSmartServiceId());

        try{
            if (scheduledTaskService.registerScheduledTask(newTask))
                logger.info("Successfully registered scheduled task for threshold-based maintenance - Module: {}, Frequency: {} {}",
                        newTask.getModuleId(),
                        newTask.getFrequencyValue(),
                        newTask.getFrequencyType());

        } catch (Exception e) {
            logger.error("Error creating scheduled task for threshold-based maintenance: {}", e.getMessage());
        }
    }
}

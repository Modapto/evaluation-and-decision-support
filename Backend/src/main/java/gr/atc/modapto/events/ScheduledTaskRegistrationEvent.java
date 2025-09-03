package gr.atc.modapto.events;

import gr.atc.modapto.dto.ScheduledTaskDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ScheduledTaskRegistrationEvent extends ApplicationEvent {
    private final ScheduledTaskDto scheduledTask;
    private final String taskType;

    public ScheduledTaskRegistrationEvent(Object source, ScheduledTaskDto scheduledTask, String taskType) {
        super(source);
        this.scheduledTask = scheduledTask;
        this.taskType = taskType;
    }
}

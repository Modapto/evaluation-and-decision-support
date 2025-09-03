package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.ScheduledTaskDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IScheduledTaskService {

    boolean registerScheduledTask(ScheduledTaskDto task);

    Page<ScheduledTaskDto> retrieveScheduledTaskBySmartServiceType(Pageable pageable, String smartServiceType);

    void deleteScheduledTaskById(String taskId);
}

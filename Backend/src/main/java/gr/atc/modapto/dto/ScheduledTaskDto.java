package gr.atc.modapto.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import gr.atc.modapto.enums.FrequencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduledTaskDto {

    private String id;

    private String smartServiceType;

    private FrequencyType frequencyType;

    private Integer frequencyValue;

    private String moduleId;

    private String smartServiceId;

    private LocalDateTime nextExecutionTime;

    private LocalDateTime createdAt;

    private Object requestBody;

}

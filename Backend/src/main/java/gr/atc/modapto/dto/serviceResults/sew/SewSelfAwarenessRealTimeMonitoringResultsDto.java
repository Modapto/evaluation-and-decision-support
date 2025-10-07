package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.JsonInclude;
import gr.atc.modapto.dto.BaseEventResultsDto;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewSelfAwarenessRealTimeMonitoringResultsDto extends BaseEventResultsDto {

    private String id;

    private LocalDateTime timestamp;

    private String smartServiceId;

    private String moduleId;

    private String module;

    private String component;

    private String property;

    private String value;

    private Double lowThreshold;

    private Double highThreshold;

    private Double deviationPercentage;
}
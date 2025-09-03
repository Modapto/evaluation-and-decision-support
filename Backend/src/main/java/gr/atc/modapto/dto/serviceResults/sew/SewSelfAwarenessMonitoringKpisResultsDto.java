package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.BaseEventResultsDto;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewSelfAwarenessMonitoringKpisResultsDto extends BaseEventResultsDto {

    private String id;

    @JsonProperty("timestamp")
    private String timestamp;

    private String smartServiceId;

    private String moduleId;

    @JsonProperty("Ligne")
    private String ligne;

    @JsonProperty("Component")
    private String component;

    @JsonProperty("Variable")
    private String variable;

    @JsonProperty("Starting_date")
    private String startingDate;

    @JsonProperty("Ending_date")
    private String endingDate;

    @JsonProperty("Data_source")
    private String dataSource;

    @JsonProperty("Bucket")
    private String bucket;

    @JsonProperty("Data_list")
    private List<Double> data;
}

package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.BaseEventResultsDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewSelfAwarenessMonitoringKpisResultsDto extends BaseEventResultsDto {

    private String id;

    private LocalDateTime timestamp;

    private String smartServiceId;

    private String moduleId;

    @JsonProperty("Stage")
    private String stage;

    @JsonProperty("Cell")
    private String cell;

    @JsonProperty("PLC")
    private String plc;

    @JsonProperty("Module")
    private String module;

    @JsonProperty("SubElement")
    private String subElement;

    @JsonProperty("Component")
    private String component;

    @JsonProperty("Variable")
    private String variable;

    @JsonProperty("Variable_Type")
    private String variableType;

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

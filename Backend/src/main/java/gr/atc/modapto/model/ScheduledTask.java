package gr.atc.modapto.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.enums.FrequencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "scheduled-tasks")
public class ScheduledTask {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String smartServiceType;

    @Field(type = FieldType.Keyword)
    private FrequencyType frequencyType;

    @Field(type = FieldType.Integer)
    private Integer frequencyValue;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Keyword)
    private String smartServiceId;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime nextExecutionTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Object)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SewThresholdBasedMaintenanceInputDataDto.class, name = "threshold-based-pdm-input"),
    })
    private Object requestBody;

    /*
     * Helper methods to retrieve Threshold Based Input Data type
     */
    public SewThresholdBasedMaintenanceInputDataDto getThresholdBasedData() {
        if (requestBody instanceof SewThresholdBasedMaintenanceInputDataDto) {
            return (SewThresholdBasedMaintenanceInputDataDto) requestBody;
        }
        return null;
    }

    public boolean isThresholdBased() {
        return requestBody instanceof SewThresholdBasedMaintenanceInputDataDto;
    }

}

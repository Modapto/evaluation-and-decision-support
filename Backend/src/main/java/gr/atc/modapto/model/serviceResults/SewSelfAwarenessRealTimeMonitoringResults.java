package gr.atc.modapto.model.serviceResults;

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
@Document(indexName = "sew-self-awareness-real-time-monitoring-results")
public class SewSelfAwarenessRealTimeMonitoringResults {

    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Keyword)
    private String module;

    @Field(type = FieldType.Keyword)
    private String component;

    @Field(type = FieldType.Keyword)
    private String property;

    @Field(type = FieldType.Keyword)
    private String value;

    @Field(type = FieldType.Double)
    private Double lowThreshold;

    @Field(type = FieldType.Double)
    private Double highThreshold;

    @Field(type = FieldType.Double)
    private Double deviationPercentage;
}
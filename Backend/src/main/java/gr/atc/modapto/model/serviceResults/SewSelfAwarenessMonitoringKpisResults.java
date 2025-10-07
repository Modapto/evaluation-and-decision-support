package gr.atc.modapto.model.serviceResults;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "sew-self-awareness-monitoring-kpis")
public class SewSelfAwarenessMonitoringKpisResults {

    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword)
    private String smartServiceId;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Keyword)
    private String stage;

    @Field(type = FieldType.Keyword)
    private String cell;

    @Field(type = FieldType.Keyword)
    private String plc;

    @Field(type = FieldType.Keyword)
    private String module;

    @Field(type = FieldType.Keyword)
    private String subElement;

    @Field(type = FieldType.Keyword)
    private String component;

    @Field(type = FieldType.Keyword)
    private String variable;

    @Field(type = FieldType.Keyword)
    private String variableType;

    @Field(type = FieldType.Keyword)
    private String startingDate;

    @Field(type = FieldType.Keyword)
    private String endingDate;

    @Field(type = FieldType.Text)
    private String dataSource;

    @Field(type = FieldType.Keyword)
    private String bucket;

    @Field(type = FieldType.Double)
    private List<Double> data;
}

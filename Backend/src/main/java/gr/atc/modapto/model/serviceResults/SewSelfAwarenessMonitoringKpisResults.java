package gr.atc.modapto.model.serviceResults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "sew-self-awareness-monitoring-kpis")
public class SewSelfAwarenessMonitoringKpisResults {

    @Id
    private String id;

    @Field(name = "@timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(type = FieldType.Keyword)
    private String smartServiceId;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Keyword)
    private String ligne;

    @Field(type = FieldType.Keyword)
    private String component;

    @Field(type = FieldType.Keyword)
    private String variable;

    @Field(type = FieldType.Keyword)
    private String startingDate;

    @Field(type = FieldType.Keyword)
    private String endingDate;

    @Field(type = FieldType.Text)
    private String dataSource;

    @Field(type = FieldType.Keyword)
    private String bucket;

    @Field(type = FieldType.Object)
    private List<Double> data;
}

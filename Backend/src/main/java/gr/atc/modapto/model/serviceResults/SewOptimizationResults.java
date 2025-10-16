package gr.atc.modapto.model.serviceResults;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "optimization-sew")
public class SewOptimizationResults {

    @Id
    private String id;

    @Field(name = "timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;

    @Field(name = "data", type = FieldType.Object, enabled = false)
    private Map<String, SolutionData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolutionData {

        @Field(type = FieldType.Object, enabled = false)
        private MetricsData metrics;

        @Field(type = FieldType.Object, enabled = false)
        private Map<String, Map<String, String>> seq;

        @Field(type = FieldType.Object, enabled = false)
        private Map<String, Map<String, Map<String, Map<String, TimeRange>>>> orders;

        @Field(name = "init_order", type = FieldType.Object, enabled = false)
        private List<String> initOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsData {
        @Field(type = FieldType.Keyword)
        private String makespan;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRange {
        @Field(type = FieldType.Keyword)
        private String start;

        @Field(type = FieldType.Keyword)
        private String end;
    }
}

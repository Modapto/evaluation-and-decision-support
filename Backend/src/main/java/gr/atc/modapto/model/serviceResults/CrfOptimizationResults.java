package gr.atc.modapto.model.serviceResults;

import java.util.List;

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
@Document(indexName = "optimization-crf")
public class CrfOptimizationResults {

    @Id
    private String id;

    @Field(name = "@timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(name = "message", type = FieldType.Text)
    private String message;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;

    @Field(name = "optimization_results", type = FieldType.Flattened)
    private OptimizationResults optimizationResults;

    @Field(name = "optimization_run", type = FieldType.Boolean)
    private Boolean optimizationRun;

    @Field(name = "solutionTime", type = FieldType.Long)
    private Long solutionTime;

    @Field(name = "totalTime",type = FieldType.Long)
    private Long totalTime;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptimizationResults {

        @Field(type = FieldType.Flattened)
        private Exact exact;

        @Field(name = "improvement_percentage", type = FieldType.Float)
        private Float improvementPercentage;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Exact {

            @Field(type = FieldType.Long)
            private Long cost;

            @Field(name = "time_details", type = FieldType.Flattened)
            private List<TimeDetail> timeDetails;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            public static class TimeDetail {

                @Field(name = "component_picked", type = FieldType.Text)
                private String componentPicked;

                @Field(name = "component_placed", type = FieldType.Text)
                private String componentPlaced;

                @Field(type = FieldType.Long)
                private Long distance;

                @Field(type = FieldType.Text)
                private String from;

                @Field(type = FieldType.Text)
                private String to;
            }
        }
    }
}

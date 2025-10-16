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
@Document(indexName = "simulation-crf")
public class CrfSimulationResults {

    @Id
    private String id;

    @Field(name = "@timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(type = FieldType.Text)
    private String message;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;

    @Field(name = "simulation_run", type = FieldType.Boolean)
    private Boolean simulationRun;

    @Field(type = FieldType.Long)
    private Long solutionTime;

    @Field(type = FieldType.Long)
    private Long totalTime;

    @Field(type = FieldType.Flattened)
    private Baseline baseline;

    @Field(name = "best_phase", type = FieldType.Flattened)
    private BestPhase bestPhase;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Baseline {

        @Field(type = FieldType.Flattened)
        private Exact exact;

        @Field(type = FieldType.Flattened)
        private Linear linear;

        @Field(name = "gr_sequence", type = FieldType.Flattened)
        private List<Map<String, String>> grSequence;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Exact {
            @Field(type = FieldType.Text)
            private String cost;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Linear {
            @Field(type = FieldType.Text)
            private String cost;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BestPhase {

        @Field(name = "exact_cost", type = FieldType.Text)
        private String exactCost;

        @Field(name = "gr_sequence", type = FieldType.Object, enabled = false)
        private List<Map<String, String>> grSequence;

        @Field(name = "improvement_exact", type = FieldType.Float)
        private Float improvementExact;

        @Field(name = "improvement_linear", type = FieldType.Float)
        private Float improvementLinear;

        @Field(type = FieldType.Long)
        private Long phase;
    }
}
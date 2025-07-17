package gr.atc.modapto.model.serviceResults;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

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

    @Field(name = "productionModule", type = FieldType.Keyword)
    private String productionModule;

    @Field(name = "simulation_run", type = FieldType.Boolean)
    private Boolean simulationRun;

    @Field(type = FieldType.Long)
    private Long solutionTime;

    @Field(type = FieldType.Long)
    private Long totalTime;

    @Field(type = FieldType.Object)
    private Baseline baseline;

    @Field(name = "best_phase", type = FieldType.Object)
    private BestPhase bestPhase;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Baseline {

        @Field(type = FieldType.Object)
        private Exact exact;

        @Field(type = FieldType.Object)
        private Linear linear;

        @Field(name = "gr_sequence", type = FieldType.Object)
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

        @Field(name = "gr_sequence", type = FieldType.Object)
        private List<Map<String, String>> grSequence;

        @Field(name = "improvement_exact", type = FieldType.Float)
        private Float improvementExact;

        @Field(name = "improvement_linear", type = FieldType.Float)
        private Float improvementLinear;

        @Field(type = FieldType.Long)
        private Long phase;
    }
}
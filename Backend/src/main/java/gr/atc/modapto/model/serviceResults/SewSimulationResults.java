package gr.atc.modapto.model.serviceResults;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "simulation-sew")
public class SewSimulationResults {

    @Id
    private String id;

    @Field(name = "timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(name = "simulationData", type = FieldType.Object)
    private SimulationData simulationData;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationData {
        @Field(name = "makespan", type = FieldType.Object)
        private KpiMetric makespan;

        @Field(name = "machineUtilization", type = FieldType.Object)
        private KpiMetric machineUtilization;

        @Field(name = "throughputStdev", type = FieldType.Object)
        private KpiMetric throughputStdev;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiMetric {

        @Field(name = "current", type = FieldType.Double)
        private Double current;

        @Field(name = "simulated", type = FieldType.Double)
        private Double simulated;

        @Field(name = "difference", type = FieldType.Double)
        private Double difference;

        @Field(name = "differencePercent", type = FieldType.Double)
        private Double differencePercent;
    }
}

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

    @Field(name = "@timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(name = "simulationData", type = FieldType.Object)
    private SimulationData simulationData;

    @Field(name = "productionModule", type = FieldType.Keyword)
    private String productionModule;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationData {

        @Field(name = "makespan", type = FieldType.Object)
        private MetricComparison makespan;

        @Field(name = "machine_utilization", type = FieldType.Object)
        private MetricComparison machineUtilization;

        @Field(name = "throughput_stdev", type = FieldType.Object)
        private MetricComparison throughputStdev;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricComparison {
        @Field(name = "current", type = FieldType.Double)
        private Double current;

        @Field(name = "simulated", type = FieldType.Double)
        private Double simulated;

        @Field(name = "simulated", type = FieldType.Double)
        private Double difference;

        @Field(name = "difference_percent", type = FieldType.Double)
        private Double differencePercent;
    }
}

package gr.atc.modapto.dto.serviceResults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.BaseEventResultsDto;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SewSimulationResultsDto extends BaseEventResultsDto {

    private String id;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("productionModule")
    private String productionModule;

    @JsonProperty("simulationData")
    private SimulationData simulationData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationData {
        private MetricComparison makespan;

        @JsonProperty("machine_utilization")
        private MetricComparison machineUtilization;

        @JsonProperty("throughput_stdev")
        private MetricComparison throughputStdev;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricComparison {
        @JsonProperty("Current")
        private Double current;

        @JsonProperty("Simulated")
        private Double simulated;

        @JsonProperty("Difference")
        private Double difference;

        @JsonProperty("Difference_percent")
        private Double differencePercent;
    }

}

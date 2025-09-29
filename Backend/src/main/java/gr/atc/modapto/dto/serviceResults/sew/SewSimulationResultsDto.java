package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("module")
    private String productionModule;

    @JsonProperty("simulationData")
    private SimulationData simulationData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationData {
        @JsonProperty("makespan")
        private KpiMetric makespan;

        @JsonProperty("machine_utilization")
        private KpiMetric machineUtilization;

        @JsonProperty("throughput_stdev")
        private KpiMetric throughputStdev;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiMetric {

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

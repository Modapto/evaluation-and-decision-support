package gr.atc.modapto.dto.serviceResults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.BaseEventResultsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CrfSimulationResults", description = "Simulation Results for CRF Pilot Case")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class CrfSimulationResultsDto extends BaseEventResultsDto {

    private String id;

    @JsonProperty("timestamp")
    private String timestamp;

    private String message;

    @JsonProperty("productionModule")
    private String productionModule;

    @JsonProperty("simulation_run")
    private Boolean simulationRun;

    @JsonProperty("solutionTime")
    private Long solutionTime;

    @JsonProperty("totalTime")
    private Long totalTime;

    private Baseline baseline;

    @JsonProperty("best_phase")
    private BestPhase bestPhase;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Baseline {

        private Exact exact;

        private Linear linear;

        @JsonProperty("gr_sequence")
        private List<Map<String, String>> grSequence;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Exact {
            private String cost;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Linear {
            private String cost;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BestPhase {

        @JsonProperty("exact_cost")
        private String exactCost;

        @JsonProperty("gr_sequence")
        private List<Map<String, String>> grSequence;

        @JsonProperty("improvement_exact")
        private Float improvementExact;

        @JsonProperty("improvement_linear")
        private Float improvementLinear;

        private Long phase;
    }
}

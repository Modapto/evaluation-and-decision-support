package gr.atc.modapto.dto.serviceResults.crf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gr.atc.modapto.dto.BaseEventResultsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CrfOptimizationResults", description = "Optimization Results for CRF Pilot Case")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class CrfOptimizationResultsDto extends BaseEventResultsDto {

    private String id;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("message")
    private String message;

    @JsonProperty("module")
    private String module;

    @JsonProperty("optimization_results")
    private OptimizationResults optimizationResults;

    @JsonProperty("optimization_run")
    private Boolean optimizationRun;

    @JsonProperty("solutionTime")
    private Long solutionTime;

    @JsonProperty("totalTime")
    private Long totalTime;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptimizationResults {

        private Exact exact;

        @JsonProperty("improvement_percentage")
        private Float improvementPercentage;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Exact {

            private Long cost;

            @JsonProperty("time_details")
            private List<TimeDetail> timeDetails;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            public static class TimeDetail {

                @JsonProperty("component_picked")
                private String componentPicked;

                @JsonProperty("component_placed")
                private String componentPlaced;

                private Long distance;

                private String from;

                private String to;
            }
        }
    }
}
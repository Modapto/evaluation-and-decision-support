package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.BaseEventResultsDto;
import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SewOptimizationResultsDto extends BaseEventResultsDto {

    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("productionModule")
    private String productionModule;

    @JsonProperty("data")
    private Map<String, SolutionData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolutionData {

        @JsonProperty("metrics")
        private MetricsData metrics;

        @JsonProperty("seq")
        private Map<String, Map<String, String>> seq;

        @JsonProperty("orders")
        private Map<String, Map<String, Map<String, Map<String, TimeRange>>>> orders;

        @JsonProperty("init_order")
        private List<String> initOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsData {
        @JsonProperty("makespan")
        private String makespan;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRange {
        @JsonProperty("start")
        private String start;

        @JsonProperty("end")
        private String end;
    }

}

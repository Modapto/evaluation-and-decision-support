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

    @JsonProperty("moduleId")
    private String moduleId;

    @JsonProperty("data")
    private Map<String, SolutionData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolutionData {

        @JsonProperty("KPIs")
        private KpiData kpis;

        @JsonProperty("schedule")
        private Map<String, Map<String, OrderData>> schedule;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiData {

        @JsonProperty("makespan")
        private String makespan;

        @JsonProperty("machine_utilization")
        private Double machineUtilization;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderData {
        @JsonProperty("OrderID")
        private String orderId;

        @JsonProperty("machines")
        private Map<String, TimeRange> machines;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    class TimeRange {

        @JsonProperty("start")
        private String start;

        @JsonProperty("end")
        private String end;
    }
}

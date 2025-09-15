package gr.atc.modapto.dto.serviceResults.sew;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.atc.modapto.dto.BaseEventResultsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Schema(name = "SEW Grouping Predictive Maintenance Result", 
        description = "Results from grouping-based predictive maintenance optimization for SEW plant")
public class SewGroupingPredictiveMaintenanceOutputDto extends BaseEventResultsDto {

    @Schema(description = "Unique identifier for this maintenance result")
    private String id;

    @Schema(description = "MODAPTO Module ID")
    private String moduleId;

    @Schema(description = "Smart service ID")
    private String smartServiceId;

    @Schema(description = "Cost savings achieved through grouping maintenance activities")
    @JsonProperty("Cost savings")
    private Double costSavings;

    @Schema(description = "Time window for the maintenance activities")
    @JsonProperty("Time window")
    private TimeWindow timeWindow;

    @Schema(description = "Grouped maintenance activities organized by category")
    @JsonProperty("Grouping maintenance")
    private Map<String, List<MaintenanceComponent>> groupingMaintenance;

    @Schema(description = "Individual maintenance activities organized by category")
    @JsonProperty("Individual maintenance")
    private Map<String, List<MaintenanceComponent>> individualMaintenance;

    @Schema(description = "Timestamp when this result was generated", 
            example = "2024-01-15T14:30:45")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "Maintenance Component", description = "Individual maintenance component details")
    public static class MaintenanceComponent {

        @Schema(description = "Unique identifier for the component", example = "12345")
        @JsonProperty("Module ID")
        private Integer moduleId;

        @Schema(description = "Name of the component requiring maintenance", example = "Motor Bearing Unit A1")
        @JsonProperty("Module")
        private String module;

        @Schema(description = "Optimal replacement time (hours from now)", example = "48.5")
        @JsonProperty("Replacement time")
        private Double replacementTime;

        @Schema(description = "Estimated maintenance duration in hours", example = "2.5")
        @JsonProperty("Duration")
        private Double duration;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "Maintenance Time Window", description = "Time window for maintenance activities")
    public static class TimeWindow {

        @Schema(description = "Start of the time window for analysis in ISO 8601 format",
                example = "2025-12-31T23:59:59")
        @JsonProperty("Begin")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime begin;

        @Schema(description = "End of the time window for analysis in ISO 8601 format",
                example = "2025-12-31T23:59:59")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("End")
        private LocalDateTime end;
    }

}

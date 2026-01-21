package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SEW Threshold-Based Predictive Maintenance Result", 
        description = "Results from threshold-based predictive maintenance analysis for SEW plant")
public class SewThresholdBasedPredictiveMaintenanceOutputDto {

    @Schema(description = "Unique identifier for this maintenance result")
    private String id;

    @Schema(description = "MODAPTO Module ID")
    private String moduleId;

    @Schema(description = "Smart service ID")
    private String smartServiceId;

    @Schema(description = "Maintenance recommendation based on threshold analysis", 
            example = "Replace bearing in motor unit within 72 hours")
    @JsonProperty("recommendation")
    @JsonAlias({"Recommendation", "recommendation"})
    private String recommendation;

    @JsonProperty("cell")
    @JsonAlias({"Cell", "cell"})
    private String cell;

    @JsonProperty("duration")
    @JsonAlias({"Duration", "duration"})
    private Integer duration;

    @JsonProperty("module_id")
    @JsonAlias({"Module ID", "module_id"})
    private String moduleIdentifier;

    @JsonProperty("sub_element_id")
    @JsonAlias({"Sub Element ID", "Sub element ID", "sub_element_id"})
    private String subElementIdentifier;

    @Schema(description = "Detailed analysis and supporting information", 
            example = "Vibration levels exceeded threshold of 2.5mm/s RMS. Temperature trending upward.")
    @JsonProperty("details")
    @JsonAlias({"Details", "details"})
    private String details;

    @Schema(description = "Timestamp when this result was generated", 
            example = "2024-01-15T14:30:45")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}

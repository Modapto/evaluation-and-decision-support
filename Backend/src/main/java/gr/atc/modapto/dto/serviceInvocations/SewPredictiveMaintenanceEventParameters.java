package gr.atc.modapto.dto.serviceInvocations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "SEW Predictive Maintenance Parameters",
        description = "Algorithm parameters for SEW predictive maintenance analysis")
public class SewPredictiveMaintenanceEventParameters {

    @Schema(description = "Module identifier for the maintenance analysis", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Module ID can not be empty")
    @JsonProperty("module_ID")
    private String moduleID;

    @Schema(description = "List of component identifiers to analyze", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Component ID can not be empty")
    @JsonProperty("components_ID")
    private List<String> componentsID;

    @Schema(description = "Size of the analysis window in time units",
            example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Window Size can not be null")
    @Positive(message = "Window Size must be positive")
    @JsonProperty("window_size")
    private Integer windowSize;

    @Schema(description = "Threshold value for inspection recommendations",
            example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Inspection Threshold can not be null")
    @Positive(message = "Inspection Threshold must be positive")
    @JsonProperty("inspection_threshold")
    private Integer inspectionThreshold;

    @Schema(description = "Threshold value for replacement recommendations",
            example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Replacement Threshold can not be null")
    @Positive(message = "Replacement Threshold must be positive")
    @JsonProperty("replacement_threshold")
    private Integer replacementThreshold;
}

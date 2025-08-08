package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SEW Threshold-Based Maintenance Input", 
        description = "Input data for threshold-based predictive maintenance analysis")
public class SewThresholdBasedMaintenanceInputDataDto {

    @Schema(description = "MODAPTO Module ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Module ID cannot be empty")
    @JsonProperty("moduleId")
    private String moduleId;

    @Schema(description = "Smart service ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Smart Service ID cannot be empty")
    @JsonProperty("smartServiceId")
    private String smartServiceId;

    @Schema(description = "List of maintenance event data for analysis")
    @JsonProperty("events")
    private List<MaintenanceDataDto> events;

    @Schema(description = "Parameters for the predictive maintenance analysis algorithm", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "Parameters cannot be null")
    @JsonProperty("parameters")
    private SewPredictiveMaintenanceEventParameters parameters;
}

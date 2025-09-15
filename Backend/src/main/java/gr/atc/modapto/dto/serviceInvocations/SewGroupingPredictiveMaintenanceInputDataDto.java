package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.sew.SewComponentInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SEW Grouping Predictive Maintenance Input", 
        description = "Input data for grouping-based predictive maintenance optimization")
public class SewGroupingPredictiveMaintenanceInputDataDto {

    @Schema(description = "MODAPTO Module ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Module Id cannot be empty")
    @JsonProperty("moduleId")
    private String moduleId;

    @Schema(description = "Smart Service ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Smart Service Id cannot be empty")
    @JsonProperty("smartServiceId")
    private String smartServiceId;

    @Schema(description = "Setup cost for maintenance activities", 
            example = "500.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Setup Cost cannot be null")
    @JsonProperty("setupCost")
    private Double setupCost;

    @Schema(description = "Cost rate per hour of downtime", 
            example = "150.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Downtime Cost Rate cannot be null")
    @JsonProperty("downtimeCostRate")
    private Double downtimeCostRate;

    @Schema(description = "Number of available repair technicians", 
            example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "No repairmen cannot be empty")
    @JsonProperty("noRepairmen")
    private Integer noRepairmen;

    @Schema(description = "List of components to be analyzed for maintenance grouping", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("components")
    private List<SewComponentInfoDto> componentList;

    @Schema(description = "Start of the time window for analysis in ISO 8601 format",
            example = "2025-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Time Window Start can not be null")
    @JsonProperty("timeWindowStart")
    private LocalDateTime timeWindowStart;

    @Schema(description = "End of the time window for analysis in ISO 8601 format",
            example = "2025-12-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Time Window End can not be null")
    @JsonProperty("timeWindowEnd")
    private LocalDateTime timeWindowEnd;

}

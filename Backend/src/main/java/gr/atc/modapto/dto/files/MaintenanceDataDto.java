package gr.atc.modapto.dto.files;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "Maintenance Data (CORIM)", description = "CORIM File Structure")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceDataDto {

    @JsonProperty("Stage")
    private String stage;

    @JsonProperty("Cell")
    private String cell;

    @JsonProperty("Module")
    private String module;

    @JsonProperty("Component")
    private String component;

    @JsonProperty("Failure Type")
    private String failureType;

    @JsonProperty("Failure description")
    private String failureDescription;

    @JsonProperty("Maintenance Action performed")
    private String maintenanceActionPerformed;

    @JsonProperty("component replacement (yes/no)")
    private String componentReplacement;

    @JsonProperty("Name")
    private String componentName;

    @JsonProperty("TS request creation")
    private String tsRequestCreation;

    @JsonProperty("TS request acknowledged")
    private String tsRequestAcknowledged;

    @JsonProperty("TS Intervention started")
    private String tsInterventionStarted;

    @JsonProperty("TS intervention finished")
    private String tsInterventionFinished;

    @JsonProperty("Intervention status")
    private String interventionStatus;

    @JsonProperty("MTBF")
    private String mtbf;

    @JsonProperty("MTBF stage level")
    private String mtbfStageLevel;

    @JsonProperty("Duration creation - acknowledged")
    private String durationCreationToAcknowledged;

    @JsonProperty("Duration creation -  intervention start")
    private String durationCreationToInterventionStart;

    @JsonProperty("Duration intervention started- finished")
    private String durationInterventionStartedToFinished;

    @JsonProperty("Total duration creation-finished")
    private String totalDurationCreationToFinished;

    @JsonProperty("Total maintenance time allocated")
    private String totalMaintenanceTimeAllocated;
}

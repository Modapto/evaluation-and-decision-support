package gr.atc.modapto.dto.sew;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "Maintenance Data (CORIM)", description = "CORIM File Structure")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceDataDto {

    @JsonProperty("dataId")
    private String id;

    @JsonProperty("modaptoModule")
    private String modaptoModule;

    @NotBlank(message = "Stage can not be blank")
    @JsonProperty("Stage")
    private String stage;

    @NotBlank(message = "Cell can not be blank")
    @JsonProperty("Cell")
    private String cell;

    @JsonProperty("ID")
    private String faultyElementId;

    @NotBlank(message = "Module description can not be blank")
    @JsonProperty("Module description")
    private String module;

    @NotBlank(message = "Module ID can not be blank")
    @JsonProperty("Module ID")
    private String moduleId;

    @NotBlank(message = "Component can not be blank")
    @JsonProperty("Component")
    private String component;

    @NotBlank(message = "Component ID can not be blank")
    @JsonProperty("Component ID")
    private String componentId;

    @JsonProperty("Failure Type (electrical/mechanical)")
    private String failureType;

    @JsonProperty("Failure description")
    private String failureDescription;

    @JsonProperty("Maintenance Action performed")
    private String maintenanceActionPerformed;

    @JsonProperty("Component replacement (yes/no)")
    private String componentReplacement;

    @JsonProperty("Name")
    private String workerName;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonProperty("TS request creation")
    private LocalDateTime tsRequestCreation;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonProperty("TS Intervention started")
    private LocalDateTime tsInterventionStarted;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonProperty("TS intervention finished")
    private LocalDateTime tsInterventionFinished;
}

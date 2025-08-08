package gr.atc.modapto.dto.sew;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "SewComponentInfo", description = "SEW Component Information List")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SewComponentInfoDto {

    @NotBlank(message = "Stage cannot be empty")
    @JsonProperty("Stage")
    private String stage;

    @NotBlank(message = "Cell cannot be empty")
    @JsonProperty("Cell")
    private String cell;

    @NotBlank(message = "Module cannot be empty")
    @JsonProperty("Module")
    private String module;

    @NotBlank(message = "Module ID cannot be empty")
    @JsonProperty("Module ID")
    private String moduleId;

    @NotNull(message = "Alpha cannot be empty")
    @JsonProperty("Alpha")
    private Double alpha;

    @NotNull(message = "Beta cannot be empty")
    @JsonProperty("Beta")
    private Double beta;

    @NotNull(message = "Gamma cannot be empty")
    @JsonProperty("Average maintenance duration")
    private Double averageMaintenanceDuration;

    @NotNull(message = "MTBF cannot be empty")
    @JsonProperty("MTBF")
    private Double mtbf;

    @JsonProperty("Last Maintenance Action Time")
    private String lastMaintenanceActionTime;
}

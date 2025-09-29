package gr.atc.modapto.dto.crf;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(name = "CRF Self-Awareness Parameters", description = "CRF Self-Awareness Parameters")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrfSelfAwarenessParametersDto {

    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    @Schema(description = "Threshold value - Default: 16.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Double threshold = 16.0;

    @NotNull(message = "Interval can not be null")
    @Positive(message = "Interval should be a positive number")
    @Schema(description = "Interval time in Minutes", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("intervalMinutes")
    private Integer intervalMinutes;

    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    @Schema(description = "Utilized model - Default: quadratic_model.json", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("modelPath")
    private String modelPath = "quadratic_model.json";

    @Schema(description = "MODAPTO Module ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Module Id cannot be empty")
    @JsonProperty("moduleId")
    private String moduleId;

    @Schema(description = "Smart Service ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Smart Service Id cannot be empty")
    @JsonProperty("smartServiceId")
    private String smartServiceId;
}

package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "FFT Optimization Service Input",
        description = "Input data for optimization service for Robot Configuration optimization")
public class FftOptimizationInputDto {

    @Schema(description = "MODAPTO Module ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Module Id cannot be empty")
    @JsonProperty("module")
    @JsonAlias({"moduleId", "module"})
    private String moduleId;

    @Schema(description = "Smart Service ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Smart Service Id cannot be empty")
    @JsonProperty("smartService")
    @JsonAlias({"smartService", "smartServiceId"})
    private String smartServiceId;

    @Schema(description = "Time limit for the optimization process", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("time_limit")
    private Integer timeLimit;

    @Schema(description = "Name of the DAT file", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("dat_file_name")
    private String datFileName;

    @Schema(description = "Name of the source file", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("src_file_name")
    private String srcFileName;

    @Schema(description = "Content of the DAT file", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("code_dat")
    private String codeDat;

    @Schema(description = "Content of the source file", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("code_src")
    private String codeSrc;

    @Schema(description = "Content of the configuration file", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("code_cfg")
    private String codeCfg;

    @Schema(description = "Robot configuration details", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("robotConfiguration")
    private Object robotConfiguration;
}

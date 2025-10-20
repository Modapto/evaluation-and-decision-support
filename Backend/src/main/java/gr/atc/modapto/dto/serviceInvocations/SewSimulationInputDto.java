package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import gr.atc.modapto.dto.sew.SewPlantEnvironmentDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SEW Optimization Service Input",
        description = "Input data for optimization service for Production Schedule optimization")
public class SewSimulationInputDto {

    @Schema(description = "MODAPTO Module ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Module Id cannot be empty")
    @JsonProperty("module")
    private String moduleId;

    @Schema(description = "Smart Service ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Smart Service Id cannot be empty")
    @JsonProperty("smartService")
    private String smartServiceId;

    @Valid
    @Schema(description = "Input Configuration for Optimization Invocation", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Configuration can not be null")
    private Config config;

    private Object orders;

    private Object processTimes;

    @JsonProperty("current_env")
    @JsonAlias({"current_env", "currentEnv"})
    private SewPlantEnvironmentDto.PlantData currentEnv;

    @JsonProperty("simulated_env")
    @JsonAlias({"simulated_env", "simulatedEnv"})
    private SewPlantEnvironmentDto.PlantData simulatedEnv;

    @Schema(description = "Production Schedules for SEW Case", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Map<String, JsonNode> data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Config{

        @NotNull(message = "Trials can not be null")
        @Schema(description = "Trials of simulation",
                example = "30.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("Trials")
        @JsonAlias({"trials", "Trials"})
        private Integer trials;

        @NotNull(message = "Min Var can not be null")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("min_var")
        @JsonAlias({"min_var", "minVar"})
        private Double minVar;

        @NotNull(message = "Max Var can not be null")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("max_var")
        @JsonAlias({"max_var", "maxVar"})
        private Double maxVar;

        @NotEmpty(message = "KPIs can not be empty")
        @Schema(description = "Available KPIs for optimization", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonProperty("KPIs")
        @JsonAlias({"KPIs", "kpis"})
        private Set<String> kpis;
    }
}

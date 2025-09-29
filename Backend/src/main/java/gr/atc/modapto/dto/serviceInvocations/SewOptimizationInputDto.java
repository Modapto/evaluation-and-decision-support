package gr.atc.modapto.dto.serviceInvocations;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(name = "SEW Optimization Service Input",
        description = "Input data for optimization service for Production Schedule optimization")
public class SewOptimizationInputDto {

    @Schema(description = "MODAPTO Module ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Module Id cannot be empty")
    @JsonProperty("moduleId")
    private String moduleId;

    @Schema(description = "Smart Service ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Smart Service Id cannot be empty")
    @JsonProperty("smartService")
    private String smartServiceId;

    @Valid
    @Schema(description = "Input Configuration for Optimization Invocation", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Configuration can not be null")
    private Config config;

    @Schema(description = "Production Schedules for SEW Case", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Map<String,SewProductionScheduleDto.DailyDataDto> input;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Config{

        @NotNull(message = "Time Limit can not be null")
        @Schema(description = "Time Limit of optimization",
                example = "30.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "Time Limit must be positive")
        @JsonProperty("timelimit")
        @JsonAlias({"timelimit", "timeLimit"})
        @Min(30)
        @Max(10800)
        private Double timeLimit;

        @NotNull(message = "Number of solutions can not be null")
        @Schema(description = "Number of optimized production schedules solutions",
                example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "Number of solutions  must be positive")
        @JsonProperty("returnedSols")
        @JsonAlias({"solutions", "returnedSols"})
        @Min(1)
        @Max(10)
        private Integer returnedSols;

        @NotEmpty(message = "KPIs can not be empty")
        @Schema(description = "Available KPIs for optimization", requiredMode = Schema.RequiredMode.REQUIRED)
        private Set<String> kpis;
    }
}

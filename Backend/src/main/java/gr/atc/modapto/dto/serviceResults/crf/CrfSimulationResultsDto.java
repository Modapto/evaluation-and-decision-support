package gr.atc.modapto.dto.serviceResults.crf;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.atc.modapto.dto.BaseEventResultsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CrfSimulationResults", description = "Simulation Results for CRF Pilot Case")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class CrfSimulationResultsDto extends BaseEventResultsDto {

    private String id;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    private String message;

    @JsonProperty("productionModule")
    private String productionModule;

    @JsonProperty("simulation_run")
    private Boolean simulationRun;

    @JsonProperty("solutionTime")
    private Long solutionTime;

    @JsonProperty("totalTime")
    private Long totalTime;

    private Object baseline;

    @JsonProperty("best_phase")
    private Object bestPhase;
}

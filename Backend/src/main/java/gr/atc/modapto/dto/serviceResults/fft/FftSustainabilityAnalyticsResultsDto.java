package gr.atc.modapto.dto.serviceResults.fft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "FftSustainabilityAnalyticsResults", description = "Sustainability Analytics Results for FFT Pilot Case")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class FftSustainabilityAnalyticsResultsDto{

    @JsonProperty("robot.used_power")
    private Double robotUsedPower;

    @JsonProperty("robot.used_energy")
    private Double robotUsedEnergy;

    @JsonProperty("timestamp_start")
    private Double timestampStart;

    @JsonProperty("timestamp_stop")
    private Double timestampStop;

    @JsonProperty("measurement_state")
    private Double measurementState;
}

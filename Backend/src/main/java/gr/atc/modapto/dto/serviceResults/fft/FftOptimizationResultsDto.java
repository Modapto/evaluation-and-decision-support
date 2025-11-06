package gr.atc.modapto.dto.serviceResults.fft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "FftOptimizationResults", description = "Optimization Results for FFT Pilot Case")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class FftOptimizationResultsDto {

    private String id;

    private LocalDateTime timestamp;

    private String module;

    @JsonProperty("optimizedCode_src")
    private String optimizedCodeSrc;

    @JsonProperty("time_limit")
    private Integer timeLimit;

    @JsonProperty("robotConfiguration")
    private Object robotConfiguration;

    @JsonProperty("time_difference")
    private Double timeDifference;

    @JsonProperty("optimizedCode_dat")
    private String optimizedCodeDat;

    @JsonProperty("energy_difference")
    private Double energyDifference;
}
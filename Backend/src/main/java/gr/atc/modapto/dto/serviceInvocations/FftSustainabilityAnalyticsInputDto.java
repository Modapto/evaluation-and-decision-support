package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "FftSustainabilityAnalyticsInput", description = "Sustainability Analytics Input for FFT Pilot Case")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class FftSustainabilityAnalyticsInputDto {

    private Double currentTime;

    private Double timeStep;

    private Double stepCount;

    private List<Object> argumentsPerStep;
}

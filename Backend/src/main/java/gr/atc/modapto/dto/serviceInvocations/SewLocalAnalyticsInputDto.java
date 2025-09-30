package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.serviceResults.sew.SewFilteringOptionsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "Local Analytics Input", description = "SEW Local Analytics formulated Input to generate Histograms")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewLocalAnalyticsInputDto {

    @Schema(description = "Stored Histogram Data - Not required", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("histogram_data")
    private List<SewSelfAwarenessMonitoringKpisResultsDto> histogramData;

    @Schema(description = "Parameters of the first module", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "First parameters can not be null")
    @Valid
    @JsonProperty("params1")
    private SewFilteringOptionsDto.Options firstParameters;

    @Schema(description = "Optional Parameters of the second module to compare", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("params2")
    @Valid
    private SewFilteringOptionsDto.Options secondParameters;
}

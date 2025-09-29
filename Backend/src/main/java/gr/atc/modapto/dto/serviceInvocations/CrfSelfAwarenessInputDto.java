package gr.atc.modapto.dto.serviceInvocations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.dto.crf.CrfSelfAwarenessParametersDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "CRF SA Input", description = "CRF Self Awareness Smart Service invocation data")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrfSelfAwarenessInputDto {

    @JsonIgnore
    private String moduleId;

    @JsonIgnore
    private String smartServiceId;

    @Schema(description = "Input parameters", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Parameters can not be null")
    @JsonProperty("parameters")
    @Valid
    private CrfSelfAwarenessParametersDto parameters;

    @Schema(description = "Event data of CRF MES processed when the Events CSV file is uploaded", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("data")
    @Valid
    private List<CrfKitHolderEventDto> data;
}

package gr.atc.modapto.dto.dt;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Digital Twin Input", description = "Generic input wrapper for Digital Twin service invocations")
public class DtInputDto<T>{

    @Schema(description = "Input arguments for the specific service type", 
            example = "Service-specific input data structure")
    private T inputArguments;

    @Builder.Default
    @Schema(description = "Client timeout duration in ISO 8601 format", 
            example = "PT60S", 
            defaultValue = "PT60S")
    private String clientTimeoutDuration = "PT60S";
}

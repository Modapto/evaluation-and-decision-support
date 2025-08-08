package gr.atc.modapto.dto.dt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Schema(name = "Digital Twin Response", description = "Generic response wrapper from Digital Twin service executions")
public class DtResponseDto<T>  {

    @Schema(description = "List of messages returned from the service execution in case sth failed")
    private List<JsonNode> messages;

    @Schema(description = "Current execution state of the service", 
            example = "Completed")
    private String executionState;

    @Schema(description = "Indicates whether the service execution was successful", 
            example = "true")
    private boolean success;

    @Schema(description = "Input and output arguments combined (raw JSON node)")
    private JsonNode inoutputArguments;

    @Schema(description = "Typed output arguments specific to the service type")
    private T outputArguments;
}

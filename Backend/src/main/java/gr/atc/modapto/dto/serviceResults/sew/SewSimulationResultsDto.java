package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.BaseEventResultsDto;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SewSimulationResultsDto extends BaseEventResultsDto {

    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("moduleId")
    private String moduleId;

    @JsonProperty("smartServiceId")
    private String smartServiceId;

    @JsonProperty("data")
    private Object data;
}

package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewSelfAwarenessMonitoringKpisInputDto {

    @NotBlank(message = "Smart Service ID cannot be empty")
    @JsonProperty("smartServiceId")
    private String smartServiceId;

    @NotBlank(message = "Module ID cannot be empty")
    @JsonProperty("moduleId")
    private String moduleId;

    @NotBlank(message = "Start Date cannot be empty")
    @JsonProperty("start_date")
    private String startDate;

    @NotBlank(message = "End date cannot be empty")
    @JsonProperty("end_date")
    private String endDate;

    @Valid
    @NotNull(message = "List of components cannot be null")
    @JsonProperty("components")
    private List<SewMonitorKpisComponentsDto> components;
}

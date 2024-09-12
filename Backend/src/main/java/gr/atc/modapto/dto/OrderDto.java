package gr.atc.modapto.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import gr.atc.modapto.enums.PilotCode;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {

    @JsonProperty("id")
    private String id;
    
    @NotEmpty
    @JsonProperty("customer")
    private PilotCode customer;

    @NotEmpty
    @JsonProperty("documentNumber")
    private String documentNumber;

    @NotEmpty
    @JsonProperty("orderof")
    private List<AssemblyDto> assemblies;
    
    @NotEmpty
    @JsonProperty("composedby")
    private List<ComponentDto> components;

    @JsonProperty("comments")
    private String comments;
}

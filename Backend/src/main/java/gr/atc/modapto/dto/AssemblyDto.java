package gr.atc.modapto.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssemblyDto {

    @NotEmpty
    @JsonProperty("type")
    private String type;

    @NotEmpty
    @JsonProperty("quantity")
    private Integer quantity;

    @NotEmpty
    @JsonProperty("pn")
    private String partNumber;

    @NotEmpty
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("expectedDeliveryDate")
    private String expectedDeliveryDate;
}

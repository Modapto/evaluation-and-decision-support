package gr.atc.modapto.dto.sew;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "Work Declaration Data", description = "Data for declaring work on a MODAPTO Module")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeclarationOfWorkDto {

    @NotBlank(message = "Module ID can not be blank")
    private String moduleId;

    @NotNull(message = "List of workers can not be empty")
    private List<String> workers;
}

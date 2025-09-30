package gr.atc.modapto.dto.crf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrfSimulationKittingConfigDto {

    private String id;

    private String filename;

    private Object config;

    private LocalDateTime uploadedAt;

    private String rawText;

    @JsonProperty("case")
    private String configCase;

    private String etag;
}

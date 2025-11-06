package gr.atc.modapto.dto.crf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrfOptimizationKittingConfigDto {

    private String id;

    private String filename;

    private Object config;

    private String uploadedAt;

    private String rawText;

    @JsonProperty("case")
    private String configCase;

    private String etag;
}

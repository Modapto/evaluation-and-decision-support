package gr.atc.modapto.dto.serviceResults.sew;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Schema(name = "FilteringOptions",
        description = "SEW Filtering Options for Local Analytics Process")
public class SewFilteringOptionsDto {

    @JsonProperty("filtering_options")
    private List<Options> filteringOptions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Options {
        @JsonProperty("ligne")
        @JsonAlias({"Ligne", "ligne"})
        private String ligne;

        @JsonProperty("component")
        @JsonAlias({"Component", "component"})
        private String component;

        @JsonProperty("variable")
        @JsonAlias({"Variable", "variable"})
        private String variable;

        @JsonProperty("date")
        @JsonAlias({"Date", "date"})
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        @Schema(
                description = "The date in format dd-MM-yyyy HH:mm:ss",
                type = "string",
                example = "30-09-2025 14:35:00"
        )
        private LocalDateTime date;
    }
}

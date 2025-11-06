package gr.atc.modapto.dto.serviceResults.sew;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @JsonProperty("distinct_values")
    @Schema(description = "Includes all distinct values for each category")
    private DistinctValues distinctValues;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Options {

        @JsonProperty("Cell")
        @JsonAlias({"Cell", "cell"})
        private String cell;

        @JsonProperty("Module")
        @JsonAlias({"Module", "module"})
        private String module;

        @JsonProperty("SubElement")
        @JsonAlias({"SubElement", "subElement"})
        private String subElement;

        @JsonProperty("Component")
        @JsonAlias({"Component", "component"})
        private String component;

        @JsonProperty("Variable")
        @JsonAlias({"Variable", "variable"})
        private String variable;

        @JsonProperty("Date")
        @JsonAlias({"Date", "date"})
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        @Schema(
                description = "The date in format dd-MM-yyyy HH:mm:ss",
                type = "string",
                example = "30-09-2025 14:35:00"
        )
        private LocalDateTime date;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DistinctValues {

        private List<String> cells;

        private List<String> modules;

        private List<String> subElements;

        private List<String> components;

        private List<String> variables;
    }

}

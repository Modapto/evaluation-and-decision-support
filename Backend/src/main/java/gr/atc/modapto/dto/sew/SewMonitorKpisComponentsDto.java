package gr.atc.modapto.dto.sew;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewMonitorKpisComponentsDto {

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String id;

    @NotBlank(message = "Module ID cannot be empty")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime timestampCreated;

    @NotNull(message = "List of components cannot be null")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SewMonitorKpisComponentsDataDto> components;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SewMonitorKpisComponentsDataDto {

        @NotBlank(message = "Stage cannot be empty")
        @JsonProperty("Stage")
        private String stage;

        @NotBlank(message = "Cell cannot be empty")
        @JsonProperty("Cell")
        private String cell;

        @NotBlank(message = "Plc cannot be empty")
        @JsonProperty("Plc")
        private String plc;

        @NotBlank(message = "Module cannot be empty")
        @JsonProperty("Module")
        private String module;

        @NotBlank(message = "Sub Element cannot be empty")
        @JsonProperty("subElement")
        private String subElement;

        @NotBlank(message = "Component cannot be empty")
        @JsonProperty("Component")
        private String component;

        @Valid
        @NotNull(message = "List of properties can not be null")
        @JsonProperty("Property")
        private List<PropertyDto> property;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PropertyDto {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Low_thre")
        private Integer lowThreshold;

        @JsonProperty("High_thre")
        private Integer highThreshold;
    }
}

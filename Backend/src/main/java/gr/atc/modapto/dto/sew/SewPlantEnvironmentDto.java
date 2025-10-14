package gr.atc.modapto.dto.sew;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewPlantEnvironmentDto {

    private Map<String, StageDto> stages;

    private Map<String, Map<String, Integer>> transTimes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StageDto {

        @JsonProperty("WIP_in")
        private Integer wipIn;

        @JsonProperty("WIP_out")
        private Integer wipOut;

        private String modules;

        @JsonProperty("Cells")
        private Map<String, Object> cells;
    }
}

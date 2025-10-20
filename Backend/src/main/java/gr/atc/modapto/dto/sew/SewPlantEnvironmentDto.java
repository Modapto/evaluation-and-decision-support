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

    private PlantData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlantData {

        private Object stages;

        private Object transTimes;

    }
}

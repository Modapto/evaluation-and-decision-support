package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewProductionScheduleDto {

    private Map<String, DailyDataDto> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class DailyDataDto {

        @JsonProperty("newlayout")
        private Object newLayout;

        private Object workers;

        private Object general;

        private Map<String, Object> orders;

        private Map<String, Map<String, Map<String, Integer>>> processTimes;

        private List<String> givenOrder;
    }
}

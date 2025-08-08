package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
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
    public static class DailyDataDto {

        @JsonProperty("newlayout")
        private NewLayoutDto newLayout;

        private WorkersDto workers;

        private GeneralDto general;

        private Map<String, WorkingOrderDto> orders;

        private Map<String, Map<String, Map<String, Integer>>> processTimes;

        private List<String> givenOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewLayoutDto {

        private Map<String, StageDto> stages;

        @JsonProperty("transTimes")
        private Map<String, Map<String, Integer>> transitionTimes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageDto {

        @JsonProperty("Cells")
        private Map<String, ProductionCellDto> cells;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionCellDto {

        @JsonProperty("WIP_in")
        private int wipIn;

        @JsonProperty("WIP_out")
        private int wipOut;

        @JsonProperty("sug_w")
        private int suggestedWorkers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkersDto {

        private Map<String, Double> productivity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralDto {

        private int numOrders;
        private int numJobs;
        private int numStages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkingOrderDto {

        private String name;

        private Map<String, JobConnectDto> jobs = new HashMap<>();

        /*
         * Jackson does not know that the dynamic keys (e.g., "83114461-1032", "83569050-1048") should go into the jobs map. It will try to map them directly as fields in WorkingOrderDto because the keys are at the same level as "name".
         * With the JsonAnySetter we will enforce somehow to place the keys and connect them with the List of JobConnects
         */
        @JsonAnySetter
        public void addJobEntry(String key, Object value) {
            // Only include job entries that are not the 'name' field
            if (!"name".equals(key) && value instanceof Map) {
                JobConnectDto wrapper = new JobConnectDto();
                Map<?, ?> map = (Map<?, ?>) value;
                Object jobConnect = map.get("jobConnect");
                if (jobConnect instanceof List) {
                    wrapper.setJobConnect((List<String>) jobConnect);
                    jobs.put(key, wrapper);
                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobConnectDto {

        private List<String> jobConnect;
    }

}

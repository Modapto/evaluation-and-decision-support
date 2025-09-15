package gr.atc.modapto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "sew-production-schedules")
public class ProductionSchedule {

    @Id
    private String id;

    @Field(type = FieldType.Object)
    private Map<String, DailyData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyData {

        @Field(type = FieldType.Object)
        private NewLayout newLayout;

        @Field(type = FieldType.Object)
        private Workers workers;

        @Field(type = FieldType.Object)
        private General general;

        @Field(type = FieldType.Object)
        private Map<String, WorkingOrder> orders;

        @Field(type = FieldType.Object)
        private Map<String, Map<String, Map<String, Integer>>> processTimes;

        @Field(type = FieldType.Keyword)
        private List<String> givenOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewLayout {
        @Field(type = FieldType.Object)
        private Map<String, Stage> stages;

        @Field(type = FieldType.Object)
        private Map<String, Map<String, Integer>> transitionTimes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stage {
        @Field(type = FieldType.Object)
        private Map<String, ProductionCell> cells;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionCell {
        @Field(type = FieldType.Integer)
        private int wipIn;

        @Field(type = FieldType.Integer)
        private int wipOut;

        @Field(type = FieldType.Integer)
        private int suggestedWorkers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Workers {
        @Field(type = FieldType.Object)
        private Map<String, Double> productivity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class General {
        @Field(type = FieldType.Integer)
        private int numOrders;

        @Field(type = FieldType.Integer)
        private int numJobs;

        @Field(type = FieldType.Integer)
        private int numStages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkingOrder {
        @Field(type = FieldType.Text)
        private String name;

        @Field(type = FieldType.Object)
        private Map<String, JobConnect> jobs = new HashMap<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobConnect {
        @Field(type = FieldType.Keyword)
        private List<String> jobConnect;
    }
}

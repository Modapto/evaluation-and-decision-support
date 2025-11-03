package gr.atc.modapto.model.sew;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "sew-production-schedules")
public class ProductionSchedule {

    @Id
    private String id;

    @Field(type = FieldType.Flattened)
    private Map<String, DailyData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyData {

        @Field(type = FieldType.Object)
        private Object newLayout;

        @Field(type = FieldType.Object)
        private Object workers;

        @Field(type = FieldType.Object)
        private Object general;

        @Field(type = FieldType.Flattened)
        private Map<String, Object> orders;

        @Field(type = FieldType.Flattened)
        private Map<String, Map<String, Map<String, Integer>>> processTimes;

        @Field(type = FieldType.Keyword)
        private List<String> givenOrder;
    }
}

package gr.atc.modapto.model.sew;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "sew-current-environment")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewPlantEnvironment {

    @Id
    private String id;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestampCreated;

    @Field(type = FieldType.Flattened)
    private PlantData data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlantData {

        @Field(type = FieldType.Flattened)
        private Object stages;

        @Field(type = FieldType.Flattened)
        private Object transTimes;

    }
}

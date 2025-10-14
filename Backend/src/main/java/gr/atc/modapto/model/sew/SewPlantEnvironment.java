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
import java.util.Map;

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

    @Field(type = FieldType.Object)
    private Map<String, Stage> stages;

    @Field(type = FieldType.Object)
    private Map<String, Map<String, Integer>> transTimes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Stage {

        @Field(type = FieldType.Integer)
        private Integer wipIn;

        @Field(type = FieldType.Integer)
        private Integer wipOut;

        @Field(type = FieldType.Keyword)
        private String modules;

        @Field(type = FieldType.Object)
        private Map<String, Object> cells;
    }
}

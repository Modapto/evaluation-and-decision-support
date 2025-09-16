package gr.atc.modapto.model.sew;

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
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "sew-monitor-kpis-components")
public class SewMonitorKpisComponents {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestampCreated;

    @Field(type = FieldType.Object)
    private List<SewMonitorKpisComponentData> components;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SewMonitorKpisComponentData {

        @Field(type = FieldType.Keyword)
        private String stage;

        @Field(type = FieldType.Keyword)
        private String cell;

        @Field(type = FieldType.Keyword)
        private String plc;

        @Field(type = FieldType.Keyword)
        private String module;

        @Field(type = FieldType.Keyword)
        private String subElement;

        @Field(type = FieldType.Keyword)
        private String component;

        @Field(type = FieldType.Object)
        private List<Property> property;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Property {
        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Integer)
        private Integer lowThreshold;

        @Field(type = FieldType.Integer)
        private Integer highThreshold;
    }
}
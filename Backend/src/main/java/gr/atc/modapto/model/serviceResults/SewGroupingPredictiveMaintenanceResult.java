package gr.atc.modapto.model.serviceResults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "sew-grouping-predictive-maintenance-results")
public class SewGroupingPredictiveMaintenanceResult {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Keyword)
    private String smartServiceId;

    @Field(type = FieldType.Double)
    private Double costSavings;

    @Field(type = FieldType.Object)
    private TimeWindow timeWindow;

    @Field(type = FieldType.Object)
    private Map<String, List<MaintenanceComponent>> groupingMaintenance;

    @Field(type = FieldType.Object)
    private Map<String, List<MaintenanceComponent>> individualMaintenance;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MaintenanceComponent {

        @Field(type = FieldType.Integer)
        private Integer moduleId;

        @Field(type = FieldType.Text)
        private String module;

        @Field(type = FieldType.Double)
        private Double replacementTime;

        @Field(type = FieldType.Double)
        private Double duration;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TimeWindow {

        @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
        private LocalDateTime begin;

        @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
        private LocalDateTime end;
    }
}
package gr.atc.modapto.model.serviceResults;

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
@Document(indexName = "sew-threshold-predictive-maintenance-results")
public class SewThresholdBasedPredictiveMaintenanceResult {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Keyword)
    private String smartServiceId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String recommendation;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String details;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;
}
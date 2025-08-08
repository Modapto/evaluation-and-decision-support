package gr.atc.modapto.model;

import lombok.AllArgsConstructor;
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
@Document(indexName = "sew-maintenance-data")
public class MaintenanceData {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String stage = "";

    @Field(type = FieldType.Keyword)
    private String cell = "";

    @Field(type = FieldType.Keyword)
    private String faultyElementId = "";

    @Field(type = FieldType.Keyword)
    private String module = "";

    @Field(type = FieldType.Keyword)
    private String moduleId = "";

    @Field(type = FieldType.Keyword)
    private String component = "";

    @Field(type = FieldType.Keyword)
    private String componentId = "";

    @Field(type = FieldType.Keyword)
    private String failureType = "";

    @Field(type = FieldType.Keyword)
    private String failureDescription = "";

    @Field(type = FieldType.Keyword)
    private String maintenanceActionPerformed = "";

    @Field(type = FieldType.Keyword)
    private String workerName = "";

    @Field(type = FieldType.Keyword)
    private String componentReplacement = "";

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime tsRequestCreation;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime tsInterventionStarted;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime tsInterventionFinished;


}

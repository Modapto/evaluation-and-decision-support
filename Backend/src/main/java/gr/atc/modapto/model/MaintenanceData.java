package gr.atc.modapto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "sew_maintenance_data")
public class MaintenanceData {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String stage = "";

    @Field(type = FieldType.Keyword)
    private String cell = "";

    @Field(type = FieldType.Keyword)
    private String module = "";

    @Field(type = FieldType.Keyword)
    private String component = "";

    @Field(type = FieldType.Keyword)
    private String failureType = "";

    @Field(type = FieldType.Keyword)
    private String failureDescription = "";

    @Field(type = FieldType.Keyword)
    private String maintenanceActionPerformed = "";

    @Field(type = FieldType.Keyword)
    private String componentReplacement = "";

    @Field(type = FieldType.Keyword)
    private String componentName = "";

    @Field(type = FieldType.Keyword)
    private String tsRequestCreation = "";

    @Field(type = FieldType.Keyword)
    private String tsRequestAcknowledged = "";

    @Field(type = FieldType.Keyword)
    private String tsInterventionStarted = "";

    @Field(type = FieldType.Keyword)
    private String tsInterventionFinished = "";

    @Field(type = FieldType.Keyword)
    private String mtbf = "";

    @Field(type = FieldType.Keyword)
    private String mtbfStageLevel = "";

    @Field(type = FieldType.Keyword)
    private String durationCreationToAcknowledged = "";

    @Field(type = FieldType.Keyword)
    private String durationCreationToInterventionStart = "";

    @Field(type = FieldType.Keyword)
    private String durationInterventionStartedToFinished = "";

    @Field(type = FieldType.Keyword)
    private String totalDurationCreationToFinished = "";

    @Field(type = FieldType.Keyword)
    private String totalMaintenanceTimeAllocated = "";
}

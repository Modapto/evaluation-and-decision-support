package gr.atc.modapto.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "sew-component-list")
public class SewComponentInfo {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String stage;

    @Field(type = FieldType.Keyword)
    private String cell;

    @Field(type = FieldType.Keyword)
    private String module;

    @Field(type = FieldType.Keyword)
    private String moduleId;

    @Field(type = FieldType.Double)
    private Double alpha;

    @Field(type = FieldType.Double)
    private Double beta;

    @Field(name = "average_maintenance_duration", type = FieldType.Double)
    private Double averageMaintenanceDuration;

    @Field(type = FieldType.Double)
    private Double mtbf;

    @Field(name = "last_maintenance_action_time", type = FieldType.Keyword)
    private String lastMaintenanceActionTime;
}
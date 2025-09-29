package gr.atc.modapto.model.serviceResults;

import lombok.*;
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
@Document(indexName = "crf-kit-holder-notifications")
public class CrfKitHolderEvent{

    @Id
    public String id;

    @Field(type = FieldType.Keyword)
    public String moduleId;

    @Field(type = FieldType.Integer)
    public Integer eventType;

    @Field(type = FieldType.Integer)
    private Integer rfidStation;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Field(type = FieldType.Integer)
    private Integer khType;

    @Field(type = FieldType.Integer)
    private Integer khId;
}

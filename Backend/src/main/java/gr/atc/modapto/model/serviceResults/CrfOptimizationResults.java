package gr.atc.modapto.model.serviceResults;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "optimization-crf")
public class CrfOptimizationResults {

    @Id
    private String id;

    @Field(name = "timestamp", type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Field(name = "message", type = FieldType.Text)
    private String message;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;

    @Field(name = "optimization_results", type = FieldType.Flattened)
    private Object optimizationResults;

    @Field(name = "optimization_run", type = FieldType.Boolean)
    private Boolean optimizationRun;

    @Field(name = "solutionTime", type = FieldType.Long)
    private Long solutionTime;

    @Field(name = "totalTime",type = FieldType.Long)
    private Long totalTime;
}

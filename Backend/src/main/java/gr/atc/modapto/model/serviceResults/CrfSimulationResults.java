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
@Document(indexName = "simulation-crf")
public class CrfSimulationResults {

    @Id
    private String id;

    @Field(name = "timestamp", type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Field(type = FieldType.Text)
    private String message;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;

    @Field(name = "simulation_run", type = FieldType.Boolean)
    private Boolean simulationRun;

    @Field(type = FieldType.Long)
    private Long solutionTime;

    @Field(type = FieldType.Long)
    private Long totalTime;

    @Field(type = FieldType.Flattened)
    private Object baseline;

    @Field(name = "best_phase", type = FieldType.Flattened)
    private Object bestPhase;
}
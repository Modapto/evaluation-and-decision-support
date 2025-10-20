package gr.atc.modapto.model.serviceResults;

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
@Document(indexName = "simulation-sew")
public class SewSimulationResults {

    @Id
    private String id;

    @Field(name = "timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(name = "simulationData", type = FieldType.Flattened)
    private Object simulationData;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;
}

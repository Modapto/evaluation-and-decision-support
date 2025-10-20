package gr.atc.modapto.model.serviceResults;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "optimization-sew")
public class SewOptimizationResults {

    @Id
    private String id;

    @Field(name = "timestamp", type = FieldType.Keyword)
    private String timestamp;

    @Field(name = "moduleId", type = FieldType.Keyword)
    private String moduleId;

    @Field(name = "data", type = FieldType.Flattened)
    private Map<String, Object> data;
}

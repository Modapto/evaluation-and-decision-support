package gr.atc.modapto.model.crf;

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
@Document(indexName = "kitting-configs-sim")
public class CrfSimulationKittingConfig {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String filename;

    @Field(type = FieldType.Object)
    private Object config;
    
    @Field(type = FieldType.Keyword)
    private String uploadedAt;

    @Field(type = FieldType.Keyword)
    private String rawText;

    @Field(name = "case", type = FieldType.Keyword)
    private String configCase;

    @Field(type = FieldType.Keyword)
    private String etag;
}

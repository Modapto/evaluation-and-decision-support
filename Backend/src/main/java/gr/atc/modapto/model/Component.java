package gr.atc.modapto.model;

import java.time.LocalDate;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "components")
public class Component {

    @Field(type = FieldType.Keyword, name = "type")
    private String type;

    @Field(type = FieldType.Integer, name = "quantity")
    private Integer quantity;

    @Field(type = FieldType.Keyword, name = "pn")
    private String partNumber;

    @Field(type = FieldType.Date, name = "expectedDeliveryDate", pattern = "yyyy-MM-dd")
    private LocalDate expectedDeliveryDate;
}   

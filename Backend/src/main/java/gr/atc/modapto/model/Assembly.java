package gr.atc.modapto.model;


import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "assemblies")
public class Assembly {

    @Field(type = FieldType.Text, name = "type")
    private String type;

    @Field(type = FieldType.Integer, name = "quantity")
    private Integer quantity;

    @Field(type = FieldType.Text, name = "pn")
    private String partNumber;

    @Field(type = FieldType.Text, name = "expectedDeliveryDate")
    private String expectedDeliveryDate;
}   


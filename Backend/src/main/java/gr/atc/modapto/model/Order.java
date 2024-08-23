package gr.atc.modapto.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import gr.atc.modapto.enums.PilotCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "orders")
public class Order {

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "customer")
    private PilotCode customer;

    @Field(type = FieldType.Text, name = "documentNumber")
    private String documentNumber;

    @Field(type = FieldType.Nested, name = "orderof", includeInParent = true)
    private List<Assembly> assemblies;
    
    @Field(type = FieldType.Nested, name = "composedby", includeInParent = true)
    private List<Component> components;

    @Field(type = FieldType.Text, name = "comments")
    private String comments;
}

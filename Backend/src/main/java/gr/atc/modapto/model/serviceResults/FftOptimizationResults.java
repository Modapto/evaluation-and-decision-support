package gr.atc.modapto.model.serviceResults;

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
@Document(indexName = "optimization-fft")
public class FftOptimizationResults {

    @Id
    private String id;

    @Field(name = "timestamp", type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Field(name = "module", type = FieldType.Keyword)
    private String module;

    @Field(name = "optimizedCode_src", type = FieldType.Text)
    private String optimizedCodeSrc;

    @Field(name = "time_limit", type = FieldType.Integer)
    private Integer timeLimit;

    @Field(name = "robotConfiguration", type = FieldType.Flattened)
    private Object robotConfiguration;

    @Field(name = "time_difference", type = FieldType.Double)
    private Double timeDifference;

    @Field(name = "optimizedCode_dat", type = FieldType.Text)
    private String optimizedCodeDat;

    @Field(name = "energy_difference", type = FieldType.Double)
    private Double energyDifference;
}

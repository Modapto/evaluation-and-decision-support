package gr.atc.modapto.dto.crf;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import gr.atc.modapto.dto.BaseEventResultsDto;
import gr.atc.modapto.util.UnixTimestampConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "KH Events", description = "CRF KH Events associated with the Events CSV")
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrfKitHolderEventDto extends BaseEventResultsDto {

    @Schema(description = "ID of Kit Holder event in PKB (Automatically generated)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    public String id;

    @Schema(description = "Module ID", requiredMode = Schema.RequiredMode.REQUIRED)
    public String moduleId;

    @Min(1)
    @Max(2)
    @CsvBindByPosition(position = 1)
    public Integer eventType;

    @Min(1)
    @Max(9)
    @CsvBindByPosition(position = 2)
    private Integer rfidStation;

    @CsvCustomBindByPosition(position = 3, converter = UnixTimestampConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Min(1)
    @Max(3)
    @CsvBindByPosition(position = 4)
    private Integer khType;

    @Min(1)
    @Max(999999)
    @CsvBindByPosition(position = 5)
    private Integer khId;
}

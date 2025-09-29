package gr.atc.modapto.dto.serviceResults.crf;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.atc.modapto.dto.BaseEventResultsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CrfKhEventNotifications", description = "KH Event Results from Self-Awareness or Workers registration process ")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class CrfKhEventNotificationDto extends BaseEventResultsDto {

    @JsonProperty("time_window")
    @JsonFormat(pattern = "YYYY-MM-DD'T'hh:mm:ss")
    private LocalDateTime timeWindow;

    private String moduleId;

    private Double force;

    private Double threshold;

    @JsonProperty("kh_info")
    private KitHolderInfo khInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class KitHolderInfo{

        private Integer type;

        private Integer id;
    }
}

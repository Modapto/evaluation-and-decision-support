package gr.atc.modapto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModaptoModuleDto {

    private String id;
    
    private String moduleId;
    
    private String name;
    
    private String endpoint;

    private LocalDateTime timestampDt;

    private Long timestampElastic;
    
    private List<SmartServiceDto> smartServices;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SmartServiceDto {
        private String name;
        private String catalogueId;
        private String serviceId;
        private String endpoint;
    }
}
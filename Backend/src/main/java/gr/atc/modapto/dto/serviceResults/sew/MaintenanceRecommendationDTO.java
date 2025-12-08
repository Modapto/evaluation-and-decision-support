package gr.atc.modapto.dto.serviceResults.sew;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceRecommendationDTO {
    private Integer duration;
    private String cell;
    private String recommendation;
    private String moduleID;
    private LocalDateTime timestamp;
}
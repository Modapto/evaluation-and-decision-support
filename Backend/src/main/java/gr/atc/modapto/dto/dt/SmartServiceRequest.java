package gr.atc.modapto.dto.dt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmartServiceRequest {

    // Generic Pattern - Mostly for UL Services
    private String request;

    // Used for AUEB services
    @Builder.Default
    private String data = null;

    @Builder.Default
    private String route = null;
}

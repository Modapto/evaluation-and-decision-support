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
    private Base64Data data = null; // Base64 Encoding format of Input

    @Builder.Default
    private String route = null;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Base64Data{
        private String base64;
    }
}

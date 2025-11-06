package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceInvocations.FftSustainabilityAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceResults.fft.FftSustainabilityAnalyticsResultsDto;
import gr.atc.modapto.service.FftSustainabilityAnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@WebMvcTest(SustainabilityAnalyticsController.class)
@ActiveProfiles("test")
@DisplayName("SustainabilityAnalyticsController Tests")
class SustainabilityAnalyticsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FftSustainabilityAnalyticsService fftSustainabilityAnalyticsService;

    @Nested
    @DisplayName("Invoke FFT Sustainability Analytics")
    class InvokeFftSustainabilityAnalytics {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke FFT sustainability analytics : Success")
        void givenValidInput_whenInvokeSustainabilityAnalytics_thenReturnsSuccess() throws Exception {
            // Given
            FftSustainabilityAnalyticsInputDto inputDto = FftSustainabilityAnalyticsInputDto.builder()
                    .build();
            GlobalRequestDto<FftSustainabilityAnalyticsInputDto> request = GlobalRequestDto.<FftSustainabilityAnalyticsInputDto>builder()
                    .input(inputDto)
                    .moduleId("fft_module_1")
                    .smartServiceId("service_1")
                    .build();

            FftSustainabilityAnalyticsResultsDto mockResult = FftSustainabilityAnalyticsResultsDto.builder()
                    .robotUsedPower(100.0)
                    .robotUsedEnergy(10.0)
                    .timestampStart(1672531200.0)
                    .timestampStop(1672534800.0)
                    .measurementState(1.0)
                    .build();

            when(fftSustainabilityAnalyticsService.extractFftSustainabilityAnalytics(any(GlobalRequestDto.class))).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(post("/api/eds/sustainability-analytics/pilots/fft/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Sustainability Analytics data extracted successfully"))
                    .andExpect(jsonPath("$.data").isNotEmpty());

            verify(fftSustainabilityAnalyticsService).extractFftSustainabilityAnalytics(any(GlobalRequestDto.class));
        }
    }
}

package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.service.interfaces.IOptimizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OptimizationController.class)
@ActiveProfiles("test")
@DisplayName("OptimizationController Tests")
class OptimizationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IOptimizationService<CrfOptimizationResultsDto> crfOptimizationService;

    @MockitoBean
    private IOptimizationService<SewOptimizationResultsDto> sewOptimizationService;

    @Nested
    @DisplayName("Retrieve Latest CRF Optimization Results")
    class RetrieveLatestCrfResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results : Success")
        void givenValidRequest_whenRetrieveLatestCrfResults_thenReturnsSuccess() throws Exception {
            // Given
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .message("Optimization completed successfully")
                    .build();
            when(crfOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.timestamp").value("2024-01-15T10:30:00.000Z"));

            verify(crfOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest CRF results as Admin : Success")
        void givenValidRequestAsAdmin_whenRetrieveLatestCrfResults_thenReturnsSuccess() throws Exception {
            // Given
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .message("Admin access successful")
                    .build();
            when(crfOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results retrieved successfully"));

            verify(crfOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @DisplayName("Retrieve latest CRF results : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestCrfResults_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest CRF Optimization Results by Production Module")
    class RetrieveLatestCrfResultsByProductionModule {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results by module : Success")
        void givenValidModuleId_whenRetrieveLatestCrfResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "crf_module_1";
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .message("Module optimization completed")
                    .build();
            when(crfOptimizationService.retrieveLatestOptimizationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results for Module " + moduleId + " retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(crfOptimizationService).retrieveLatestOptimizationResultsByProductionModule(moduleId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest CRF results by module as Admin : Success")
        void givenValidModuleIdAsAdmin_whenRetrieveLatestCrfResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "admin_module";
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .message("Admin module access successful")
                    .build();
            when(crfOptimizationService.retrieveLatestOptimizationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results for Module " + moduleId + " retrieved successfully"));

            verify(crfOptimizationService).retrieveLatestOptimizationResultsByProductionModule(moduleId);
        }

        @Test
        @DisplayName("Retrieve latest CRF results by module : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestCrfResultsByProductionModule_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/modules/test_module/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest SEW Optimization Results")
    class RetrieveLatestSewResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest SEW results : Success")
        void givenValidRequest_whenRetrieveLatestSewResults_thenReturnsSuccess() throws Exception {
            // Given
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .data(createSampleData())
                    .build();
            when(sewOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.timestamp").value("2024-01-15T10:30:00.000Z"));

            verify(sewOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest SEW results as Admin : Success")
        void givenValidRequestAsAdmin_whenRetrieveLatestSewResults_thenReturnsSuccess() throws Exception {
            // Given
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .data(createSampleData())
                    .build();
            when(sewOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results retrieved successfully"));

            verify(sewOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @DisplayName("Retrieve latest SEW results : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestSewResults_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest SEW Optimization Results by Production Module")
    class RetrieveLatestSewResultsByProductionModule {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest SEW results by module : Success")
        void givenValidModuleId_whenRetrieveLatestSewResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "sew_module_1";
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .data(createSampleData())
                    .build();
            when(sewOptimizationService.retrieveLatestOptimizationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results for Module " + moduleId + " retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(sewOptimizationService).retrieveLatestOptimizationResultsByProductionModule(moduleId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest SEW results by module as Admin : Success")
        void givenValidModuleIdAsAdmin_whenRetrieveLatestSewResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "admin_sew_module";
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .data(createSampleData())
                    .build();
            when(sewOptimizationService.retrieveLatestOptimizationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results for Module " + moduleId + " retrieved successfully"));

            verify(sewOptimizationService).retrieveLatestOptimizationResultsByProductionModule(moduleId);
        }

        @Test
        @DisplayName("Retrieve latest SEW results by module : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestSewResultsByProductionModule_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/modules/test_module/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    /*
     * Helper methods
     */
    private Map<String, SewOptimizationResultsDto.SolutionData> createSampleData() {
        // Create DTO structure matching the exact model structure
        Map<String, SewOptimizationResultsDto.SolutionData> data = new HashMap<>();

        // Create MetricsData for DTO
        SewOptimizationResultsDto.MetricsData metricsDto = new SewOptimizationResultsDto.MetricsData("240");

        // Create seq map for DTO
        Map<String, Map<String, String>> seqDto = new HashMap<>();
        Map<String, String> seqItemDto = new HashMap<>();
        seqItemDto.put("operation", "cutting");
        seqItemDto.put("duration", "30");
        seqDto.put("seq_1", seqItemDto);

        // Create orders map for DTO
        Map<String, Map<String, Map<String, Map<String, SewOptimizationResultsDto.TimeRange>>>> ordersDto = new HashMap<>();

        // Create init order for DTO
        List<String> initOrderDto = Arrays.asList("order_1", "order_2", "order_3");

        // Create SolutionData for DTO
        SewOptimizationResultsDto.SolutionData solutionDataDto = new SewOptimizationResultsDto.SolutionData(
                metricsDto, seqDto, ordersDto, initOrderDto
        );

        data.put("solution_1", solutionDataDto);
        return data;
    }
}

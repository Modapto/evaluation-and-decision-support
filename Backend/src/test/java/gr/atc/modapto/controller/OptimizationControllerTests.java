package gr.atc.modapto.controller;

import gr.atc.modapto.dto.crf.CrfOptimizationKittingConfigDto;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceInvocations.FftOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.fft.FftOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.service.interfaces.IKhPickingSequenceOptimizationService;
import gr.atc.modapto.service.interfaces.IProductionScheduleOptimizationService;
import gr.atc.modapto.service.interfaces.IRobotConfigurationOptimizationService;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

@WebMvcTest(OptimizationController.class)
@ActiveProfiles("test")
@DisplayName("OptimizationController Tests")
class OptimizationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IKhPickingSequenceOptimizationService khPickingSequenceOptimizationService;

    @MockitoBean
    private IProductionScheduleOptimizationService productionScheduleOptimizationService;

    @MockitoBean
    private IRobotConfigurationOptimizationService robotConfigurationOptimizationService;

    @Nested
    @DisplayName("Retrieve Latest CRF Optimization Results")
    class RetrieveLatestCrfResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results : Success")
        void givenValidRequest_whenRetrieveLatestCrfResults_thenReturnsSuccess() throws Exception {
            // Given
            LocalDateTime testTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp(testTimestamp)
                    .message("Optimization completed successfully")
                    .build();
            when(khPickingSequenceOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/crf/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(khPickingSequenceOptimizationService).retrieveLatestOptimizationResults();
        }
    }

    @Nested
    @DisplayName("Retrieve Latest CRF Optimization Results by Module ID")
    class RetrieveLatestCrfResultsByModuleId {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results by module : Success")
        void givenValidModuleId_whenRetrieveLatestCrfResultsByModuleId_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "crf_module_1";
            LocalDateTime testTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp(testTimestamp)
                    .message("Module optimization completed")
                    .build();
            when(khPickingSequenceOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId)).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/crf/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results for Module " + moduleId + " retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(khPickingSequenceOptimizationService).retrieveLatestOptimizationResultsByModuleId(moduleId);
        }
    }

    @Nested
    @DisplayName("Invoke CRF KH Picking Sequence Optimization")
    class InvokeKhPickingSequenceOptimization {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke KH picking sequence optimization : Success")
        void givenValidInput_whenInvokeOptimizationOfKhPickingSequence_thenReturnsSuccess() throws Exception {
            CrfInvocationInputDto inputDto = CrfInvocationInputDto.builder()
                    .moduleId("crf_module_1")
                    .smartServiceId("service_1")
                    .build();

            mockMvc.perform(post("/api/eds/optimization/pilots/crf/kh-picking-sequence/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Request for optimization of CRF KH Picking Sequence has been successfully submitted"));

            verify(khPickingSequenceOptimizationService).invokeOptimizationOfKhPickingSequence(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Optimization Kitting Config")
    class RetrieveOptimizationKittingConfig {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve optimization kitting config : Success")
        void givenValidRequest_whenRetrieveOptimizationKittingConfig_thenReturnsConfig() throws Exception {
            // Given
            CrfOptimizationKittingConfigDto mockConfig = new CrfOptimizationKittingConfigDto();
            mockConfig.setId("opt-current");
            mockConfig.setFilename("optimization-config.json");
            mockConfig.setUploadedAt("2025-01-30T14:30:00Z");
            mockConfig.setConfigCase("production");

            when(khPickingSequenceOptimizationService.retrieveOptimizationKittingConfig()).thenReturn(mockConfig);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/kitting-configs")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Kitting Configs retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("opt-current"))
                    .andExpect(jsonPath("$.data.filename").value("optimization-config.json"));

            verify(khPickingSequenceOptimizationService).retrieveOptimizationKittingConfig();
        }
    }

    @Nested
    @DisplayName("Retrieve Latest SEW Optimization Results")
    class RetrieveLatestSewResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest SEW results : Success")
        void givenValidRequest_whenRetrieveLatestSewResults_thenReturnsSuccess() throws Exception {
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .data(createSampleData())
                    .build();
            when(productionScheduleOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/sew/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(productionScheduleOptimizationService).retrieveLatestOptimizationResults();
        }
    }

    @Nested
    @DisplayName("Retrieve Latest SEW Optimization Results by Module ID")
    class RetrieveLatestSewResultsByModuleId {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest SEW results by module : Success")
        void givenValidModuleId_whenRetrieveLatestSewResultsByModuleId_thenReturnsSuccess() throws Exception {
            String moduleId = "sew_module_1";
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .data(createSampleData())
                    .build();
            when(productionScheduleOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId)).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/sew/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results for Module " + moduleId + " retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(productionScheduleOptimizationService).retrieveLatestOptimizationResultsByModuleId(moduleId);
        }
    }

    @Nested
    @DisplayName("Upload SEW Production Schedule")
    class UploadSewProductionSchedule {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload SEW production schedule : Success")
        void givenValidSchedule_whenUploadSewProductionSchedule_thenReturnsSuccess() throws Exception {
            SewProductionScheduleDto scheduleDto = new SewProductionScheduleDto();

            mockMvc.perform(post("/api/eds/optimization/pilots/sew/schedules/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduleDto))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SEW Production Schedule has been successfully uploaded"));

            verify(productionScheduleOptimizationService).uploadProductionSchedule(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest SEW Production Schedule")
    class RetrieveLatestSewProductionSchedule {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest SEW production schedule : Success")
        void givenValidRequest_whenRetrieveLatestProductionSchedule_thenReturnsSuccess() throws Exception {
            SewProductionScheduleDto mockSchedule = new SewProductionScheduleDto();
            when(productionScheduleOptimizationService.retrieveLatestProductionSchedule()).thenReturn(mockSchedule);

            mockMvc.perform(get("/api/eds/optimization/pilots/sew/schedules/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Production Schedule retrieved successfully"));

            verify(productionScheduleOptimizationService).retrieveLatestProductionSchedule();
        }
    }

    @Nested
    @DisplayName("Invoke SEW Production Schedule Optimization")
    class InvokeSewProductionScheduleOptimization {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke SEW optimization : Success")
        void givenValidInput_whenInvokeOptimizationOfProductionSchedules_thenReturnsSuccess() throws Exception {
            SewOptimizationInputDto inputDto = SewOptimizationInputDto.builder()
                    .moduleId("sew_module_1")
                    .smartServiceId("service_1")
                    .build();

            mockMvc.perform(post("/api/eds/optimization/pilots/sew/schedules/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Request for optimization of SEW production schedules has been successfully submitted"));

            verify(productionScheduleOptimizationService).invokeOptimizationOfProductionSchedules(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest FFT Optimization Results")
    class RetrieveLatestFftResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest FFT results : Success")
        void givenValidRequest_whenRetrieveLatestFftResults_thenReturnsSuccess() throws Exception {
            // Given
            LocalDateTime testTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            FftOptimizationResultsDto mockResult = FftOptimizationResultsDto.builder()
                    .timestamp(testTimestamp)
                    .build();
            when(robotConfigurationOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/fft/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest FFT Optimization results retrieved successfully"));

            verify(robotConfigurationOptimizationService).retrieveLatestOptimizationResults();
        }
    }

    @Nested
    @DisplayName("Retrieve Latest FFT Optimization Results by Module ID")
    class RetrieveLatestFftResultsByModuleId {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest FFT results by module : Success")
        void givenValidModuleId_whenRetrieveLatestFftResultsByModuleId_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "fft_module_1";
            LocalDateTime testTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            FftOptimizationResultsDto mockResult = FftOptimizationResultsDto.builder()
                    .timestamp(testTimestamp)
                    .build();
            when(robotConfigurationOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/fft/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest FFT Optimization results for Module " + moduleId + " retrieved successfully"));

            verify(robotConfigurationOptimizationService).retrieveLatestOptimizationResultsByModuleId(moduleId);
        }
    }

    @Nested
    @DisplayName("Invoke FFT Robot Configuration Optimization")
    class InvokeFftRobotConfigurationOptimization {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke FFT robot configuration optimization : Success")
        void givenValidInput_whenInvokeRobotConfigurationOptimization_thenReturnsSuccess() throws Exception {
            // Given
            FftOptimizationInputDto inputDto = FftOptimizationInputDto.builder()
                    .moduleId("fft_module_1")
                    .smartServiceId("service_1")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/optimization/pilots/fft/robot-configuration/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Request for optimization of FFT Robot Configuration has been successfully submitted"));

            verify(robotConfigurationOptimizationService).invokeOptimizationOfRobotConfiguration(any());
        }
    }

    /*
     * Helper methods
     */
    private Map<String, Object> createSampleData() {
        Map<String, Object> solution = new HashMap<>();
        solution.put("schedule", "optimized_schedule_data");
        solution.put("cost", 1000.0);

        Map<String, Object> data = new HashMap<>();
        data.put("solution_1", solution);
        return data;
    }
}
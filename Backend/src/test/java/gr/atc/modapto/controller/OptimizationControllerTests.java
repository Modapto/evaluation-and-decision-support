package gr.atc.modapto.controller;

import gr.atc.modapto.dto.crf.CrfOptimizationKittingConfigDto;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.service.interfaces.IKhPickingSequenceOptimizationService;
import gr.atc.modapto.service.interfaces.IProductionScheduleOptimizationService;
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
import java.time.format.DateTimeFormatter;
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

    @Nested
    @DisplayName("Retrieve Latest CRF Optimization Results")
    class RetrieveLatestCrfResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results : Success")
        void givenValidRequest_whenRetrieveLatestCrfResults_thenReturnsSuccess() throws Exception {
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .message("Optimization completed successfully")
                    .build();
            when(khPickingSequenceOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/crf/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.timestamp").value("2024-01-15T10:30:00.000Z"));

            verify(khPickingSequenceOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest CRF results as Admin : Success")
        void givenValidRequestAsAdmin_whenRetrieveLatestCrfResults_thenReturnsSuccess() throws Exception {
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .message("Admin access successful")
                    .build();
            when(khPickingSequenceOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/crf/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results retrieved successfully"));

            verify(khPickingSequenceOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @DisplayName("Retrieve latest CRF results : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestCrfResults_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest CRF Optimization Results by Module ID")
    class RetrieveLatestCrfResultsByModuleId {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results by module : Success")
        void givenValidModuleId_whenRetrieveLatestCrfResultsByModuleId_thenReturnsSuccess() throws Exception {
            String moduleId = "crf_module_1";
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
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

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest CRF results by module as Admin : Success")
        void givenValidModuleIdAsAdmin_whenRetrieveLatestCrfResultsByModuleId_thenReturnsSuccess() throws Exception {
            String moduleId = "admin_module";
            CrfOptimizationResultsDto mockResult = CrfOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .message("Admin module access successful")
                    .build();
            when(khPickingSequenceOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId)).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/crf/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Optimization results for Module " + moduleId + " retrieved successfully"));

            verify(khPickingSequenceOptimizationService).retrieveLatestOptimizationResultsByModuleId(moduleId);
        }

        @Test
        @DisplayName("Retrieve latest CRF results by module : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestCrfResultsByModuleId_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/modules/test_module/latest"))
                    .andExpect(status().isUnauthorized());
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

        @Test
        @DisplayName("Invoke KH picking sequence optimization : Unauthorized")
        void givenUnauthorizedRequest_whenInvokeOptimizationOfKhPickingSequence_thenReturnsUnauthorized() throws Exception {
            CrfInvocationInputDto inputDto = CrfInvocationInputDto.builder()
                    .moduleId("crf_module_1")
                    .smartServiceId("service_1")
                    .build();

            mockMvc.perform(post("/api/eds/optimization/pilots/crf/kh-picking-sequence/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isForbidden());
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            CrfOptimizationKittingConfigDto mockConfig = new CrfOptimizationKittingConfigDto();
            mockConfig.setId("opt-current");
            mockConfig.setFilename("optimization-config.json");
            mockConfig.setUploadedAt(LocalDateTime.parse("2025-01-30T14:30:00Z", formatter));
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

        @Test
        @DisplayName("Retrieve optimization kitting config : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveOptimizationKittingConfig_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/optimization/pilots/crf/kitting-configs"))
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
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.timestamp").value("2024-01-15T10:30:00.000Z"));

            verify(productionScheduleOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest SEW results as Admin : Success")
        void givenValidRequestAsAdmin_whenRetrieveLatestSewResults_thenReturnsSuccess() throws Exception {
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .data(createSampleData())
                    .build();
            when(productionScheduleOptimizationService.retrieveLatestOptimizationResults()).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/sew/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results retrieved successfully"));

            verify(productionScheduleOptimizationService).retrieveLatestOptimizationResults();
        }

        @Test
        @DisplayName("Retrieve latest SEW results : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestSewResults_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/latest"))
                    .andExpect(status().isUnauthorized());
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

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest SEW results by module as Admin : Success")
        void givenValidModuleIdAsAdmin_whenRetrieveLatestSewResultsByModuleId_thenReturnsSuccess() throws Exception {
            String moduleId = "admin_sew_module";
            SewOptimizationResultsDto mockResult = SewOptimizationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .data(createSampleData())
                    .build();
            when(productionScheduleOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId)).thenReturn(mockResult);

            mockMvc.perform(get("/api/eds/optimization/pilots/sew/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Optimization results for Module " + moduleId + " retrieved successfully"));

            verify(productionScheduleOptimizationService).retrieveLatestOptimizationResultsByModuleId(moduleId);
        }

        @Test
        @DisplayName("Retrieve latest SEW results by module : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestSewResultsByModuleId_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/modules/test_module/latest"))
                    .andExpect(status().isUnauthorized());
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

        @Test
        @DisplayName("Upload SEW production schedule : Unauthorized")
        void givenUnauthorizedRequest_whenUploadSewProductionSchedule_thenReturnsUnauthorized() throws Exception {
            SewProductionScheduleDto scheduleDto = new SewProductionScheduleDto();

            mockMvc.perform(post("/api/eds/optimization/pilots/sew/schedules/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scheduleDto)))
                    .andExpect(status().isForbidden());
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

        @Test
        @DisplayName("Retrieve latest SEW production schedule : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestProductionSchedule_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/eds/optimization/pilots/sew/schedules/latest"))
                    .andExpect(status().isUnauthorized());
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


        @Test
        @DisplayName("Invoke SEW optimization : Unauthorized")
        void givenUnauthorizedRequest_whenInvokeOptimizationOfProductionSchedules_thenReturnsUnauthorized() throws Exception {
            SewOptimizationInputDto inputDto = SewOptimizationInputDto.builder()
                    .moduleId("sew_module_1")
                    .smartServiceId("service_1")
                    .build();

            mockMvc.perform(post("/api/eds/optimization/pilots/sew/schedules/optimize")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isForbidden());
        }
    }

    /*
     * Helper methods
     */
    private Map<String, SewOptimizationResultsDto.SolutionData> createSampleData() {
        Map<String, SewOptimizationResultsDto.SolutionData> data = new HashMap<>();

        SewOptimizationResultsDto.KpiData kpiData = new SewOptimizationResultsDto.KpiData();
        kpiData.setMakespan("240");
        kpiData.setMachineUtilization(0.85);

        Map<String, Map<String, SewOptimizationResultsDto.OrderData>> scheduleData = new HashMap<>();
        Map<String, SewOptimizationResultsDto.OrderData> daySchedule = new HashMap<>();

        SewOptimizationResultsDto.OrderData orderData = new SewOptimizationResultsDto.OrderData();
        orderData.setOrderId("order_1");

        orderData.setMachines(new HashMap<>());

        daySchedule.put("order_1", orderData);
        scheduleData.put("day_1", daySchedule);

        SewOptimizationResultsDto.SolutionData solutionData = new SewOptimizationResultsDto.SolutionData();
        solutionData.setKpis(kpiData);
        solutionData.setSchedule(scheduleData);

        data.put("solution_1", solutionData);
        return data;
    }
}

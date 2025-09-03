package gr.atc.modapto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.service.interfaces.ISelfAwarenessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SelfAwarenessController.class)
@ActiveProfiles("test")
@DisplayName("SelfAwarenessController Tests")
class SelfAwarenessControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ISelfAwarenessService selfAwarenessService;

    @Nested
    @DisplayName("Invoke Self-Awareness Monitoring KPIs Process")
    class InvokeSelfAwarenessMonitoringKpisProcess {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke monitoring KPIs : Success")
        void givenValidInput_whenInvokeMonitoringKpis_thenReturnsSuccess() throws Exception {
            // Given
            SewSelfAwarenessMonitoringKpisInputDto inputData = createValidInputData("TEST_MODULE");

            doNothing().when(selfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any());

            // When & Then
            mockMvc.perform(post("/api/eds/self-awareness/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputData))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Self-Awareness Monitoring KPIs algorithm invoked successfully"));

            verify(selfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any(SewSelfAwarenessMonitoringKpisInputDto.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Invoke monitoring KPIs as admin : Success")
        void givenValidInputAsAdmin_whenInvokeMonitoringKpis_thenReturnsSuccess() throws Exception {
            // Given
            SewSelfAwarenessMonitoringKpisInputDto inputData = createValidInputData("ADMIN_MODULE");

            doNothing().when(selfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any());

            // When & Then
            mockMvc.perform(post("/api/eds/self-awareness/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputData))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(selfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any(SewSelfAwarenessMonitoringKpisInputDto.class));
        }

        @Test
        @DisplayName("Invoke monitoring KPIs : Unauthorized")
        void givenNoAuthentication_whenInvokeMonitoringKpis_thenReturnsUnauthorized() throws Exception {
            // Given
            SewSelfAwarenessMonitoringKpisInputDto inputData = createValidInputData("TEST_MODULE");

            // When & Then
            mockMvc.perform(post("/api/eds/self-awareness/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputData))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(selfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke monitoring KPIs : Validation error - Missing moduleId")
        void givenMissingModuleId_whenInvokeMonitoringKpis_thenReturnsValidationError() throws Exception {
            // Given
            SewSelfAwarenessMonitoringKpisInputDto invalidInput = SewSelfAwarenessMonitoringKpisInputDto.builder()
                    .smartServiceId("SELF_AWARENESS_SERVICE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/self-awareness/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidInput))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(selfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke monitoring KPIs : Validation error - Empty moduleId")
        void givenEmptyModuleId_whenInvokeMonitoringKpis_thenReturnsValidationError() throws Exception {
            // Given
            SewSelfAwarenessMonitoringKpisInputDto invalidInput = SewSelfAwarenessMonitoringKpisInputDto.builder()
                    .moduleId("")
                    .smartServiceId("SELF_AWARENESS_SERVICE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/self-awareness/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidInput))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(selfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Self-Awareness Monitoring KPIs Results")
    class RetrieveLatestSelfAwarenessMonitoringKpisResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest results : Success")
        void givenExistingResults_whenRetrieveLatest_thenReturnsLatestResult() throws Exception {
            // Given
            SewSelfAwarenessMonitoringKpisResultsDto expectedResult = createSampleResultDto("test-id", "TEST_MODULE");

            when(selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults())
                    .thenReturn(expectedResult);

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("test-id"))
                    .andExpect(jsonPath("$.data.moduleId").value("TEST_MODULE"))
                    .andExpect(jsonPath("$.data.smartServiceId").value("Smart-Service-1"))
                    .andExpect(jsonPath("$.message").value("Latest Self-Awareness Monitoring KPIs results retrieved successfully"));

            verify(selfAwarenessService).retrieveLatestSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @DisplayName("Retrieve latest results : Unauthorized")
        void givenNoAuthentication_whenRetrieveLatest_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/latest"))
                    .andExpect(status().isUnauthorized());

            verify(selfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest results : JSON content type")
        void givenValidRequest_whenRetrieveLatest_thenReturnsJsonContentType() throws Exception {
            // Given
            SewSelfAwarenessMonitoringKpisResultsDto expectedResult = createSampleResultDto("test-id", "TEST_MODULE");

            when(selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults())
                    .thenReturn(expectedResult);

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/latest"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", "application/json"));
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Self-Awareness Monitoring KPIs Results by Module ID")
    class RetrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest results by module ID : Success")
        void givenValidModuleId_whenRetrieveLatestByModuleId_thenReturnsLatestResult() throws Exception {
            // Given
            String moduleId = "TEST_MODULE";
            SewSelfAwarenessMonitoringKpisResultsDto expectedResult = createSampleResultDto("test-id", moduleId);

            when(selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId))
                    .thenReturn(expectedResult);

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/{moduleId}/latest", moduleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("test-id"))
                    .andExpect(jsonPath("$.data.moduleId").value(moduleId))
                    .andExpect(jsonPath("$.message").value("Latest Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"));

            verify(selfAwarenessService).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest results by module ID : Validation error - Empty moduleId")
        void givenEmptyModuleId_whenRetrieveLatestByModuleId_thenReturnsValidationError() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/modules/{moduleId}/latest", ""))
                    .andExpect(status().isBadRequest()); // Empty path variable results in 404

            verify(selfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }

        @Test
        @DisplayName("Retrieve latest results by module ID : Unauthorized")
        void givenNoAuthentication_whenRetrieveLatestByModuleId_thenReturnsUnauthorized() throws Exception {
            // Given
            String moduleId = "TEST_MODULE";

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/{moduleId}/latest", moduleId))
                    .andExpect(status().isUnauthorized());

            verify(selfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }
    }

    @Nested
    @DisplayName("Retrieve All Self-Awareness Monitoring KPIs Results")
    class RetrieveAllSelfAwarenessMonitoringKpisResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results : Success")
        void givenExistingResults_whenRetrieveAll_thenReturnsAllResults() throws Exception {
            // Given
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Arrays.asList(
                    createSampleResultDto("test-id-1", "MODULE_1"),
                    createSampleResultDto("test-id-2", "MODULE_2")
            );

            when(selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults())
                    .thenReturn(expectedResults);

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].id").value("test-id-1"))
                    .andExpect(jsonPath("$.data[1].id").value("test-id-2"))
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results retrieved successfully"));

            verify(selfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results : Empty list")
        void givenNoResults_whenRetrieveAll_thenReturnsEmptyList() throws Exception {
            // Given
            when(selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults())
                    .thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results retrieved successfully"));

            verify(selfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @DisplayName("Retrieve all results : Unauthorized")
        void givenNoAuthentication_whenRetrieveAll_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results"))
                    .andExpect(status().isUnauthorized());

            verify(selfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResults();
        }
    }

    @Nested
    @DisplayName("Retrieve All Self-Awareness Monitoring KPIs Results by Module ID")
    class RetrieveAllSelfAwarenessMonitoringKpisResultsByModuleId {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results by module ID : Success")
        void givenValidModuleId_whenRetrieveAllByModuleId_thenReturnsAllResultsForModule() throws Exception {
            // Given
            String moduleId = "TEST_MODULE";
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Arrays.asList(
                    createSampleResultDto("test-id-1", moduleId),
                    createSampleResultDto("test-id-2", moduleId)
            );

            when(selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId))
                    .thenReturn(expectedResults);

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/{moduleId}", moduleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].moduleId").value(moduleId))
                    .andExpect(jsonPath("$.data[1].moduleId").value(moduleId))
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"));

            verify(selfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results by module ID : Empty list")
        void givenNonExistentModuleId_whenRetrieveAllByModuleId_thenReturnsEmptyList() throws Exception {
            // Given
            String moduleId = "NON_EXISTENT_MODULE";
            when(selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId))
                    .thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/{moduleId}", moduleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"));

            verify(selfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);
        }

        @Test
        @DisplayName("Retrieve all results by module ID : Unauthorized")
        void givenNoAuthentication_whenRetrieveAllByModuleId_thenReturnsUnauthorized() throws Exception {
            // Given
            String moduleId = "TEST_MODULE";

            // When & Then
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/{moduleId}", moduleId))
                    .andExpect(status().isUnauthorized());

            verify(selfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Access endpoints : Authentication required")
        void givenNoAuthentication_whenAccessEndpoints_thenReturnsUnauthorized() throws Exception {
            // Test invoke endpoint
            SewSelfAwarenessMonitoringKpisInputDto inputData = SewSelfAwarenessMonitoringKpisInputDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("SELF_AWARENESS_SERVICE")
                    .build();

            mockMvc.perform(post("/api/eds/self-awareness/monitor-kpis/invoke")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputData))
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Test retrieve endpoints
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/latest"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/TEST/latest"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/results/TEST"))
                    .andExpect(status().isUnauthorized());

            verify(selfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
            verify(selfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResults();
            verify(selfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(any());
            verify(selfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResults();
            verify(selfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }
    }

    private SewSelfAwarenessMonitoringKpisInputDto createValidInputData(String moduleId) {
        SewMonitorKpisComponentsDto components = SewMonitorKpisComponentsDto.builder()
                .stage("Stage1")
                .cell("Cell1")
                .plc("PLC1")
                .module("Module1")
                .subElement("SubElement1")
                .component("Component1")
                .property(Collections.singletonList(
                        SewMonitorKpisComponentsDto.PropertyDto.builder()
                                .name("Temperature")
                                .lowThreshold(10)
                                .highThreshold(80)
                                .build()
                ))
                .build();

        return SewSelfAwarenessMonitoringKpisInputDto.builder()
                .moduleId(moduleId)
                .smartServiceId("SELF_AWARENESS_SERVICE")
                .startDate("Start_Date")
                .endDate("End_Date")
                .components(List.of(components))
                .build();
    }

    private SewSelfAwarenessMonitoringKpisResultsDto createSampleResultDto(String id, String moduleId) {
        return SewSelfAwarenessMonitoringKpisResultsDto.builder()
                .id(id)
                .moduleId(moduleId)
                .smartServiceId("Smart-Service-1")
                .timestamp("2024-01-15T10:30:00Z")
                .ligne("Line1")
                .component("Component1")
                .variable("Temperature")
                .startingDate("2024-01-15 00:00:00")
                .endingDate("2024-01-15 23:59:59")
                .dataSource("Influx DB")
                .bucket("BUC")
                .data(Arrays.asList(25.5, 26.1, 27.3, 28.0))
                .build();
    }
}
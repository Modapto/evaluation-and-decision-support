package gr.atc.modapto.controller;

import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.service.interfaces.ISimulationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SimulationController.class)
@ActiveProfiles("test")
@DisplayName("SimulationController Tests")
class SimulationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISimulationService<CrfSimulationResultsDto> crfSimulationService;

    @MockitoBean
    private ISimulationService<SewSimulationResultsDto> sewSimulationService;

    @Nested
    @DisplayName("Retrieve Latest CRF Simulation Results")
    class RetrieveLatestCrfResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results : Success")
        void givenValidRequest_whenRetrieveLatestCrfResults_thenReturnsSuccess() throws Exception {
            // Given
            CrfSimulationResultsDto mockResult = CrfSimulationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .message("Simulation completed successfully")
                    .simulationRun(true)
                    .solutionTime(5000L)
                    .totalTime(8000L)
                    .build();
            when(crfSimulationService.retrieveLatestSimulationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/crf/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Simulation results retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.timestamp").value("2024-01-15T10:30:00.000Z"))
                    .andExpect(jsonPath("$.data.simulation_run").value(true));

            verify(crfSimulationService).retrieveLatestSimulationResults();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest CRF results as Admin : Success")
        void givenValidRequestAsAdmin_whenRetrieveLatestCrfResults_thenReturnsSuccess() throws Exception {
            // Given
            CrfSimulationResultsDto mockResult = CrfSimulationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .message("Admin simulation access successful")
                    .simulationRun(true)
                    .build();
            when(crfSimulationService.retrieveLatestSimulationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/crf/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Simulation results retrieved successfully"));

            verify(crfSimulationService).retrieveLatestSimulationResults();
        }

        @Test
        @DisplayName("Retrieve latest CRF results : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestCrfResults_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/crf/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest CRF Simulation Results by Production Module")
    class RetrieveLatestCrfResultsByProductionModule {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest CRF results by module : Success")
        void givenValidModuleId_whenRetrieveLatestCrfResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "crf_module_1";
            CrfSimulationResultsDto mockResult = CrfSimulationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .message("Module simulation completed")
                    .simulationRun(true)
                    .build();
            when(crfSimulationService.retrieveLatestSimulationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/crf/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Simulation results for Module " + moduleId + " retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(crfSimulationService).retrieveLatestSimulationResultsByProductionModule(moduleId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest CRF results by module as Admin : Success")
        void givenValidModuleIdAsAdmin_whenRetrieveLatestCrfResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "admin_crf_module";
            CrfSimulationResultsDto mockResult = CrfSimulationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .message("Admin module simulation successful")
                    .simulationRun(true)
                    .build();
            when(crfSimulationService.retrieveLatestSimulationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/crf/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest CRF Simulation results for Module " + moduleId + " retrieved successfully"));

            verify(crfSimulationService).retrieveLatestSimulationResultsByProductionModule(moduleId);
        }

        @Test
        @DisplayName("Retrieve latest CRF results by module : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestCrfResultsByProductionModule_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/crf/modules/test_module/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest SEW Simulation Results")
    class RetrieveLatestSewResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest SEW results : Success")
        void givenValidRequest_whenRetrieveLatestSewResults_thenReturnsSuccess() throws Exception {
            // Given
            SewSimulationResultsDto mockResult = SewSimulationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .simulationData(createSampleSimulationData())
                    .build();
            when(sewSimulationService.retrieveLatestSimulationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/sew/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Simulation results retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.timestamp").value("2024-01-15T10:30:00.000Z"));

            verify(sewSimulationService).retrieveLatestSimulationResults();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest SEW results as Admin : Success")
        void givenValidRequestAsAdmin_whenRetrieveLatestSewResults_thenReturnsSuccess() throws Exception {
            // Given
            SewSimulationResultsDto mockResult = SewSimulationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .simulationData(createSampleSimulationData())
                    .build();
            when(sewSimulationService.retrieveLatestSimulationResults()).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/sew/latest")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Simulation results retrieved successfully"));

            verify(sewSimulationService).retrieveLatestSimulationResults();
        }

        @Test
        @DisplayName("Retrieve latest SEW results : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestSewResults_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/sew/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest SEW Simulation Results by Production Module")
    class RetrieveLatestSewResultsByProductionModule {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest SEW results by module : Success")
        void givenValidModuleId_whenRetrieveLatestSewResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "sew_module_1";
            SewSimulationResultsDto mockResult = SewSimulationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .simulationData(createSampleSimulationData())
                    .build();
            when(sewSimulationService.retrieveLatestSimulationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/sew/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Simulation results for Module " + moduleId + " retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value("1"));

            verify(sewSimulationService).retrieveLatestSimulationResultsByProductionModule(moduleId);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve latest SEW results by module as Admin : Success")
        void givenValidModuleIdAsAdmin_whenRetrieveLatestSewResultsByProductionModule_thenReturnsSuccess() throws Exception {
            // Given
            String moduleId = "admin_sew_module";
            SewSimulationResultsDto mockResult = SewSimulationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .simulationData(createSampleSimulationData())
                    .build();
            when(sewSimulationService.retrieveLatestSimulationResultsByProductionModule(moduleId)).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/sew/modules/{moduleId}/latest", moduleId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Latest SEW Simulation results for Module " + moduleId + " retrieved successfully"));

            verify(sewSimulationService).retrieveLatestSimulationResultsByProductionModule(moduleId);
        }

        @Test
        @DisplayName("Retrieve latest SEW results by module : Unauthorized")
        void givenUnauthorizedRequest_whenRetrieveLatestSewResultsByProductionModule_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/simulation/pilots/sew/modules/test_module/latest"))
                    .andExpect(status().isUnauthorized());
        }
    }

    /*
     * Helper Methods
     */
    private SewSimulationResultsDto.SimulationData createSampleSimulationData() {
        SewSimulationResultsDto.MetricComparison makespan = new SewSimulationResultsDto.MetricComparison(
                240.0, 220.0, -20.0, -8.33
        );
        SewSimulationResultsDto.MetricComparison machineUtilization = new SewSimulationResultsDto.MetricComparison(
                85.5, 92.3, 6.8, 7.95
        );
        SewSimulationResultsDto.MetricComparison throughputStdev = new SewSimulationResultsDto.MetricComparison(
                12.5, 8.7, -3.8, -30.4
        );

        return new SewSimulationResultsDto.SimulationData(makespan, machineUtilization, throughputStdev);
    }
}
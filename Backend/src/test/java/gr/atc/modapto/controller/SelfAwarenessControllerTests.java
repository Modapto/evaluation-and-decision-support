package gr.atc.modapto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.dto.crf.CrfSelfAwarenessParametersDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.service.interfaces.ICrfSelfAwarenessService;
import gr.atc.modapto.service.interfaces.ISewSelfAwarenessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private ISewSelfAwarenessService sewSelfAwarenessService;

    @MockitoBean
    private ICrfSelfAwarenessService crfSelfAwarenessService;

    @Nested
    @DisplayName("Invoke Self-Awareness Monitoring KPIs Process")
    class InvokeSelfAwarenessMonitoringKpisProcess {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke monitoring KPIs : Success")
        void givenValidInput_whenInvokeMonitoringKpis_thenReturnsSuccess() throws Exception {
            SewSelfAwarenessMonitoringKpisInputDto inputData = createValidInputData("TEST_MODULE");

            doNothing().when(sewSelfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any());

            mockMvc.perform(post("/api/eds/self-awareness/pilots/sew/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputData))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Self-Awareness Monitoring KPIs algorithm invoked successfully"));

            verify(sewSelfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any(SewSelfAwarenessMonitoringKpisInputDto.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Invoke monitoring KPIs as admin : Success")
        void givenValidInputAsAdmin_whenInvokeMonitoringKpis_thenReturnsSuccess() throws Exception {
            SewSelfAwarenessMonitoringKpisInputDto inputData = createValidInputData("ADMIN_MODULE");

            doNothing().when(sewSelfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any());

            mockMvc.perform(post("/api/eds/self-awareness/pilots/sew/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputData))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(sewSelfAwarenessService).invokeSelfAwarenessMonitoringKpisAlgorithm(any(SewSelfAwarenessMonitoringKpisInputDto.class));
        }

        @Test
        @DisplayName("Invoke monitoring KPIs : Unauthorized")
        void givenNoAuthentication_whenInvokeMonitoringKpis_thenReturnsUnauthorized() throws Exception {
            SewSelfAwarenessMonitoringKpisInputDto inputData = createValidInputData("TEST_MODULE");

            mockMvc.perform(post("/api/eds/self-awareness/pilots/sew/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputData))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(sewSelfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke monitoring KPIs : Validation error - Missing moduleId")
        void givenMissingModuleId_whenInvokeMonitoringKpis_thenReturnsValidationError() throws Exception {
            SewSelfAwarenessMonitoringKpisInputDto invalidInput = SewSelfAwarenessMonitoringKpisInputDto.builder()
                    .smartServiceId("SELF_AWARENESS_SERVICE")
                    .build();

            mockMvc.perform(post("/api/eds/self-awareness/pilots/sew/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidInput))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(sewSelfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke monitoring KPIs : Validation error - Empty moduleId")
        void givenEmptyModuleId_whenInvokeMonitoringKpis_thenReturnsValidationError() throws Exception {
            SewSelfAwarenessMonitoringKpisInputDto invalidInput = SewSelfAwarenessMonitoringKpisInputDto.builder()
                    .moduleId("")
                    .smartServiceId("SELF_AWARENESS_SERVICE")
                    .build();

            mockMvc.perform(post("/api/eds/self-awareness/pilots/sew/monitor-kpis/invoke")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidInput))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(sewSelfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Self-Awareness Monitoring KPIs Results")
    class RetrieveLatestSelfAwarenessMonitoringKpisResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest results : Success")
        void givenExistingResults_whenRetrieveLatest_thenReturnsLatestResult() throws Exception {
            SewSelfAwarenessMonitoringKpisResultsDto expectedResult = createSampleResultDto("test-id", "TEST_MODULE");

            when(sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults())
                    .thenReturn(expectedResult);

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("test-id"))
                    .andExpect(jsonPath("$.data.moduleId").value("TEST_MODULE"))
                    .andExpect(jsonPath("$.data.smartServiceId").value("Smart-Service-1"))
                    .andExpect(jsonPath("$.message").value("Latest Self-Awareness Monitoring KPIs results retrieved successfully"));

            verify(sewSelfAwarenessService).retrieveLatestSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @DisplayName("Retrieve latest results : Unauthorized")
        void givenNoAuthentication_whenRetrieveLatest_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/latest"))
                    .andExpect(status().isUnauthorized());

            verify(sewSelfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest results : JSON content type")
        void givenValidRequest_whenRetrieveLatest_thenReturnsJsonContentType() throws Exception {
            SewSelfAwarenessMonitoringKpisResultsDto expectedResult = createSampleResultDto("test-id", "TEST_MODULE");

            when(sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults())
                    .thenReturn(expectedResult);

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/latest"))
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
            String moduleId = "TEST_MODULE";
            SewSelfAwarenessMonitoringKpisResultsDto expectedResult = createSampleResultDto("test-id", moduleId);

            when(sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId))
                    .thenReturn(expectedResult);

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/{moduleId}/latest", moduleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("test-id"))
                    .andExpect(jsonPath("$.data.moduleId").value(moduleId))
                    .andExpect(jsonPath("$.message").value("Latest Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"));

            verify(sewSelfAwarenessService).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve latest results by module ID : Validation error - Empty moduleId")
        void givenEmptyModuleId_whenRetrieveLatestByModuleId_thenReturnsValidationError() throws Exception {
            mockMvc.perform(get("/api/eds/self-awareness/monitor-kpis/modules/{moduleId}/latest", ""))
                    .andExpect(status().isBadRequest()); // Empty path variable results in 404

            verify(sewSelfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }

        @Test
        @DisplayName("Retrieve latest results by module ID : Unauthorized")
        void givenNoAuthentication_whenRetrieveLatestByModuleId_thenReturnsUnauthorized() throws Exception {
            String moduleId = "TEST_MODULE";

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/{moduleId}/latest", moduleId))
                    .andExpect(status().isUnauthorized());

            verify(sewSelfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }
    }

    @Nested
    @DisplayName("Retrieve All Self-Awareness Monitoring KPIs Results")
    class RetrieveAllSelfAwarenessMonitoringKpisResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results : Success")
        void givenExistingResults_whenRetrieveAll_thenReturnsAllResults() throws Exception {
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Arrays.asList(
                    createSampleResultDto("test-id-1", "MODULE_1"),
                    createSampleResultDto("test-id-2", "MODULE_2")
            );

            when(sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults())
                    .thenReturn(expectedResults);

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].id").value("test-id-1"))
                    .andExpect(jsonPath("$.data[1].id").value("test-id-2"))
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results retrieved successfully"));

            verify(sewSelfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results : Empty list")
        void givenNoResults_whenRetrieveAll_thenReturnsEmptyList() throws Exception {
            when(sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults())
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results retrieved successfully"));

            verify(sewSelfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResults();
        }

        @Test
        @DisplayName("Retrieve all results : Unauthorized")
        void givenNoAuthentication_whenRetrieveAll_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results"))
                    .andExpect(status().isUnauthorized());

            verify(sewSelfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResults();
        }
    }

    @Nested
    @DisplayName("Retrieve All Self-Awareness Monitoring KPIs Results by Module ID")
    class RetrieveAllSelfAwarenessMonitoringKpisResultsByModuleId {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results by module ID : Success")
        void givenValidModuleId_whenRetrieveAllByModuleId_thenReturnsAllResultsForModule() throws Exception {
            String moduleId = "TEST_MODULE";
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Arrays.asList(
                    createSampleResultDto("test-id-1", moduleId),
                    createSampleResultDto("test-id-2", moduleId)
            );

            when(sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId))
                    .thenReturn(expectedResults);

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/{moduleId}", moduleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].moduleId").value(moduleId))
                    .andExpect(jsonPath("$.data[1].moduleId").value(moduleId))
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"));

            verify(sewSelfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve all results by module ID : Empty list")
        void givenNonExistentModuleId_whenRetrieveAllByModuleId_thenReturnsEmptyList() throws Exception {
            String moduleId = "NON_EXISTENT_MODULE";
            when(sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/{moduleId}", moduleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("All Self-Awareness Monitoring KPIs results for Module " + moduleId + " retrieved successfully"));

            verify(sewSelfAwarenessService).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);
        }

        @Test
        @DisplayName("Retrieve all results by module ID : Unauthorized")
        void givenNoAuthentication_whenRetrieveAllByModuleId_thenReturnsUnauthorized() throws Exception {
            String moduleId = "TEST_MODULE";

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/{moduleId}", moduleId))
                    .andExpect(status().isUnauthorized());

            verify(sewSelfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Access endpoints : Authentication required")
        void givenNoAuthentication_whenAccessEndpoints_thenReturnsUnauthorized() throws Exception {
            SewSelfAwarenessMonitoringKpisInputDto inputData = SewSelfAwarenessMonitoringKpisInputDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("SELF_AWARENESS_SERVICE")
                    .build();

            mockMvc.perform(post("/api/eds/self-awareness/pilots/sew/monitor-kpis/invoke")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputData))
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/latest"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/TEST/latest"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/self-awareness/pilots/sew/monitor-kpis/results/TEST"))
                    .andExpect(status().isUnauthorized());

            verify(sewSelfAwarenessService, never()).invokeSelfAwarenessMonitoringKpisAlgorithm(any());
            verify(sewSelfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResults();
            verify(sewSelfAwarenessService, never()).retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(any());
            verify(sewSelfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResults();
            verify(sewSelfAwarenessService, never()).retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(any());
        }
    }

    @Nested
    @DisplayName("CRF Self-Awareness Tests")
    class CrfSelfAwarenessTests {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke CRF KH Self-Awareness : Success")
        void givenValidFileAndParameters_whenInvokeCrfKhSelfAwareness_thenReturnsSuccess() throws Exception {
            String csvContent = """
                    eventType;rfidStation;timestamp;khType;khId
                    1;3;1672531200;2;12345
                    2;5;1672531260;1;67890
                    """;
            MockMultipartFile file = new MockMultipartFile(
                    "file", "events.csv", "text/csv", csvContent.getBytes()
            );

            CrfSelfAwarenessParametersDto parameters = CrfSelfAwarenessParametersDto.builder()
                    .moduleId("crf_module_1")
                    .smartServiceId("service_1")
                    .threshold(16.0)
                    .intervalMinutes(30)
                    .modelPath("quadratic_model.json")
                    .build();

            String parametersJson = objectMapper.writeValueAsString(parameters);
            MockMultipartFile parametersFile = new MockMultipartFile(
                    "parameters", "", "application/json", parametersJson.getBytes()
            );

            doNothing().when(crfSelfAwarenessService).invokeKhSelfAwareness(any(), any());

            mockMvc.perform(multipart("/api/eds/self-awareness/pilots/crf/invoke")
                            .file(file)
                            .file(parametersFile)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Self-Awareness algorithm for Kit-Holder has been successfully initialized"));

            verify(crfSelfAwarenessService).invokeKhSelfAwareness(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve paginated KH events : Success")
        void givenValidPaginationParams_whenRetrievePaginatedKhEvents_thenReturnsPagedResults() throws Exception {
            List<CrfKitHolderEventDto> events = Arrays.asList(
                    createSampleKhEventDto("1"),
                    createSampleKhEventDto("2")
            );
            Page<CrfKitHolderEventDto> page = new PageImpl<>(events);
            PaginatedResultsDto<CrfKitHolderEventDto> expectedResult = new PaginatedResultsDto<>(
                    events, 1, 2, true
            );

            when(crfSelfAwarenessService.retrievePaginatedKhEventResultsPaginated(any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/eds/self-awareness/pilots/crf/kh-events")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sortAttribute", "timeWindow")
                            .param("isAscending", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.results").isArray())
                    .andExpect(jsonPath("$.data.results").isNotEmpty());

            verify(crfSelfAwarenessService).retrievePaginatedKhEventResultsPaginated(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Register KH event : JSON deserialization error")
        void givenValidKhEvent_whenRegisterKitHolderEvent_thenReturnsJsonError() throws Exception {
            CrfKitHolderEventDto event = createSampleKhEventDto("2");

            doNothing().when(crfSelfAwarenessService).registerKitHolderEvent(any());

            mockMvc.perform(post("/api/eds/self-awareness/pilots/crf/register-event")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(event))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(crfSelfAwarenessService, never()).registerKitHolderEvent(any(CrfKitHolderEventDto.class));
        }

        @Test
        @DisplayName("CRF endpoints : Unauthorized")
        void givenNoAuthentication_whenAccessCrfEndpoints_thenReturnsUnauthorized() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "events.csv", "text/csv", "test".getBytes()
            );
            MockMultipartFile params = new MockMultipartFile(
                    "parameters", "", "application/json", "{}".getBytes()
            );

            mockMvc.perform(multipart("/api/eds/self-awareness/pilots/crf/invoke")
                            .file(file)
                            .file(params))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/eds/self-awareness/pilots/crf/kh-events"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/eds/self-awareness/pilots/crf/register-event")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());

            verify(crfSelfAwarenessService, never()).invokeKhSelfAwareness(any(), any());
            verify(crfSelfAwarenessService, never()).retrievePaginatedKhEventResultsPaginated(any());
            verify(crfSelfAwarenessService, never()).registerKitHolderEvent(any());
        }
    }

    private SewSelfAwarenessMonitoringKpisInputDto createValidInputData(String moduleId) {
        SewMonitorKpisComponentsDto componentsData = SewMonitorKpisComponentsDto.builder()
                .timestampCreated(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)))
                .components(List.of(SewMonitorKpisComponentsDto.SewMonitorKpisComponentsDataDto.builder()
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
                        .build()))
                .moduleId("TEST_MODULE")
                .build();

        return SewSelfAwarenessMonitoringKpisInputDto.builder()
                .moduleId(moduleId)
                .smartServiceId("SELF_AWARENESS_SERVICE")
                .startDate("Start_Date")
                .endDate("End_Date")
                .components(componentsData.getComponents())
                .build();
    }

    private SewSelfAwarenessMonitoringKpisResultsDto createSampleResultDto(String id, String moduleId) {
        return SewSelfAwarenessMonitoringKpisResultsDto.builder()
                .id(id)
                .moduleId(moduleId)
                .smartServiceId("Smart-Service-1")
                .timestamp(LocalDateTime.now())
                .ligne("Line1")
                .component("Component1")
                .variable("Temperature")
                .startingDate("2025-01-15 00:00:00")
                .endingDate("2025-01-15 23:59:59")
                .dataSource("Influx DB")
                .bucket("BUC")
                .data(Arrays.asList(25.5, 26.1, 27.3, 28.0))
                .build();
    }

    private CrfKitHolderEventDto createSampleKhEventDto(String id) {
        return CrfKitHolderEventDto.builder()
                .id(id)
                .moduleId("crf_module_1")
                .eventType(1)
                .rfidStation(3)
                .timestamp(LocalDateTime.now())
                .khType(2)
                .khId(12345)
                .build();
    }

}
package gr.atc.modapto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.ModaptoModuleDto;
import gr.atc.modapto.dto.sew.DeclarationOfWorkDto;
import gr.atc.modapto.service.interfaces.IModaptoModuleService;
import gr.atc.modapto.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModaptoModulesController.class)
@DisplayName("MODAPTO Modules Controller Tests")
class ModaptoModulesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IModaptoModuleService modaptoModuleService;

    private ModaptoModuleDto testModuleDto;
    private ModaptoModuleDto.SmartServiceDto testSmartServiceDto;
    private static final String TEST_MODULE_ID = "TEST_MODULE";
    private static final String TEST_WORKER = "worker1";

    @BeforeEach
    void setUp() {
        testSmartServiceDto = ModaptoModuleDto.SmartServiceDto.builder()
                .name("Test Smart Service")
                .catalogueId("CAT_001")
                .serviceId("SERVICE_001")
                .endpoint("https://test-service.com")
                .build();

        testModuleDto = ModaptoModuleDto.builder()
                .id("1")
                .moduleId(TEST_MODULE_ID)
                .name("Test Module")
                .endpoint("https://test-module.com")
                .smartServices(Collections.singletonList(testSmartServiceDto))
                .timestampDt(LocalDateTime.now())
                .workers(new ArrayList<>(Collections.singletonList(TEST_WORKER)))
                .build();
    }

    @Nested
    @DisplayName("Retrieve All Modules Paginated")
    class RetrieveAllModulesPaginated {

        @Test
        @WithMockUser
        @DisplayName("Retrieve paginated modules : Success")
        void givenValidPaginationParams_whenRetrievePaginatedModules_thenReturnsSuccess() throws Exception {
            // Given
            Page<ModaptoModuleDto> modulePage = new PageImpl<>(Collections.singletonList(testModuleDto));
            given(modaptoModuleService.retrieveAllModulesPaginated(any(Pageable.class)))
                    .willReturn(modulePage);

            // When & Then
            mockMvc.perform(get("/api/eds/modules")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.results").isArray())
                    .andExpect(jsonPath("$.data.results[0].moduleId").value(TEST_MODULE_ID));
        }

        @Test
        @DisplayName("Retrieve paginated modules : Unauthorized")
        void givenNoAuthentication_whenRetrievePaginatedModules_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/modules")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve All Modules")
    class RetrieveAllModules {

        @Test
        @WithMockUser
        @DisplayName("Retrieve all modules : Success")
        void givenValidRequest_whenRetrieveAllModules_thenReturnsSuccess() throws Exception {
            // Given
            List<ModaptoModuleDto> modules = Collections.singletonList(testModuleDto);
            given(modaptoModuleService.retrieveAllModules()).willReturn(modules);

            // When & Then
            mockMvc.perform(get("/api/eds/modules/all")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].moduleId").value(TEST_MODULE_ID));
        }

        @Test
        @DisplayName("Retrieve all modules : Unauthorized")
        void givenNoAuthentication_whenRetrieveAllModules_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/modules/all")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Module By ID")
    class RetrieveModuleById {

        @Test
        @WithMockUser
        @DisplayName("Retrieve module by ID : Success")
        void givenValidModuleId_whenRetrieveModuleById_thenReturnsSuccess() throws Exception {
            // Given
            given(modaptoModuleService.retrieveModuleByModuleId(TEST_MODULE_ID))
                    .willReturn(testModuleDto);

            // When & Then
            mockMvc.perform(get("/api/eds/modules/{moduleId}", TEST_MODULE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.moduleId").value(TEST_MODULE_ID))
                    .andExpect(jsonPath("$.data.name").value("Test Module"));
        }

        @Test
        @DisplayName("Retrieve module by ID : Unauthorized")
        void givenNoAuthentication_whenRetrieveModuleById_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/modules/{moduleId}", TEST_MODULE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Retrieve Smart Services By Module ID")
    class RetrieveSmartServicesByModuleId {

        @Test
        @WithMockUser
        @DisplayName("Retrieve smart services : Success")
        void givenValidModuleId_whenRetrieveSmartServices_thenReturnsSuccess() throws Exception {
            // Given
            List<ModaptoModuleDto.SmartServiceDto> smartServices = Collections.singletonList(testSmartServiceDto);
            given(modaptoModuleService.retrieveSmartServicesByModuleId(TEST_MODULE_ID))
                    .willReturn(smartServices);

            // When & Then
            mockMvc.perform(get("/api/eds/modules/{moduleId}/smart-services", TEST_MODULE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].serviceId").value("SERVICE_001"));
        }

        @Test
        @DisplayName("Retrieve smart services : Unauthorized")
        void givenNoAuthentication_whenRetrieveSmartServices_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/modules/{moduleId}/smart-services", TEST_MODULE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Worker-Module Association")
    class WorkerModuleAssociationTests {

        @Test
//        @WithMockUser
        @DisplayName("Retrieve modules by worker : Success")
        void givenValidRequest_whenRetrieveModulesByWorker_thenReturnsSuccess() throws Exception {
            // Given
            Page<ModaptoModuleDto> modulePage = new PageImpl<>(Collections.singletonList(testModuleDto));
                given(modaptoModuleService.retrieveModulesByWorkerPaginated(eq(TEST_WORKER), any(Pageable.class)))
                        .willReturn(modulePage);

                // When & Then
                mockMvc.perform(get("/api/eds/modules/working-modules/users/{workerName}", TEST_WORKER)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "timestamp_dt")
                                .param("sortDirection", "desc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.results").isArray())
                        .andExpect(jsonPath("$.data.results[0].moduleId").value(TEST_MODULE_ID));
        }

        @Test
        @DisplayName("Retrieve modules by worker : Unauthorized")
        void givenNoAuthentication_whenRetrieveModulesByWorker_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/modules/working-modules/users/{workerName}", TEST_WORKER)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Declare work on module : Success")
        void givenValidModuleId_whenDeclareWorkOnModule_thenReturnsSuccess() throws Exception {
            try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
                // Mock static method
                mockedJwtUtils.when(() -> JwtUtils.extractUserId(any(Jwt.class))).thenReturn(TEST_WORKER);

                // Given
                given(modaptoModuleService.declareWorkOnModule(any(DeclarationOfWorkDto.class)))
                        .willReturn(testModuleDto);

                // When & Then
                DeclarationOfWorkDto workData = DeclarationOfWorkDto.builder()
                        .moduleId(TEST_MODULE_ID)
                        .workers(Arrays.asList(TEST_WORKER))
                        .build();

                mockMvc.perform(post("/api/eds/modules/declare-work")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(workData))
                                .with(jwt()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.moduleId").value(TEST_MODULE_ID));
            }
        }

        @Test
        @DisplayName("Declare work on module : Forbidden")
        void givenNoAuthentication_whenDeclareWorkOnModule_thenReturnsForbidden() throws Exception {
            // Given
            DeclarationOfWorkDto workData = DeclarationOfWorkDto.builder()
                    .moduleId(TEST_MODULE_ID)
                    .workers(Arrays.asList(TEST_WORKER))
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/modules/declare-work")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(workData)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Undeclare work on module : Success")
        void givenValidModuleId_whenUndeclareWorkOnModule_thenReturnsSuccess() throws Exception {
            try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
                // Mock static method
                mockedJwtUtils.when(() -> JwtUtils.extractUserId(any(Jwt.class))).thenReturn(TEST_WORKER);
                // Given
                given(modaptoModuleService.undeclareWorkOnModule(TEST_MODULE_ID, TEST_WORKER))
                        .willReturn(testModuleDto);

                // When & Then
                mockMvc.perform(post("/api/eds/modules/{moduleId}/undeclare-work/users/{workerName}", TEST_MODULE_ID, TEST_WORKER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(jwt()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.moduleId").value(TEST_MODULE_ID));
            }
        }

        @Test
        @DisplayName("Undeclare work on module : Forbidden")
        void givenNoAuthentication_whenUndeclareWorkOnModule_thenReturnsForbidden() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/eds/modules/{moduleId}/undeclare-work/users/{workerName}", TEST_MODULE_ID, TEST_WORKER)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
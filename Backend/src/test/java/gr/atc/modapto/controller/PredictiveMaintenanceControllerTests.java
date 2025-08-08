package gr.atc.modapto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.serviceInvocations.SewGroupingPredictiveMaintenanceInputDataDto;
import gr.atc.modapto.dto.serviceInvocations.SewPredictiveMaintenanceEventParameters;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.dto.serviceResults.sew.SewGroupingPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewThresholdBasedPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import gr.atc.modapto.dto.sew.SewComponentInfoDto;
import gr.atc.modapto.service.interfaces.IPredictiveMaintenanceService;
import gr.atc.modapto.exception.CustomExceptions.FileHandlingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PredictiveMaintenanceController.class)
@ActiveProfiles("test")
@DisplayName("PredictiveMaintenanceController Tests")
class PredictiveMaintenanceControllerTests {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IPredictiveMaintenanceService predictiveMaintenanceService;

    @Nested
    @DisplayName("Upload CORIM File")
    class UploadCorimFile {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload CORIM file : Success")
        void givenValidExcelFile_whenUploadCorimFile_thenReturnsSuccess() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );
            doNothing().when(predictiveMaintenanceService).storeCorimData(any());

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Corim file uploaded successfully"));

            verify(predictiveMaintenanceService).storeCorimData(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Upload CORIM file as Admin : Success")
        void givenValidExcelFileAsAdmin_whenUploadCorimFile_thenReturnsSuccess() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "admin_test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );
            doNothing().when(predictiveMaintenanceService).storeCorimData(any());

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Corim file uploaded successfully"));

            verify(predictiveMaintenanceService).storeCorimData(any());
        }

        @Test
        @DisplayName("Upload CORIM file : Unauthorized")
        void givenNoAuthentication_whenUploadCorimFile_thenReturnsUnauthorized() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).storeCorimData(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload CORIM file : Invalid file type")
        void givenInvalidFileType_whenUploadCorimFile_thenReturnsBadRequest() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain",
                    "not an excel file".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).storeCorimData(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload CORIM file : Service error")
        void givenServiceException_whenUploadCorimFile_thenReturnsInternalServerError() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );
            doThrow(new FileHandlingException("Processing error"))
                    .when(predictiveMaintenanceService).storeCorimData(any());

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Input file handling error"));

            verify(predictiveMaintenanceService).storeCorimData(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload CORIM file : Wrong parameter name")
        void givenWrongParameterName_whenUploadCorimFile_thenReturnsBadRequest() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "wrongParamName", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).storeCorimData(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload CORIM file : Empty file")
        void givenEmptyFile_whenUploadCorimFile_thenReturnsBadRequest() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new byte[0]
            );

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).storeCorimData(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Maintenance Data")
    class RetrieveMaintenanceData {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Success with date range")
        void givenStartAndEndDate_whenRetrieveMaintenanceData_thenReturnsDataWithinRange() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    anyString(), anyString())).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-12-31T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].Stage").value("Stage1"))
                    .andExpect(jsonPath("$.data[1].Stage").value("Stage2"))
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange("2024-01-01T00:00:00", "2024-12-31T00:00:00");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Success with start date only")
        void givenStartDateOnly_whenRetrieveMaintenanceData_thenReturnsDataFromStartDate() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq("2024-01-01T00:00:00"), eq(null))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("startDate", "2024-01-01T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange("2024-01-01T00:00:00", null);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Success with end date only")
        void givenEndDateOnly_whenRetrieveMaintenanceData_thenReturnsDataUntilEndDate() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq(null), eq("2024-12-31T00:00:00"))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("endDate", "2024-12-31T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange(null, "2024-12-31T00:00:00");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Success with no dates")
        void givenNoDates_whenRetrieveMaintenanceData_thenReturnsAllData() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq(null), eq(null))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange(null, null);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Empty result")
        void givenDateRangeWithNoData_whenRetrieveMaintenanceData_thenReturnsEmptyList() throws Exception {
            // Given
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq("2024-01-01T00:00:00"), eq("2024-01-02T00:00:00"))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-02T00:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange("2024-01-01T00:00:00", "2024-01-02T00:00:00");
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Retrieve maintenance data as admin : Success")
        void givenAdminRole_whenRetrieveMaintenanceData_thenReturnsAllData() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq(null), eq(null))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty());

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange(null, null);
        }

        @Test
        @DisplayName("Retrieve maintenance data : Unauthorized")
        void givenNoAuthentication_whenRetrieveMaintenanceData_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data"))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).retrieveMaintenanceDataByDateRange(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Special characters in dates")
        void givenSpecialCharactersInDates_whenRetrieveMaintenanceData_thenReturnsSuccess() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq("2024-01-01T00:00:00"), eq("2024-12-31T23:59:59"))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-12-31T23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange("2024-01-01T00:00:00", "2024-12-31T23:59:59");
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Access endpoints : Authentication required")
        void givenNoAuthentication_whenAccessEndpoints_thenReturnsUnauthorized() throws Exception {
            // Test upload endpoint
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );
            
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Test retrieve endpoint
            mockMvc.perform(get("/api/eds/maintenance/data"))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).storeCorimData(any());
            verify(predictiveMaintenanceService, never()).retrieveMaintenanceDataByDateRange(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload CORIM file : CSRF token required")
        void givenNoCsrfToken_whenUploadCorimFile_thenReturnsForbidden() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file))
                    .andExpect(status().isForbidden());

            verify(predictiveMaintenanceService, never()).storeCorimData(any());
        }
    }

    @Nested
    @DisplayName("Content Type and Headers")
    class ContentTypeAndHeaders {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : JSON content type")
        void givenValidRequest_whenRetrieveMaintenanceData_thenReturnsJsonContentType() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(any(), any()))
                    .thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", "application/json"));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload CORIM file : Multipart form data")
        void givenMultipartFormData_whenUploadCorimFile_thenReturnsSuccess() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "excel content".getBytes()
            );
            doNothing().when(predictiveMaintenanceService).storeCorimData(any());

            // When & Then
            mockMvc.perform(multipart("/api/eds/maintenance/uploadCorimFile")
                    .file(file)
                    .with(csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Upload Components List")
    class UploadComponentsList {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload components list : Success")
        void givenValidComponentsList_whenUploadComponentsList_thenReturnsSuccess() throws Exception {
            // Given
            List<SewComponentInfoDto> componentsList = Arrays.asList(
                    SewComponentInfoDto.builder()
                            .stage("Stage1")
                            .cell("Cell1")
                            .module("Module1")
                            .moduleId("MOD1")
                            .alpha(5.0)
                            .beta(5.0)
                            .mtbf(5.0)
                            .averageMaintenanceDuration(5.0)
                            .build(),
                    SewComponentInfoDto.builder()
                            .stage("Stage2")
                            .cell("Cell2")
                            .module("Module2")
                            .moduleId("MOD2")
                            .alpha(5.0)
                            .beta(5.0)
                            .mtbf(5.0)
                            .averageMaintenanceDuration(5.0)
                            .build()
            );

            doNothing().when(predictiveMaintenanceService).storeComponentsListData(any());
            doNothing().when(predictiveMaintenanceService).locateLastMaintenanceActionForStoredComponents();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/uploadComponentsList")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(componentsList))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Components file uploaded successfully"));

            verify(predictiveMaintenanceService).storeComponentsListData(componentsList);
            verify(predictiveMaintenanceService).locateLastMaintenanceActionForStoredComponents();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload components list : Empty list")
        void givenEmptyComponentsList_whenUploadComponentsList_thenReturnsSuccess() throws Exception {
            // Given
            List<SewComponentInfoDto> emptyList = Collections.emptyList();
            doNothing().when(predictiveMaintenanceService).storeComponentsListData(any());
            doNothing().when(predictiveMaintenanceService).locateLastMaintenanceActionForStoredComponents();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/uploadComponentsList")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyList))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(predictiveMaintenanceService).storeComponentsListData(emptyList);
            verify(predictiveMaintenanceService).locateLastMaintenanceActionForStoredComponents();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload components list : Validation error - Missing required fields")
        void givenInvalidComponentsList_whenUploadComponentsList_thenReturnsValidationError() throws Exception {
            // Given - Component with missing required fields
            List<SewComponentInfoDto> invalidComponentsList = Arrays.asList(
                    SewComponentInfoDto.builder()
                            .stage("Stage1")
                            .cell("") // Blank field
                            .module("Module1")
                            .moduleId("MOD1")
                            .alpha(5.0)
                            .beta(5.0)
                            .mtbf(5.0)
                            .averageMaintenanceDuration(5.0)
                            .build(),
                    SewComponentInfoDto.builder()
                            // Missing stage
                            .cell("Cell2")
                            .module("Module2")
                            .moduleId("MOD2")
                            .alpha(5.0)
                            .beta(5.0)
                            .mtbf(5.0)
                            .averageMaintenanceDuration(5.0)
                            .build()
            );

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/uploadComponentsList")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidComponentsList))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).storeComponentsListData(any());
            verify(predictiveMaintenanceService, never()).locateLastMaintenanceActionForStoredComponents();
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload components list : Validation error - Null list")
        void givenNullComponentsList_whenUploadComponentsList_thenReturnsValidationError() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/uploadComponentsList")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).storeComponentsListData(any());
            verify(predictiveMaintenanceService, never()).locateLastMaintenanceActionForStoredComponents();
        }

        @Test
        @DisplayName("Upload components list : Unauthorized")
        void givenNoAuthentication_whenUploadComponentsList_thenReturnsUnauthorized() throws Exception {
            // Given
            List<SewComponentInfoDto> componentsList = Collections.singletonList(
                    SewComponentInfoDto.builder().stage("Stage1").build()
            );

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/uploadComponentsList")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(componentsList))
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).storeComponentsListData(any());
        }
    }

    @Nested
    @DisplayName("Invoke Grouping Predictive Maintenance")
    class InvokeGroupingPredictiveMaintenance {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke grouping maintenance : Success")
        void givenValidInput_whenInvokeGroupingMaintenance_thenReturnsSuccess() throws Exception {
            // Given
            SewGroupingPredictiveMaintenanceInputDataDto inputData = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("GROUPING_SERVICE")
                    .setupCost(500.0)
                    .downtimeCostRate(150.0)
                    .noRepairmen(3)
                    .build();

            doNothing().when(predictiveMaintenanceService).invokeGroupingPredictiveMaintenance(any());

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/grouping-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputData))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Predictive Maintenance service for Grouping Maintenance invoked successfully"));

            verify(predictiveMaintenanceService).invokeGroupingPredictiveMaintenance(any(SewGroupingPredictiveMaintenanceInputDataDto.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke grouping maintenance : Validation error - Missing moduleId")
        void givenMissingModuleId_whenInvokeGroupingMaintenance_thenReturnsValidationError() throws Exception {
            // Given - Missing required moduleId
            SewGroupingPredictiveMaintenanceInputDataDto invalidInput = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .smartServiceId("GROUPING_SERVICE")
                    .setupCost(500.0)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/grouping-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidInput))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).invokeGroupingPredictiveMaintenance(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke grouping maintenance : Validation error - Empty moduleId")
        void givenEmptyModuleId_whenInvokeGroupingMaintenance_thenReturnsValidationError() throws Exception {
            // Given - Empty moduleId (violates @NotEmpty)
            SewGroupingPredictiveMaintenanceInputDataDto invalidInput = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("") // Empty string violates @NotEmpty
                    .smartServiceId("GROUPING_SERVICE")
                    .setupCost(500.0)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/grouping-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidInput))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).invokeGroupingPredictiveMaintenance(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke grouping maintenance : Validation error - Missing smartServiceId")
        void givenMissingSmartServiceId_whenInvokeGroupingMaintenance_thenReturnsValidationError() throws Exception {
            // Given - Missing required smartServiceId
            SewGroupingPredictiveMaintenanceInputDataDto invalidInput = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .setupCost(500.0)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/grouping-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidInput))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).invokeGroupingPredictiveMaintenance(any());
        }

        @Test
        @DisplayName("Invoke grouping maintenance : Unauthorized")
        void givenNoAuthentication_whenInvokeGroupingMaintenance_thenReturnsUnauthorized() throws Exception {
            // Given
            SewGroupingPredictiveMaintenanceInputDataDto inputData = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("GROUPING_SERVICE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/grouping-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputData))
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).invokeGroupingPredictiveMaintenance(any());
        }
    }

    @Nested
    @DisplayName("Invoke Threshold-Based Predictive Maintenance")
    class InvokeThresholdBasedPredictiveMaintenance {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke threshold maintenance : Success")
        void givenValidInput_whenInvokeThresholdMaintenance_thenReturnsResult() throws Exception {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .parameters(SewPredictiveMaintenanceEventParameters.builder()
                            .moduleID("TEST_MODULE")
                            .componentsID(Arrays.asList("COMP1", "COMP2"))
                            .windowSize(30)
                            .inspectionThreshold(5)
                            .replacementThreshold(10)
                            .build())
                    .build();

            SewThresholdBasedPredictiveMaintenanceOutputDto expectedOutput = SewThresholdBasedPredictiveMaintenanceOutputDto.builder()
                    .id("test-id")
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .recommendation("Replace bearing in motor unit within 72 hours")
                    .details("Vibration levels exceeded threshold")
                    .build();

            when(predictiveMaintenanceService.invokeThresholdBasedPredictiveMaintenance(any()))
                    .thenReturn(expectedOutput);

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/threshold-based-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputData))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("test-id"))
                    .andExpect(jsonPath("$.data.moduleId").value("TEST_MODULE"))
                    .andExpect(jsonPath("$.data.smartServiceId").value("THRESHOLD_SERVICE"))
                    .andExpect(jsonPath("$.data.recommendation").value("Replace bearing in motor unit within 72 hours"))
                    .andExpect(jsonPath("$.message").value("Predictive Maintenance service for Threshold-Based Maintenance completed successfully"));

            verify(predictiveMaintenanceService).invokeThresholdBasedPredictiveMaintenance(any(SewThresholdBasedMaintenanceInputDataDto.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke threshold maintenance : Validation error - Missing moduleId")
        void givenMissingModuleId_whenInvokeThresholdMaintenance_thenReturnsValidationError() throws Exception {
            // Given - Missing required moduleId
            SewThresholdBasedMaintenanceInputDataDto invalidInput = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/threshold-based-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidInput))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).invokeThresholdBasedPredictiveMaintenance(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke threshold maintenance : Validation error - Empty moduleId")
        void givenEmptyModuleId_whenInvokeThresholdMaintenance_thenReturnsValidationError() throws Exception {
            // Given - Empty moduleId
            SewThresholdBasedMaintenanceInputDataDto invalidInput = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("") // Empty string
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/threshold-based-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidInput))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).invokeThresholdBasedPredictiveMaintenance(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke threshold maintenance : Validation error - Missing smartServiceId")
        void givenMissingSmartServiceId_whenInvokeThresholdMaintenance_thenReturnsValidationError() throws Exception {
            // Given - Missing required smartServiceId
            SewThresholdBasedMaintenanceInputDataDto invalidInput = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/threshold-based-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidInput))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).invokeThresholdBasedPredictiveMaintenance(any());
        }

        @Test
        @DisplayName("Invoke threshold maintenance : Unauthorized")
        void givenNoAuthentication_whenInvokeThresholdMaintenance_thenReturnsUnauthorized() throws Exception {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/threshold-based-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputData))
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).invokeThresholdBasedPredictiveMaintenance(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Maintenance Results")
    class RetrieveMaintenanceResults {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve grouping results : Success")
        void givenValidModuleId_whenRetrieveGroupingResults_thenReturnsResults() throws Exception {
            // Given
            SewGroupingPredictiveMaintenanceOutputDto expectedOutput = SewGroupingPredictiveMaintenanceOutputDto.builder()
                    .id("result-id")
                    .moduleId("TEST_MODULE")
                    .smartServiceId("GROUPING_SERVICE")
                    .build();

            when(predictiveMaintenanceService.retrieveLatestGroupingMaintenanceResults("TEST_MODULE"))
                    .thenReturn(expectedOutput);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/predict/grouping-maintenance/results")
                    .param("moduleId", "TEST_MODULE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("result-id"))
                    .andExpect(jsonPath("$.data.moduleId").value("TEST_MODULE"))
                    .andExpect(jsonPath("$.message").value("Grouping Predictive Maintenance latest results retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveLatestGroupingMaintenanceResults("TEST_MODULE");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve threshold results : Success")
        void givenValidModuleId_whenRetrieveThresholdResults_thenReturnsResults() throws Exception {
            // Given
            SewThresholdBasedPredictiveMaintenanceOutputDto expectedOutput = SewThresholdBasedPredictiveMaintenanceOutputDto.builder()
                    .id("result-id")
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .recommendation("Replace bearing immediately")
                    .build();

            when(predictiveMaintenanceService.retrieveLatestThresholdBasedMaintenanceResults("TEST_MODULE"))
                    .thenReturn(expectedOutput);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/predict/threshold-based-maintenance/results")
                    .param("moduleId", "TEST_MODULE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("result-id"))
                    .andExpect(jsonPath("$.data.moduleId").value("TEST_MODULE"))
                    .andExpect(jsonPath("$.data.recommendation").value("Replace bearing immediately"))
                    .andExpect(jsonPath("$.message").value("Threshold-Based Predictive Maintenance latest results retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveLatestThresholdBasedMaintenanceResults("TEST_MODULE");
        }

        @Test
        @DisplayName("Retrieve results : Unauthorized")
        void givenNoAuthentication_whenRetrieveResults_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/predict/grouping-maintenance/results")
                    .param("moduleId", "TEST_MODULE"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/eds/maintenance/predict/threshold-based-maintenance/results")
                    .param("moduleId", "TEST_MODULE"))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).retrieveLatestGroupingMaintenanceResults(any());
            verify(predictiveMaintenanceService, never()).retrieveLatestThresholdBasedMaintenanceResults(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve results : Missing module ID")
        void givenMissingModuleId_whenRetrieveResults_thenReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/predict/grouping-maintenance/results"))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/eds/maintenance/predict/threshold-based-maintenance/results"))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).retrieveLatestGroupingMaintenanceResults(any());
            verify(predictiveMaintenanceService, never()).retrieveLatestThresholdBasedMaintenanceResults(any());
        }
    }

    @Nested
    @DisplayName("Content Type and Request Validation")
    class ContentTypeAndRequestValidation {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke grouping maintenance : Invalid Content-Type")
        void givenInvalidContentType_whenInvokeGroupingMaintenance_thenReturnsUnsupportedMediaType() throws Exception {
            // Given
            SewGroupingPredictiveMaintenanceInputDataDto inputData = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("GROUPING_SERVICE")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/grouping-maintenance")
                    .contentType(MediaType.APPLICATION_XML) // Wrong content type
                    .content(objectMapper.writeValueAsString(inputData))
                    .with(csrf()))
                    .andExpect(status().isUnsupportedMediaType());

            verify(predictiveMaintenanceService, never()).invokeGroupingPredictiveMaintenance(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Invoke threshold maintenance : Malformed JSON")
        void givenMalformedJson_whenInvokeThresholdMaintenance_thenReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/predict/threshold-based-maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"moduleId\": \"TEST_MODULE\", \"smartServiceId\": }") // Malformed JSON
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).invokeThresholdBasedPredictiveMaintenance(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Upload components : Missing Content-Type")
        void givenMissingContentType_whenUploadComponents_thenReturnsUnsupportedMediaType() throws Exception {
            // Given
            List<SewComponentInfoDto> componentsList = Collections.singletonList(
                    SewComponentInfoDto.builder()
                            .stage("Stage1")
                            .cell("Cell1")
                            .module("Module1")
                            .moduleId("MOD1")
                            .build()
            );

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/uploadComponentsList")
                    // Missing content type
                    .content(objectMapper.writeValueAsString(componentsList))
                    .with(csrf()))
                    .andExpect(status().isUnsupportedMediaType());

            verify(predictiveMaintenanceService, never()).storeComponentsListData(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve results : Invalid module ID parameter")
        void givenEmptyModuleIdParameter_whenRetrieveResults_thenHandlesGracefully() throws Exception {
            // Given
            SewGroupingPredictiveMaintenanceOutputDto expectedOutput = SewGroupingPredictiveMaintenanceOutputDto.builder()
                    .id("result-id")
                    .moduleId("")
                    .smartServiceId("GROUPING_SERVICE")
                    .build();

            when(predictiveMaintenanceService.retrieveLatestGroupingMaintenanceResults(""))
                    .thenReturn(expectedOutput);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/predict/grouping-maintenance/results")
                    .param("moduleId", "")) // Empty parameter value
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("result-id"));

            verify(predictiveMaintenanceService).retrieveLatestGroupingMaintenanceResults("");
        }
    }

    @Nested
    @DisplayName("Process Drift Management")
    class ProcessDriftManagement {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Declare process drift : Success")
        void givenValidProcessDriftData_whenDeclareProcessDrift_thenReturnsCreated() throws Exception {
            // Given
            MaintenanceDataDto processDriftData = MaintenanceDataDto.builder()
                    .stage("TestStage")
                    .cell("TestCell")
                    .module("TestModule")
                    .moduleId("MOD001")
                    .component("TestComponent")
                    .componentId("COMP001")
                    .failureType("ProcessDrift")
                    .build();

            when(predictiveMaintenanceService.declareProcessDrift(any(MaintenanceDataDto.class)))
                    .thenReturn("generated-drift-id");

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/process-drifts/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(processDriftData))
                    .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value("generated-drift-id"))
                    .andExpect(jsonPath("$.message").value("Process Drift has been successfully registered in the System"));

            verify(predictiveMaintenanceService).declareProcessDrift(any(MaintenanceDataDto.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Declare process drift : Validation error - Missing required fields")
        void givenInvalidProcessDriftData_whenDeclareProcessDrift_thenReturnsValidationError() throws Exception {
            // Given - Process drift with missing required fields
            MaintenanceDataDto invalidData = MaintenanceDataDto.builder()
                    .stage("") // Empty stage
                    .cell("TestCell")
                    .component("TestComponent")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/process-drifts/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidData))
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verify(predictiveMaintenanceService, never()).declareProcessDrift(any());
        }

        @Test
        @DisplayName("Declare process drift : Unauthorized")
        void givenNoAuthentication_whenDeclareProcessDrift_thenReturnsUnauthorized() throws Exception {
            // Given
            MaintenanceDataDto processDriftData = MaintenanceDataDto.builder()
                    .stage("TestStage")
                    .cell("TestCell")
                    .component("TestComponent")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/eds/maintenance/process-drifts/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(processDriftData))
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).declareProcessDrift(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve process drift by ID : Success")
        void givenValidProcessDriftId_whenRetrieveProcessDriftById_thenReturnsProcessDrift() throws Exception {
            // Given
            String driftId = "test-drift-id";
            MaintenanceDataDto expectedDto = MaintenanceDataDto.builder()
                    .stage("TestStage")
                    .cell("TestCell")
                    .component("TestComponent")
                    .failureType("ProcessDrift")
                    .build();

            when(predictiveMaintenanceService.retrieveProcessDriftById(driftId))
                    .thenReturn(expectedDto);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/process-drifts/{processDriftId}", driftId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.Stage").value("TestStage"))
                    .andExpect(jsonPath("$.data.Cell").value("TestCell"))
                    .andExpect(jsonPath("$.data.Component").value("TestComponent"))
                    .andExpect(jsonPath("$.message").value("Process Drift retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveProcessDriftById(driftId);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve process drift by ID : Validation error - Empty ID")
        void givenEmptyProcessDriftId_whenRetrieveProcessDriftById_thenReturnsValidationError() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/process-drifts/{processDriftId}", ""))
                    .andExpect(status().isNotFound()); // Path variable empty results in 404

            verify(predictiveMaintenanceService, never()).retrieveProcessDriftById(any());
        }

        @Test
        @DisplayName("Retrieve process drift by ID : Unauthorized")
        void givenNoAuthentication_whenRetrieveProcessDriftById_thenReturnsUnauthorized() throws Exception {
            // Given
            String driftId = "test-drift-id";

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/process-drifts/{processDriftId}", driftId))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).retrieveProcessDriftById(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Complete process drift : Success")
        void givenValidIdAndEndTime_whenCompleteProcessDrift_thenReturnsSuccess() throws Exception {
            // Given
            String driftId = "test-drift-id";
            String endDatetime = "2025-08-03T14:30:00";
            doNothing().when(predictiveMaintenanceService)
                    .completeProcessDrift(eq(driftId), any(LocalDateTime.class));

            // When & Then
            mockMvc.perform(put("/api/eds/maintenance/process-drifts/{processDriftId}/complete", driftId)
                    .param("endDatetime", endDatetime)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Process Drift completed successfully"));

            verify(predictiveMaintenanceService).completeProcessDrift(eq(driftId), any(LocalDateTime.class));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Complete process drift : Validation error - Missing end datetime")
        void givenMissingEndDatetime_whenCompleteProcessDrift_thenReturnsValidationError() throws Exception {
            // Given
            String driftId = "test-drift-id";

            // When & Then
            mockMvc.perform(put("/api/eds/maintenance/process-drifts/{processDriftId}/complete", driftId)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).completeProcessDrift(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Complete process drift : Validation error - Invalid datetime format")
        void givenInvalidDatetimeFormat_whenCompleteProcessDrift_thenReturnsValidationError() throws Exception {
            // Given
            String driftId = "test-drift-id";
            String invalidEndDatetime = "invalid-datetime-format";

            // When & Then
            mockMvc.perform(put("/api/eds/maintenance/process-drifts/{processDriftId}/complete", driftId)
                    .param("endDatetime", invalidEndDatetime)
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(predictiveMaintenanceService, never()).completeProcessDrift(any(), any());
        }

        @Test
        @DisplayName("Complete process drift : Unauthorized")
        void givenNoAuthentication_whenCompleteProcessDrift_thenReturnsUnauthorized() throws Exception {
            // Given
            String driftId = "test-drift-id";
            String endDatetime = "2025-08-03T14:30:00";

            // When & Then
            mockMvc.perform(put("/api/eds/maintenance/process-drifts/{processDriftId}/complete", driftId)
                    .param("endDatetime", endDatetime)
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).completeProcessDrift(any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve paginated uncompleted process drifts : Success")
        void givenValidPaginationParams_whenRetrievePaginatedUncompletedProcessDrifts_thenReturnsPagedResults() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = Arrays.asList(
                    MaintenanceDataDto.builder()
                            .stage("Stage1")
                            .cell("Cell1")
                            .component("Component1")
                            .failureType("ProcessDrift")
                            .tsRequestCreation(LocalDateTime.parse("2024-01-15T10:30:00"))
                            .build(),
                    MaintenanceDataDto.builder()
                            .stage("Stage2")
                            .cell("Cell2")
                            .component("Component2")
                            .failureType("ProcessDrift")
                            .tsRequestCreation(LocalDateTime.parse("2024-01-16T11:30:00"))
                            .build()
            );

            Page<MaintenanceDataDto> mockPage = new PageImpl<>(mockData);
            when(predictiveMaintenanceService.retrievePaginatedUncompletedProcessDrifts(any()))
                    .thenReturn(mockPage);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/process-drifts")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sortAttribute", "tsInterventionStarted")
                    .param("isAscending", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.results").isArray())
                    .andExpect(jsonPath("$.data.results").isNotEmpty())
                    .andExpect(jsonPath("$.data.results[0].Stage").value("Stage1"))
                    .andExpect(jsonPath("$.data.results[1].Stage").value("Stage2"))
                    .andExpect(jsonPath("$.message").value("Uncompleted paginated process drifts retrieved successfully"));

            verify(predictiveMaintenanceService).retrievePaginatedUncompletedProcessDrifts(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve paginated uncompleted process drifts : Default parameters")
        void givenNoParams_whenRetrievePaginatedUncompletedProcessDrifts_thenUsesDefaults() throws Exception {
            // Given
            Page<MaintenanceDataDto> emptyPage = new PageImpl<>(Collections.emptyList());
            when(predictiveMaintenanceService.retrievePaginatedUncompletedProcessDrifts(any()))
                    .thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/process-drifts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.results").isArray())
                    .andExpect(jsonPath("$.data.results").isEmpty())
                    .andExpect(jsonPath("$.message").value("Uncompleted paginated process drifts retrieved successfully"));

            verify(predictiveMaintenanceService).retrievePaginatedUncompletedProcessDrifts(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve paginated uncompleted process drifts : Invalid sort attribute")
        void givenInvalidSortAttribute_whenRetrievePaginatedUncompletedProcessDrifts_thenReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/process-drifts")
                    .param("sortAttribute", "invalidAttribute"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid pagination sort attributes"));

            verify(predictiveMaintenanceService, never()).retrievePaginatedUncompletedProcessDrifts(any());
        }

        @Test
        @DisplayName("Retrieve paginated uncompleted process drifts : Unauthorized")
        void givenNoAuthentication_whenRetrievePaginatedUncompletedProcessDrifts_thenReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/process-drifts"))
                    .andExpect(status().isUnauthorized());

            verify(predictiveMaintenanceService, never()).retrievePaginatedUncompletedProcessDrifts(any());
        }
    }

    private List<MaintenanceDataDto> createMockMaintenanceData() {
        return Arrays.asList(
                MaintenanceDataDto.builder()
                        .stage("Stage1")
                        .cell("Cell1")
                        .component("Component1")
                        .failureType("Type1")
                        .tsRequestCreation(LocalDateTime.parse("2024-01-15T10:30:00", DateTimeFormatter.ISO_DATE_TIME))
                        .build(),
                MaintenanceDataDto.builder()
                        .stage("Stage2")
                        .cell("Cell2")
                        .component("Component2")
                        .failureType("Type2")
                        .tsRequestCreation(LocalDateTime.parse("2024-01-16T14:45:00", DateTimeFormatter.ISO_DATE_TIME))
                        .build()
        );
    }
}
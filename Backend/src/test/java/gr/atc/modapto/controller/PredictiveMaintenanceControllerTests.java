package gr.atc.modapto.controller;

import gr.atc.modapto.dto.files.MaintenanceDataDto;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PredictiveMaintenanceController.class)
@ActiveProfiles("test")
@DisplayName("PredictiveMaintenanceController Tests")
class PredictiveMaintenanceControllerTests {

    @Autowired
    private MockMvc mockMvc;

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
                    eq("2024-01-01"), eq("2024-12-31"))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("startDate", "2024-01-01")
                    .param("endDate", "2024-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].Stage").value("Stage1"))
                    .andExpect(jsonPath("$.data[1].Stage").value("Stage2"))
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange("2024-01-01", "2024-12-31");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Success with start date only")
        void givenStartDateOnly_whenRetrieveMaintenanceData_thenReturnsDataFromStartDate() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq("2024-01-01"), eq(null))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("startDate", "2024-01-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange("2024-01-01", null);
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Retrieve maintenance data : Success with end date only")
        void givenEndDateOnly_whenRetrieveMaintenanceData_thenReturnsDataUntilEndDate() throws Exception {
            // Given
            List<MaintenanceDataDto> mockData = createMockMaintenanceData();
            when(predictiveMaintenanceService.retrieveMaintenanceDataByDateRange(
                    eq(null), eq("2024-12-31"))).thenReturn(mockData);

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("endDate", "2024-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange(null, "2024-12-31");
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
                    eq("2024-01-01"), eq("2024-01-02"))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/eds/maintenance/data")
                    .param("startDate", "2024-01-01")
                    .param("endDate", "2024-01-02"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("Maintenance data within given timeframe retrieved successfully"));

            verify(predictiveMaintenanceService).retrieveMaintenanceDataByDateRange("2024-01-01", "2024-01-02");
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

    private List<MaintenanceDataDto> createMockMaintenanceData() {
        return Arrays.asList(
                MaintenanceDataDto.builder()
                        .stage("Stage1")
                        .cell("Cell1")
                        .component("Component1")
                        .failureType("Type1")
                        .tsRequestCreation("2024-01-15 10:30:00")
                        .build(),
                MaintenanceDataDto.builder()
                        .stage("Stage2")
                        .cell("Cell2")
                        .component("Component2")
                        .failureType("Type2")
                        .tsRequestCreation("2024-01-16 14:45:00")
                        .build()
        );
    }
}
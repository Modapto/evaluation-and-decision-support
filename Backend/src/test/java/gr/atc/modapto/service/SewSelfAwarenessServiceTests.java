package gr.atc.modapto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceInvocations.SewLocalAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewFilteringOptionsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessMonitoringKpisResults;
import gr.atc.modapto.model.sew.SewMonitorKpisComponents;
import gr.atc.modapto.repository.SewMonitorKpisComponentsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessMonitoringKpisResultsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessRealTimeMonitoringResultsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SewSelfAwarenessService Unit Tests")
class SewSelfAwarenessServiceTests {

    @Mock
    private SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository;

    @Mock
    private SewSelfAwarenessRealTimeMonitoringResultsRepository sewSelfAwarenessRealTimeMonitoringResultsRepository;

    @Mock
    private SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @Mock
    private ExceptionHandlerService exceptionHandler;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SewSelfAwarenessService sewSelfAwarenessService;

    private SewSelfAwarenessMonitoringKpisInputDto sampleInputData;
    private SewSelfAwarenessMonitoringKpisResults sampleEntity;
    private SewSelfAwarenessMonitoringKpisResultsDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleInputData = SewSelfAwarenessMonitoringKpisInputDto.builder()
                .moduleId("sew_module_1")
                .smartServiceId("service_1")
                .startDate("2023-01-01")
                .endDate("2023-01-31")
                .build();

        sampleEntity = new SewSelfAwarenessMonitoringKpisResults();
        sampleEntity.setId("test-id-1");
        sampleEntity.setModuleId("sew_module_1");
        sampleEntity.setTimestamp(LocalDateTime.now());

        sampleDto = SewSelfAwarenessMonitoringKpisResultsDto.builder()
                .id("test-id-1")
                .moduleId("sew_module_1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Invoke Self-Awareness Monitoring KPIs Algorithm")
    class InvokeSelfAwarenessMonitoringKpisAlgorithm {

        @Test
        @DisplayName("Invoke monitoring KPIs : Success")
        void givenValidInput_whenInvokeMonitoringKpis_thenCallsSmartServicesInvocationService() {
            SewMonitorKpisComponents mockComponent = new SewMonitorKpisComponents();
            mockComponent.setModuleId("sew_module_1");
            mockComponent.setComponents(Collections.emptyList());

            when(sewMonitorKpisComponentsRepository.findByModuleId("sew_module_1"))
                    .thenReturn(Optional.of(mockComponent));

            doNothing().when(smartServicesInvocationService)
                    .formulateAndImplementSmartServiceRequest(any(), eq(null), eq("Self-Awareness Monitoring KPIs"));

            sewSelfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(sampleInputData);

            verify(sewMonitorKpisComponentsRepository).findByModuleId("sew_module_1");
            verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                    any(SewSelfAwarenessMonitoringKpisInputDto.class),
                    eq(null),
                    eq("Self-Awareness Monitoring KPIs")
            );
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Monitoring KPIs Results")
    class RetrieveLatestMonitoringKpisResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingResults_whenRetrieveLatest_thenReturnsLatestResult() {
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults")))
                    .thenReturn(sampleDto);

            SewSelfAwarenessMonitoringKpisResultsDto result = sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-id-1");
            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults"));
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Monitoring KPIs Results by Module ID")
    class RetrieveLatestMonitoringKpisResultsByModuleId {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenValidModuleId_whenRetrieveLatestByModule_thenReturnsLatestResult() {
            String moduleId = "sew_module_1";
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId")))
                    .thenReturn(sampleDto);

            SewSelfAwarenessMonitoringKpisResultsDto result = sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-id-1");
            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId"));
        }
    }

    @Nested
    @DisplayName("Retrieve All Monitoring KPIs Results")
    class RetrieveAllMonitoringKpisResults {

        @Test
        @DisplayName("Retrieve all results : Success")
        void givenExistingResults_whenRetrieveAll_thenReturnsAllResults() {
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Arrays.asList(sampleDto);
            when(exceptionHandler.handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults")))
                    .thenReturn(expectedResults);

            List<SewSelfAwarenessMonitoringKpisResultsDto> result = sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults();

            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            verify(exceptionHandler).handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults"));
        }
    }

    @Nested
    @DisplayName("Upload Module Components List")
    class UploadModuleComponentsList {

        @Test
        @DisplayName("Upload components list : Success")
        void givenValidComponentsData_whenUploadComponentsList_thenCallsExceptionHandler() {
            SewMonitorKpisComponentsDto componentsData = SewMonitorKpisComponentsDto.builder()
                    .moduleId("sew_module_1")
                    .build();

            when(exceptionHandler.handleOperation(any(), eq("uploadModuleComponentsList")))
                    .thenReturn(null);

            sewSelfAwarenessService.uploadModuleComponentsList(componentsData);

            verify(exceptionHandler).handleOperation(any(), eq("uploadModuleComponentsList"));
        }
    }

    @Nested
    @DisplayName("Retrieve Component List by Module ID")
    class RetrieveComponentListByModuleId {

        @Test
        @DisplayName("Retrieve component list : Success")
        void givenValidModuleId_whenRetrieveComponentList_thenReturnsComponentsList() {
            String moduleId = "sew_module_1";
            SewMonitorKpisComponentsDto expectedDto = SewMonitorKpisComponentsDto.builder()
                    .moduleId(moduleId)
                    .build();

            when(exceptionHandler.handleOperation(any(), eq("retrieveSelfAwarenessComponentListByModuleId")))
                    .thenReturn(expectedDto);

            SewMonitorKpisComponentsDto result = sewSelfAwarenessService.retrieveSelfAwarenessComponentListByModuleId(moduleId);

            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo(moduleId);
            verify(exceptionHandler).handleOperation(any(), eq("retrieveSelfAwarenessComponentListByModuleId"));
        }
    }

    @Nested
    @DisplayName("Delete Component List by Module ID")
    class DeleteComponentListByModuleId {

        @Test
        @DisplayName("Delete component list : Success")
        void givenValidModuleId_whenDeleteComponentList_thenCallsExceptionHandler() {
            String moduleId = "sew_module_1";

            when(exceptionHandler.handleOperation(any(), eq("deleteSelfAwarenessComponentListByModuleId")))
                    .thenReturn(null);

            sewSelfAwarenessService.deleteSelfAwarenessComponentListByModuleId(moduleId);

            verify(exceptionHandler).handleOperation(any(), eq("deleteSelfAwarenessComponentListByModuleId"));
        }
    }

    @Nested
    @DisplayName("Retrieve Filtering Options for Local Analytics")
    class RetrieveFilteringOptionsForLocalAnalytics {

        @Test
        @DisplayName("Retrieve filtering options : Success with valid data")
        void givenValidRequest_whenRetrieveFilteringOptions_thenReturnsFilteringOptions() {
            // Given
            GlobalRequestDto request = GlobalRequestDto.builder()
                    .moduleId("sew_module_1")
                    .smartServiceId("service_1")
                    .build();

            // Mock repository data
            SewSelfAwarenessMonitoringKpisResults entity1 = new SewSelfAwarenessMonitoringKpisResults();
            entity1.setId("result-1");
            entity1.setModuleId("sew_module_1");

            SewSelfAwarenessMonitoringKpisResults entity2 = new SewSelfAwarenessMonitoringKpisResults();
            entity2.setId("result-2");
            entity2.setModuleId("sew_module_2");

            Page<SewSelfAwarenessMonitoringKpisResults> resultsPage = new PageImpl<>(Arrays.asList(entity1, entity2));

            SewFilteringOptionsDto expectedOptions = new SewFilteringOptionsDto();
            when(exceptionHandler.handleOperation(any(), eq("retrieveFilteringOptionsForLocalAnalytics")))
                    .thenReturn(expectedOptions);

            // When
            SewFilteringOptionsDto result = sewSelfAwarenessService.retrieveFilteringOptionsForLocalAnalytics(request);

            // Then
            assertThat(result).isNotNull();
            verify(exceptionHandler).handleOperation(any(), eq("retrieveFilteringOptionsForLocalAnalytics"));
        }

        @Test
        @DisplayName("Retrieve filtering options : Handles empty monitoring results")
        void givenNoMonitoringResults_whenRetrieveFilteringOptions_thenHandlesEmptyData() {
            // Given
            GlobalRequestDto request = GlobalRequestDto.builder()
                    .moduleId("sew_module_1")
                    .smartServiceId("service_1")
                    .build();

            // Mock empty repository data
            Page<SewSelfAwarenessMonitoringKpisResults> emptyPage = new PageImpl<>(Collections.emptyList());

            SewFilteringOptionsDto expectedOptions = new SewFilteringOptionsDto();
            when(exceptionHandler.handleOperation(any(), eq("retrieveFilteringOptionsForLocalAnalytics")))
                    .thenReturn(expectedOptions);

            // When
            SewFilteringOptionsDto result = sewSelfAwarenessService.retrieveFilteringOptionsForLocalAnalytics(request);

            // Then
            assertThat(result).isNotNull();
            verify(exceptionHandler).handleOperation(any(), eq("retrieveFilteringOptionsForLocalAnalytics"));
        }
    }

    @Nested
    @DisplayName("Generate Histogram for Comparing Modules")
    class GenerateHistogramForComparingModules {

        @Test
        @DisplayName("Generate histogram : Success")
        void givenValidRequest_whenGenerateHistogram_thenReturnsEncodedImage() {
            // Given
            SewLocalAnalyticsInputDto analyticsInput = SewLocalAnalyticsInputDto.builder()
                    .firstParameters(new SewFilteringOptionsDto.Options())
                    .secondParameters(new SewFilteringOptionsDto.Options())
                    .build();

            GlobalRequestDto<SewLocalAnalyticsInputDto> request = GlobalRequestDto.<SewLocalAnalyticsInputDto>builder()
                    .moduleId("sew_module_1")
                    .smartServiceId("service_1")
                    .input(analyticsInput)
                    .build();

            String expectedEncodedImage = "base64EncodedImageString";
            when(exceptionHandler.handleOperation(any(), eq("generateHistogramForComparingModules")))
                    .thenReturn(expectedEncodedImage);

            // When
            String result = sewSelfAwarenessService.generateHistogramForComparingModules(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedEncodedImage);
            verify(exceptionHandler).handleOperation(any(), eq("generateHistogramForComparingModules"));
        }

        @Test
        @DisplayName("Generate histogram : Verifies processing flow with valid datetime")
        void givenValidRequest_whenGenerateHistogram_thenFollowsProcessingFlow() {
            // Given
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            SewFilteringOptionsDto.Options optionsDto = new SewFilteringOptionsDto.Options("test", "test", "test", LocalDateTime.parse("10-05-2025 20:00:00", formatter));
            SewLocalAnalyticsInputDto analyticsInput = SewLocalAnalyticsInputDto.builder()
                    .firstParameters(optionsDto)
                    .secondParameters(null)
                    .build();

            GlobalRequestDto<SewLocalAnalyticsInputDto> request = GlobalRequestDto.<SewLocalAnalyticsInputDto>builder()
                    .moduleId("sew_module_1")
                    .smartServiceId("service_1")
                    .input(analyticsInput)
                    .build();

            when(exceptionHandler.handleOperation(any(), eq("generateHistogramForComparingModules")))
                    .thenReturn("encodedImage");

            // When
            String result = sewSelfAwarenessService.generateHistogramForComparingModules(request);

            // Then
            assertThat(result).isNotNull();
            verify(exceptionHandler).handleOperation(any(), eq("generateHistogramForComparingModules"));
        }
    }

    @Nested
    @DisplayName("Date Time Parsing Tests")
    class DateTimeParsingTests {

        @Test
        @DisplayName("Parse valid datetime format : Success")
        void givenValidDateTimeFormat_whenParsing_thenSucceeds() {
            // Given
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String validDateTime = "10-05-2025 20:00:00";

            // When
            LocalDateTime result = LocalDateTime.parse(validDateTime, formatter);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDayOfMonth()).isEqualTo(10);
            assertThat(result.getMonthValue()).isEqualTo(5);
            assertThat(result.getYear()).isEqualTo(2025);
            assertThat(result.getHour()).isEqualTo(20);
        }

        @Test
        @DisplayName("Parse invalid datetime format : Throws exception")
        void givenInvalidDateTimeFormat_whenParsing_thenThrowsException() {
            // Given
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String invalidDateTime = "2025-05-10 20:00:00"; // Wrong format (yyyy-MM-dd instead of dd-MM-yyyy)

            // When & Then
            assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                LocalDateTime.parse(invalidDateTime, formatter)
            )).isInstanceOf(java.time.format.DateTimeParseException.class);
        }
    }
}
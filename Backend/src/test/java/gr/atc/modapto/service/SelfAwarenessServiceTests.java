package gr.atc.modapto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.exception.CustomExceptions.SmartServiceInvocationException;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessMonitoringKpisResults;
import gr.atc.modapto.repository.SewSelfAwarenessMonitoringKpisResultsRepository;
import gr.atc.modapto.service.processors.NoOpResponseProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SelfAwarenessService Unit Tests")
class SelfAwarenessServiceTests {

    @Mock
    private SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @Mock
    private ExceptionHandlerService exceptionHandler;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NoOpResponseProcessor noOpResponseProcessor;

    @InjectMocks
    private SelfAwarenessService selfAwarenessService;

    private SewSelfAwarenessMonitoringKpisInputDto sampleInputDto;
    private SewSelfAwarenessMonitoringKpisResultsDto sampleResultDto;

    @BeforeEach
    void setUp() {
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

        sampleInputDto = SewSelfAwarenessMonitoringKpisInputDto.builder()
                .moduleId("TEST_MODULE")
                .smartServiceId("SELF_AWARENESS_SERVICE")
                .startDate("Start_Date")
                .endDate("End_Date")
                .components(List.of(components))
                .build();

        SewSelfAwarenessMonitoringKpisResults sampleEntity = new SewSelfAwarenessMonitoringKpisResults();
        sampleEntity.setId("test-id");
        sampleEntity.setModuleId("TEST_MODULE");
        sampleEntity.setSmartServiceId("SELF_AWARENESS_SERVICE");
        sampleEntity.setTimestamp("2024-01-15T10:30:00Z");
        sampleEntity.setLigne("Line1");
        sampleEntity.setComponent("Component1");
        sampleEntity.setVariable("Temperature");
        sampleEntity.setStartingDate("2024-01-15T00:00:00Z");
        sampleEntity.setEndingDate("2024-01-15T23:59:59Z");
        sampleEntity.setDataSource("Sensor_Data");
        sampleEntity.setBucket("hourly");
        sampleEntity.setData(Arrays.asList(25.5, 26.1, 27.3, 28.0));

        sampleResultDto = SewSelfAwarenessMonitoringKpisResultsDto.builder()
                .id("test-id")
                .timestamp("2024-01-15T10:30:00Z")
                .moduleId("TEST_MODULE")
                .smartServiceId("SELF_AWARENESS_SERVICE")
                .ligne("Line1")
                .component("Component1")
                .variable("Temperature")
                .startingDate("2024-01-15T00:00:00Z")
                .endingDate("2024-01-15T23:59:59Z")
                .dataSource("Sensor_Data")
                .bucket("hourly")
                .data(Arrays.asList(25.5, 26.1, 27.3, 28.0))
                .build();
    }

    @Nested
    @DisplayName("Invoke Self-Awareness Monitoring KPIs Algorithm")
    class InvokeSelfAwarenessMonitoringKpisAlgorithm {

        @Test
        @DisplayName("Invoke algorithm : Success")
        void givenValidInput_whenInvokeAlgorithm_thenInvokesSuccessfully() throws JsonProcessingException {
            // Given
            when(objectMapper.writeValueAsString(sampleInputDto)).thenReturn("{\"mockJson\":\"data\"}");
            ResponseEntity<DtResponseDto> mockResponse = new ResponseEntity<>(new DtResponseDto(), HttpStatus.OK);
            when(smartServicesInvocationService.invokeSmartService(
                    anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenReturn(mockResponse);
            when(noOpResponseProcessor.processResponse(any(), anyString(), anyString()))
                    .thenReturn(null);

            // When
            selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(sampleInputDto);

            // Then
            verify(objectMapper).writeValueAsString(sampleInputDto);
            verify(smartServicesInvocationService).invokeSmartService(
                    eq("SELF_AWARENESS_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), eq(ModaptoHeader.ASYNC));
            verify(noOpResponseProcessor).processResponse(mockResponse, "TEST_MODULE", "SELF_AWARENESS_SERVICE");
        }

        @Test
        @DisplayName("Invoke algorithm : JSON processing exception")
        void givenJsonProcessingError_whenInvokeAlgorithm_thenThrowsSmartServiceInvocationException() throws JsonProcessingException {
            // Given
            when(objectMapper.writeValueAsString(sampleInputDto))
                    .thenThrow(new JsonProcessingException("JSON error") {});

            // When & Then
            assertThatThrownBy(() -> selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(sampleInputDto))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Unable to convert Self-Awareness Monitoring KPIs input to Base64 Encoding");

            verify(objectMapper).writeValueAsString(sampleInputDto);
            verify(smartServicesInvocationService, never()).invokeSmartService(any(), any(), any(), any());
            verify(noOpResponseProcessor, never()).processResponse(any(), any(), any());
        }

        @Test
        @DisplayName("Invoke algorithm : Smart service invocation failure")
        void givenSmartServiceFailure_whenInvokeAlgorithm_thenThrowsException() throws JsonProcessingException {
            // Given
            when(objectMapper.writeValueAsString(sampleInputDto)).thenReturn("{\"mockJson\":\"data\"}");
            when(smartServicesInvocationService.invokeSmartService(
                    anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenThrow(new RuntimeException("Service invocation failed"));

            // When & Then
            assertThatThrownBy(() -> selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(sampleInputDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Service invocation failed");

            verify(objectMapper).writeValueAsString(sampleInputDto);
            verify(smartServicesInvocationService).invokeSmartService(
                    eq("SELF_AWARENESS_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), eq(ModaptoHeader.ASYNC));
            verify(noOpResponseProcessor, never()).processResponse(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Self-Awareness Monitoring KPIs Results")
    class RetrieveLatestSelfAwarenessMonitoringKpisResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingResults_whenRetrieveLatest_thenReturnsLatestResult() {
            // Given
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults")))
                    .thenReturn(sampleResultDto);

            // When
            SewSelfAwarenessMonitoringKpisResultsDto result = selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-id");
            assertThat(result.getModuleId()).isEqualTo("TEST_MODULE");
            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults"));
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoResults_whenRetrieveLatest_thenThrowsResourceNotFoundException() {
            // Given
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults")))
                    .thenThrow(new ResourceNotFoundException("There are no available SEW Self-Awareness Monitoring KPIs results"));

            // When & Then
            assertThatThrownBy(() -> selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("There are no available SEW Self-Awareness Monitoring KPIs results");

            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults"));
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Self-Awareness Monitoring KPIs Results by Module ID")
    class RetrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId {

        @Test
        @DisplayName("Retrieve latest results by module ID : Success")
        void givenValidModuleId_whenRetrieveLatestByModuleId_thenReturnsLatestResult() {
            // Given
            String moduleId = "TEST_MODULE";
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId")))
                    .thenReturn(sampleResultDto);

            // When
            SewSelfAwarenessMonitoringKpisResultsDto result = 
                selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-id");
            assertThat(result.getModuleId()).isEqualTo("TEST_MODULE");
            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId"));
        }

        @Test
        @DisplayName("Retrieve latest results by module ID : No results found")
        void givenNonExistentModuleId_whenRetrieveLatestByModuleId_thenThrowsResourceNotFoundException() {
            // Given
            String moduleId = "NON_EXISTENT_MODULE";
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId")))
                    .thenThrow(new ResourceNotFoundException("There are no available SEW Self-Awareness Monitoring KPIs results for Module ID: " + moduleId));

            // When & Then
            assertThatThrownBy(() -> selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("There are no available SEW Self-Awareness Monitoring KPIs results for Module ID: NON_EXISTENT_MODULE");

            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId"));
        }
    }

    @Nested
    @DisplayName("Retrieve All Self-Awareness Monitoring KPIs Results")
    class RetrieveAllSelfAwarenessMonitoringKpisResults {

        @Test
        @DisplayName("Retrieve all results : Success")
        void givenExistingResults_whenRetrieveAll_thenReturnsAllResults() {
            // Given
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Collections.singletonList(sampleResultDto);

            when(exceptionHandler.handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults")))
                    .thenReturn(expectedResults);

            // When
            List<SewSelfAwarenessMonitoringKpisResultsDto> results = 
                selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults();

            // Then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            assertThat(results.getFirst().getId()).isEqualTo("test-id");
            verify(exceptionHandler).handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults"));
        }

        @Test
        @DisplayName("Retrieve all results : Empty list")
        void givenNoResults_whenRetrieveAll_thenReturnsEmptyList() {
            // Given
            when(exceptionHandler.handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults")))
                    .thenReturn(Collections.emptyList());

            // When
            List<SewSelfAwarenessMonitoringKpisResultsDto> results = 
                selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults();

            // Then
            assertThat(results).isNotNull();
            assertThat(results).isEmpty();
            verify(exceptionHandler).handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults"));
        }
    }

    @Nested
    @DisplayName("Retrieve All Self-Awareness Monitoring KPIs Results by Module ID")
    class RetrieveAllSelfAwarenessMonitoringKpisResultsByModuleId {

        @Test
        @DisplayName("Retrieve all results by module ID : Success")
        void givenValidModuleId_whenRetrieveAllByModuleId_thenReturnsAllResultsForModule() {
            // Given
            String moduleId = "TEST_MODULE";
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Collections.singletonList(sampleResultDto);

            when(exceptionHandler.handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId")))
                    .thenReturn(expectedResults);

            // When
            List<SewSelfAwarenessMonitoringKpisResultsDto> results = 
                selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);

            // Then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(1);
            verify(exceptionHandler).handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId"));
        }

        @Test
        @DisplayName("Retrieve all results by module ID : Empty list")
        void givenNonExistentModuleId_whenRetrieveAllByModuleId_thenReturnsEmptyList() {
            // Given
            String moduleId = "NON_EXISTENT_MODULE";
            
            when(exceptionHandler.handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId")))
                    .thenReturn(Collections.emptyList());

            // When
            List<SewSelfAwarenessMonitoringKpisResultsDto> results = 
                selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);

            // Then
            assertThat(results).isNotNull();
            assertThat(results).isEmpty();
            verify(exceptionHandler).handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId"));
        }
    }
}
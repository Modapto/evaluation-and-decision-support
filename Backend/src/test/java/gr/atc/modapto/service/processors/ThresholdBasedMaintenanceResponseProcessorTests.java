package gr.atc.modapto.service.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.dt.SmartServiceResponse;
import gr.atc.modapto.dto.serviceResults.sew.SewThresholdBasedPredictiveMaintenanceOutputDto;
import gr.atc.modapto.exception.CustomExceptions.DtmServerErrorException;
import gr.atc.modapto.exception.CustomExceptions.SmartServiceInvocationException;
import gr.atc.modapto.model.serviceResults.SewThresholdBasedPredictiveMaintenanceResult;
import gr.atc.modapto.repository.SewThresholdBasedPredictiveMaintenanceRepository;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ThresholdBasedMaintenanceResponseProcessor Unit Tests")
class ThresholdBasedMaintenanceResponseProcessorTests {

    @Mock
    private SewThresholdBasedPredictiveMaintenanceRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ThresholdBasedMaintenanceResponseProcessor processor;

    private DtResponseDto successfulDtResponse;
    private DtResponseDto failedDtResponse;
    private ResponseEntity<DtResponseDto> successResponse;
    private ResponseEntity<DtResponseDto> failedResponse;
    private SewThresholdBasedPredictiveMaintenanceOutputDto expectedOutputDto;
    private SewThresholdBasedPredictiveMaintenanceResult expectedResult;
    private SmartServiceResponse smartServiceResponse;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        expectedOutputDto = SewThresholdBasedPredictiveMaintenanceOutputDto.builder()
                .recommendation("Replace bearing in motor unit within 72 hours")
                .details("Vibration levels exceeded threshold")
                .build();

        String expectedOutputJson = new ObjectMapper().writeValueAsString(expectedOutputDto);
        String base64EncodedBody = Base64.getEncoder().encodeToString(expectedOutputJson.getBytes());

        smartServiceResponse = new SmartServiceResponse();
        smartServiceResponse.setResponse(base64EncodedBody);

        Map<String, Object> outputArguments = new HashMap<>();
        outputArguments.put("response", base64EncodedBody);

        successfulDtResponse = DtResponseDto.builder()
                .success(true)
                .executionState("Completed")
                .outputArguments(outputArguments)
                .build();

        failedDtResponse = DtResponseDto.builder()
                .success(false)
                .executionState("Failed")
                .messages(List.of())
                .build();

        successResponse = new ResponseEntity<>(successfulDtResponse, HttpStatus.OK);
        failedResponse = new ResponseEntity<>(failedDtResponse, HttpStatus.OK);

        expectedResult = new SewThresholdBasedPredictiveMaintenanceResult();
    }

    @Nested
    @DisplayName("Process Response Success Cases")
    class ProcessResponseSuccess {

        @Test
        @DisplayName("Process response : Success")
        void givenValidSuccessResponse_whenProcessResponse_thenReturnsOutputDto() throws IOException {
            // Given
            when(objectMapper.convertValue(anyMap(), eq(SmartServiceResponse.class)))
                    .thenReturn(smartServiceResponse);
            when(objectMapper.readValue(any(byte[].class), eq(SewThresholdBasedPredictiveMaintenanceOutputDto.class)))
                    .thenReturn(expectedOutputDto);
            when(modelMapper.map(any(SewThresholdBasedPredictiveMaintenanceOutputDto.class), eq(SewThresholdBasedPredictiveMaintenanceResult.class)))
                    .thenReturn(expectedResult);
            when(repository.save(any(SewThresholdBasedPredictiveMaintenanceResult.class)))
                    .thenReturn(expectedResult);

            // When
            SewThresholdBasedPredictiveMaintenanceOutputDto result = processor.processResponse(
                    successResponse, "TEST_MODULE", "THRESHOLD_SERVICE");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo("TEST_MODULE");
            assertThat(result.getSmartServiceId()).isEqualTo("THRESHOLD_SERVICE");
            assertThat(result.getTimestamp()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(result.getRecommendation()).isEqualTo("Replace bearing in motor unit within 72 hours");
            assertThat(result.getDetails()).isEqualTo("Vibration levels exceeded threshold");

            // Verify the sequence of operations
            verify(objectMapper).convertValue(successfulDtResponse.getOutputArguments(), SmartServiceResponse.class);
            verify(objectMapper).readValue(Base64.getDecoder().decode(smartServiceResponse.getResponse()), SewThresholdBasedPredictiveMaintenanceOutputDto.class);
            verify(modelMapper).map(result, SewThresholdBasedPredictiveMaintenanceResult.class);
            verify(repository).save(any(SewThresholdBasedPredictiveMaintenanceResult.class));
        }
    }

    @Nested
    @DisplayName("Process Response Error Cases")
    class ProcessResponseErrors {

        @Test
        @DisplayName("Process response : Null response")
        void givenNullResponse_whenProcessResponse_thenThrowsDtmServerErrorException() {
            // When & Then
            assertThatThrownBy(() -> processor.processResponse(null, "TEST_MODULE", "THRESHOLD_SERVICE"))
                    .isInstanceOf(DtmServerErrorException.class)
                    .hasMessage("No response received from DTM service for threshold maintenance");

            verifyNoInteractions(objectMapper, modelMapper, repository);
        }

        @Test
        @DisplayName("Process response : Null response body")
        void givenNullResponseBody_whenProcessResponse_thenThrowsDtmServerErrorException() {
            // Given
            ResponseEntity<DtResponseDto> nullBodyResponse = new ResponseEntity<>(null, HttpStatus.OK);

            // When & Then
            assertThatThrownBy(() -> processor.processResponse(nullBodyResponse, "TEST_MODULE", "THRESHOLD_SERVICE"))
                    .isInstanceOf(DtmServerErrorException.class)
                    .hasMessage("No response received from DTM service for threshold maintenance");

            verifyNoInteractions(objectMapper, modelMapper, repository);
        }

        @Test
        @DisplayName("Process response : Failed DTM execution")
        void givenFailedDtmExecution_whenProcessResponse_thenThrowsDtmServerErrorException() {
            // When & Then
            assertThatThrownBy(() -> processor.processResponse(failedResponse, "TEST_MODULE", "THRESHOLD_SERVICE"))
                    .isInstanceOf(DtmServerErrorException.class)
                    .hasMessage("DTM service execution failed for threshold maintenance");

            verifyNoInteractions(objectMapper, modelMapper, repository);
        }

        @Test
        @DisplayName("Process response : ObjectMapper conversion error")
        void givenObjectMapperError_whenProcessResponse_thenThrowsSmartServiceInvocationException() {
            // Given
            when(objectMapper.convertValue(anyMap(), eq(SmartServiceResponse.class)))
                    .thenThrow(new IllegalArgumentException("ObjectMapper conversion error"));

            // When & Then
            assertThatThrownBy(() -> processor.processResponse(successResponse, "TEST_MODULE", "THRESHOLD_SERVICE"))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Failed to process DTM threshold-based predictive maintenance response: ObjectMapper conversion error");

            // Verify that the failing method was called, but subsequent steps were not.
            verify(objectMapper).convertValue(anyMap(), eq(SmartServiceResponse.class));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Process response : ModelMapper error")
        void givenModelMapperError_whenProcessResponse_thenThrowsSmartServiceInvocationException() throws IOException {
            // Given
            when(objectMapper.convertValue(anyMap(), eq(SmartServiceResponse.class)))
                    .thenReturn(smartServiceResponse);
            when(objectMapper.readValue(any(byte[].class), eq(SewThresholdBasedPredictiveMaintenanceOutputDto.class)))
                    .thenReturn(expectedOutputDto);
            when(modelMapper.map(any(SewThresholdBasedPredictiveMaintenanceOutputDto.class), eq(SewThresholdBasedPredictiveMaintenanceResult.class)))
                    .thenThrow(new RuntimeException("ModelMapper error"));

            // When & Then
            assertThatThrownBy(() -> processor.processResponse(successResponse, "TEST_MODULE", "THRESHOLD_SERVICE"))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Failed to process DTM threshold-based predictive maintenance response: ModelMapper error");

            verify(modelMapper).map(any(SewThresholdBasedPredictiveMaintenanceOutputDto.class), eq(SewThresholdBasedPredictiveMaintenanceResult.class));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Process response : Repository save error")
        void givenRepositorySaveError_whenProcessResponse_thenThrowsSmartServiceInvocationException() throws IOException {
            // Given
            when(objectMapper.convertValue(anyMap(), eq(SmartServiceResponse.class)))
                    .thenReturn(smartServiceResponse);
            when(objectMapper.readValue(any(byte[].class), eq(SewThresholdBasedPredictiveMaintenanceOutputDto.class)))
                    .thenReturn(expectedOutputDto);
            when(modelMapper.map(any(SewThresholdBasedPredictiveMaintenanceOutputDto.class), eq(SewThresholdBasedPredictiveMaintenanceResult.class)))
                    .thenReturn(expectedResult);
            when(repository.save(any(SewThresholdBasedPredictiveMaintenanceResult.class)))
                    .thenThrow(new RuntimeException("Database save error"));

            // When & Then
            assertThatThrownBy(() -> processor.processResponse(successResponse, "TEST_MODULE", "THRESHOLD_SERVICE"))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Failed to process DTM threshold-based predictive maintenance response: Database save error");

            verify(repository).save(any(SewThresholdBasedPredictiveMaintenanceResult.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration")
    class EdgeCasesAndIntegration {

        @Test
        @DisplayName("Process response : Empty module and service IDs")
        void givenEmptyModuleAndServiceIds_whenProcessResponse_thenProcessesSuccessfully() throws IOException {
            // Given
            when(objectMapper.convertValue(anyMap(), eq(SmartServiceResponse.class))).thenReturn(smartServiceResponse);
            when(objectMapper.readValue(any(byte[].class), eq(SewThresholdBasedPredictiveMaintenanceOutputDto.class))).thenReturn(expectedOutputDto);
            when(modelMapper.map(any(), any())).thenReturn(expectedResult);
            when(repository.save(any())).thenReturn(expectedResult);

            // When
            SewThresholdBasedPredictiveMaintenanceOutputDto result = processor.processResponse(
                    successResponse, "", "");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEmpty();
            assertThat(result.getSmartServiceId()).isEmpty();
            verify(repository).save(any(SewThresholdBasedPredictiveMaintenanceResult.class));
        }

        @Test
        @DisplayName("Process response : Complex output arguments")
        void givenComplexOutputArguments_whenProcessResponse_thenProcessesSuccessfully() throws IOException {
            // Given
            SewThresholdBasedPredictiveMaintenanceOutputDto complexDto = SewThresholdBasedPredictiveMaintenanceOutputDto.builder()
                    .recommendation("Complex recommendation")
                    .details("Detailed analysis with multiple parameters")
                    .build();
            String complexJson = new ObjectMapper().writeValueAsString(complexDto);
            String complexBase64 = Base64.getEncoder().encodeToString(complexJson.getBytes());

            SmartServiceResponse complexServiceResponse = new SmartServiceResponse();
            complexServiceResponse.setResponse(complexBase64);

            Map<String, Object> complexOutputArgs = new HashMap<>();
            complexOutputArgs.put("response", complexBase64);
            complexOutputArgs.put("someOtherField", "someValue"); // The SUT should ignore this

            DtResponseDto complexDtResponse = DtResponseDto.builder()
                    .success(true)
                    .outputArguments(complexOutputArgs)
                    .build();
            ResponseEntity<DtResponseDto> response = new ResponseEntity<>(complexDtResponse, HttpStatus.OK);


            when(objectMapper.convertValue(eq(complexOutputArgs), eq(SmartServiceResponse.class)))
                    .thenReturn(complexServiceResponse);
            when(objectMapper.readValue(any(byte[].class), eq(SewThresholdBasedPredictiveMaintenanceOutputDto.class)))
                    .thenReturn(complexDto);
            when(modelMapper.map(any(SewThresholdBasedPredictiveMaintenanceOutputDto.class), eq(SewThresholdBasedPredictiveMaintenanceResult.class)))
                    .thenReturn(expectedResult);
            when(repository.save(any(SewThresholdBasedPredictiveMaintenanceResult.class)))
                    .thenReturn(expectedResult);

            // When
            SewThresholdBasedPredictiveMaintenanceOutputDto result = processor.processResponse(
                    response, "COMPLEX_MODULE", "COMPLEX_SERVICE");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo("COMPLEX_MODULE");
            assertThat(result.getSmartServiceId()).isEqualTo("COMPLEX_SERVICE");
            assertThat(result.getRecommendation()).isEqualTo("Complex recommendation");

            // Correct verification
            verify(objectMapper).convertValue(complexOutputArgs, SmartServiceResponse.class);
            verify(repository).save(any(SewThresholdBasedPredictiveMaintenanceResult.class));
        }

        @Test
        @DisplayName("Process response : Timestamp assignment validation")
        void givenValidResponse_whenProcessResponse_thenAssignsCurrentTimestamp() throws IOException {
            // Given
            when(objectMapper.convertValue(anyMap(), eq(SmartServiceResponse.class))).thenReturn(smartServiceResponse);
            when(objectMapper.readValue(any(byte[].class), eq(SewThresholdBasedPredictiveMaintenanceOutputDto.class))).thenReturn(expectedOutputDto);
            when(modelMapper.map(any(), any())).thenReturn(expectedResult);
            when(repository.save(any())).thenReturn(expectedResult);

            LocalDateTime beforeExecution = LocalDateTime.now();

            // When
            SewThresholdBasedPredictiveMaintenanceOutputDto result = processor.processResponse(
                    successResponse, "TEST_MODULE", "THRESHOLD_SERVICE");

            LocalDateTime afterExecution = LocalDateTime.now();

            // Then
            assertThat(result.getTimestamp()).isNotNull();
            assertThat(result.getTimestamp()).isBetween(beforeExecution, afterExecution);
        }
    }
}
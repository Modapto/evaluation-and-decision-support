package gr.atc.modapto.service;

import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.serviceInvocations.FftSustainabilityAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceResults.fft.FftSustainabilityAnalyticsResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FftSustainabilityAnalyticsService Unit Tests")
class FftSustainabilityAnalyticsServiceTests {

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @Mock
    private ExceptionHandlerService exceptionHandlerService;

    @InjectMocks
    private FftSustainabilityAnalyticsService fftSustainabilityAnalyticsService;

    private FftSustainabilityAnalyticsResultsDto sampleDto;
    private LocalDateTime sampleTimestamp;

    @BeforeEach
    void setUp() {
        sampleTimestamp = LocalDateTime.of(2025, 7, 17, 10, 30, 0);

        sampleDto = createSampleDto();

        lenient().when(exceptionHandlerService.handleOperation(any(Supplier.class), anyString())).thenAnswer(invocation -> {
            try {
                Supplier<FftSustainabilityAnalyticsResultsDto> supplier = invocation.getArgument(0);
                return supplier.get();
            } catch (Exception e) {
                throw e;
            }
        });
    }

    @Nested
    @DisplayName("Retrieve Latest Sustainability Analytics Results")
    class RetrieveLatestSustainabilityAnalyticsResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingAnalyticsResults_whenRetrieveLatestSustainabilityAnalyticsResults_thenReturnsLatestResult() {
            // Given
            when(smartServicesInvocationService.formulateAndImplementSyncSmartServiceRequest(any(), any(), any()))
                    .thenReturn(ResponseEntity.ok(DtResponseDto.builder().success(true).outputArguments(sampleDto).build()));
            when(smartServicesInvocationService.validateDigitalTwinResponse(any(), anyString())).thenReturn(true);
            when(smartServicesInvocationService.decodeDigitalTwinResponseToDto(eq(FftSustainabilityAnalyticsResultsDto.class), any(), anyString()))
                    .thenReturn(sampleDto);

            // When
            FftSustainabilityAnalyticsResultsDto result = fftSustainabilityAnalyticsService.extractFftSustainabilityAnalytics(GlobalRequestDto.<FftSustainabilityAnalyticsInputDto>builder().build());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRobotUsedPower()).isEqualTo(sampleDto.getRobotUsedPower());
            assertThat(result.getRobotUsedEnergy()).isEqualTo(sampleDto.getRobotUsedEnergy());
            assertThat(result.getTimestampStart()).isEqualTo(sampleDto.getTimestampStart());
            assertThat(result.getTimestampStop()).isEqualTo(sampleDto.getTimestampStop());
            assertThat(result.getMeasurementState()).isEqualTo(sampleDto.getMeasurementState());
            verify(smartServicesInvocationService).formulateAndImplementSyncSmartServiceRequest(any(), any(), any());
            verify(smartServicesInvocationService).validateDigitalTwinResponse(any(), anyString());
            verify(smartServicesInvocationService).decodeDigitalTwinResponseToDto(eq(FftSustainabilityAnalyticsResultsDto.class), any(), anyString());
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoAnalyticsResults_whenRetrieveLatestSustainabilityAnalyticsResults_thenThrowsResourceNotFoundException() {
            // Given
            when(smartServicesInvocationService.formulateAndImplementSyncSmartServiceRequest(any(), any(), any()))
                    .thenReturn(ResponseEntity.ok(DtResponseDto.builder().success(false).build()));
            when(smartServicesInvocationService.validateDigitalTwinResponse(any(), anyString())).thenThrow(new CustomExceptions.DtmServerErrorException("DTM service execution failed"));

            // When & Then
            assertThatThrownBy(() -> fftSustainabilityAnalyticsService.extractFftSustainabilityAnalytics(GlobalRequestDto.<FftSustainabilityAnalyticsInputDto>builder().build()))
                    .isInstanceOf(CustomExceptions.DtmServerErrorException.class)
                    .hasMessageContaining("DTM service execution failed");

            verify(smartServicesInvocationService).formulateAndImplementSyncSmartServiceRequest(any(), any(), any());
            verify(smartServicesInvocationService).validateDigitalTwinResponse(any(), anyString());
            verify(smartServicesInvocationService, never()).decodeDigitalTwinResponseToDto(any(), any(), anyString());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Sustainability Analytics Results by Module ID")
    class RetrieveLatestSustainabilityAnalyticsResultsByModuleId {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenExistingModuleResults_whenRetrieveLatestSustainabilityAnalyticsResultsByModuleId_thenReturnsLatestResult() {
            // Given
            String moduleId = "fft_module_1";
            when(smartServicesInvocationService.formulateAndImplementSyncSmartServiceRequest(any(), eq(moduleId), any()))
                    .thenReturn(ResponseEntity.ok(DtResponseDto.builder().success(true).outputArguments(sampleDto).build()));
            when(smartServicesInvocationService.validateDigitalTwinResponse(any(), anyString())).thenReturn(true);
            when(smartServicesInvocationService.decodeDigitalTwinResponseToDto(eq(FftSustainabilityAnalyticsResultsDto.class), any(), anyString()))
                    .thenReturn(sampleDto);

            // When
            FftSustainabilityAnalyticsResultsDto result = fftSustainabilityAnalyticsService.extractFftSustainabilityAnalytics(GlobalRequestDto.<FftSustainabilityAnalyticsInputDto>builder().moduleId(moduleId).build());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRobotUsedPower()).isEqualTo(sampleDto.getRobotUsedPower());
            assertThat(result.getRobotUsedEnergy()).isEqualTo(sampleDto.getRobotUsedEnergy());
            assertThat(result.getTimestampStart()).isEqualTo(sampleDto.getTimestampStart());
            assertThat(result.getTimestampStop()).isEqualTo(sampleDto.getTimestampStop());
            assertThat(result.getMeasurementState()).isEqualTo(sampleDto.getMeasurementState());
            verify(smartServicesInvocationService).formulateAndImplementSyncSmartServiceRequest(any(), eq(moduleId), any());
            verify(smartServicesInvocationService).validateDigitalTwinResponse(any(), anyString());
            verify(smartServicesInvocationService).decodeDigitalTwinResponseToDto(eq(FftSustainabilityAnalyticsResultsDto.class), any(), anyString());
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestSustainabilityAnalyticsResultsByModuleId_thenThrowsResourceNotFoundException() {
            // Given
            String moduleId = "non_existing_module";
            when(smartServicesInvocationService.formulateAndImplementSyncSmartServiceRequest(any(), eq(moduleId), any()))
                    .thenReturn(ResponseEntity.ok(DtResponseDto.builder().success(false).build()));
            when(smartServicesInvocationService.validateDigitalTwinResponse(any(), anyString())).thenThrow(new CustomExceptions.DtmServerErrorException("DTM service execution failed"));

            // When & Then
            assertThatThrownBy(() -> fftSustainabilityAnalyticsService.extractFftSustainabilityAnalytics(GlobalRequestDto.<FftSustainabilityAnalyticsInputDto>builder().moduleId(moduleId).build()))
                    .isInstanceOf(CustomExceptions.DtmServerErrorException.class)
                    .hasMessageContaining("DTM service execution failed");

            verify(smartServicesInvocationService).formulateAndImplementSyncSmartServiceRequest(any(), eq(moduleId), any());
            verify(smartServicesInvocationService).validateDigitalTwinResponse(any(), anyString());
            verify(smartServicesInvocationService, never()).decodeDigitalTwinResponseToDto(any(), any(), anyString());
        }
    }

    @Nested
    @DisplayName("Invoke Sustainability Analytics")
    class InvokeSustainabilityAnalytics {

        @Test
        @DisplayName("Invoke sustainability analytics : Success")
        void givenValidInput_whenInvokeSustainabilityAnalytics_thenCallsSmartServicesInvocationService() {
            // Given
            FftSustainabilityAnalyticsInputDto inputDto = FftSustainabilityAnalyticsInputDto.builder()
                    .build();
            GlobalRequestDto<FftSustainabilityAnalyticsInputDto> request = GlobalRequestDto.<FftSustainabilityAnalyticsInputDto>builder()
                    .input(inputDto)
                    .moduleId("fft_module_1")
                    .smartServiceId("service_1")
                    .build();

            when(exceptionHandlerService.handleOperation(any(), any())).thenAnswer(invocation -> {
                Supplier<FftSustainabilityAnalyticsResultsDto> supplier = invocation.getArgument(0);
                return supplier.get();
            });

            // When
            fftSustainabilityAnalyticsService.extractFftSustainabilityAnalytics(request);

            // Then
            verify(smartServicesInvocationService).formulateAndImplementSyncSmartServiceRequest(
                    inputDto,
                    "fft_module_1",
                    "service_1"
            );
        }
    }

    private FftSustainabilityAnalyticsResultsDto createSampleDto() {
        return FftSustainabilityAnalyticsResultsDto.builder()
                .robotUsedPower(100.0)
                .robotUsedEnergy(10.0)
                .timestampStart(1672531200.0)
                .timestampStop(1672534800.0)
                .measurementState(1.0)
                .build();
    }
}
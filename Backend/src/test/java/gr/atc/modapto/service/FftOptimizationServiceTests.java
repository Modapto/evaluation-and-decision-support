package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceInvocations.FftOptimizationInputDto;
import gr.atc.modapto.dto.serviceResults.fft.FftOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.FftOptimizationResults;
import gr.atc.modapto.repository.FftOptimizationResultsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FftOptimizationService Unit Tests")
class FftOptimizationServiceTests {

    @Mock
    private FftOptimizationResultsRepository fftOptimizationResultsRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @InjectMocks
    private FftOptimizationService fftOptimizationService;

    private FftOptimizationResultsDto sampleDto;
    private FftOptimizationResults sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDto = createSampleDto();
        sampleEntity = createSampleEntity();
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results")
    class RetrieveLatestOptimizationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingOptimizationResults_whenRetrieveLatestOptimizationResults_thenReturnsLatestResult() {
            // Given
            when(fftOptimizationResultsRepository.findFirstByOrderByTimestampDesc()).thenReturn(Optional.of(sampleEntity));
            when(modelMapper.map(sampleEntity, FftOptimizationResultsDto.class)).thenReturn(sampleDto);

            // When
            FftOptimizationResultsDto result = fftOptimizationService.retrieveLatestOptimizationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            verify(fftOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, FftOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoOptimizationResults_whenRetrieveLatestOptimizationResults_thenThrowsResourceNotFoundException() {
            // Given
            when(fftOptimizationResultsRepository.findFirstByOrderByTimestampDesc()).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fftOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessageContaining("No FFT Optimization Results found");

            verify(fftOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results by Module ID")
    class RetrieveLatestOptimizationResultsByModuleId {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenExistingModuleResults_whenRetrieveLatestOptimizationResultsByModuleId_thenReturnsLatestResult() {
            // Given
            String moduleId = "fft_module_1";
            when(fftOptimizationResultsRepository.findFirstByModuleOrderByTimestampDesc(moduleId)).thenReturn(Optional.of(sampleEntity));
            when(modelMapper.map(sampleEntity, FftOptimizationResultsDto.class)).thenReturn(sampleDto);

            // When
            FftOptimizationResultsDto result = fftOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            verify(fftOptimizationResultsRepository).findFirstByModuleOrderByTimestampDesc(moduleId);
            verify(modelMapper).map(sampleEntity, FftOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestOptimizationResultsByModuleId_thenThrowsResourceNotFoundException() {
            // Given
            String moduleId = "non_existing_module";
            when(fftOptimizationResultsRepository.findFirstByModuleOrderByTimestampDesc(moduleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> fftOptimizationService.retrieveLatestOptimizationResultsByModuleId(moduleId))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessageContaining("No FFT Optimization Results for Module: " + moduleId + " found");

            verify(fftOptimizationResultsRepository).findFirstByModuleOrderByTimestampDesc(moduleId);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Invoke Robot Configuration Optimization")
    class InvokeRobotConfigurationOptimization {

        @Test
        @DisplayName("Invoke robot configuration optimization : Success")
        void givenValidInput_whenInvokeRobotConfigurationOptimization_thenCallsSmartServicesInvocationService() {
            // Given
            FftOptimizationInputDto inputDto = FftOptimizationInputDto.builder()
                    .moduleId("fft_module_1")
                    .smartServiceId("service_1")
                    .build();

            // When
            fftOptimizationService.invokeOptimizationOfRobotConfiguration(inputDto);

            // Then
            verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                    inputDto,
                    "robot-movement",
                    "FFT Robot Configuration Optimization"
            );
        }
    }

    private FftOptimizationResultsDto createSampleDto() {
        return FftOptimizationResultsDto.builder()
                .id("1")
                .timestamp(LocalDateTime.now())
                .module("fft_module_1")
                .energyDifference(4.0)
                .optimizedCodeDat("dat-file")
                .optimizedCodeSrc("src-file")
                .timeLimit(4)
                .timeDifference(4.0)
                .build();
    }

    private FftOptimizationResults createSampleEntity() {
        FftOptimizationResults entity = new FftOptimizationResults();
        entity.setId("1");
        entity.setTimestamp(LocalDateTime.now());
        entity.setModule("fft_module_1");
        entity.setEnergyDifference(4.0);
        entity.setOptimizedCodeDat("dat-file");
        entity.setOptimizedCodeSrc("src-file");
        entity.setTimeLimit(4);
        entity.setTimeDifference(4.0);
        return entity;
    }
}

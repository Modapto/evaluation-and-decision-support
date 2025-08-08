package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.CrfOptimizationResults;
import gr.atc.modapto.repository.CrfOptimizationResultsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrfOptimizationService Unit Tests")
class CrfOptimizationServiceTests {

    @Mock
    private CrfOptimizationResultsRepository crfOptimizationResultsRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CrfOptimizationService crfOptimizationService;

    private CrfOptimizationResults sampleEntity;
    private CrfOptimizationResultsDto sampleDto;
    private String sampleTimestamp;

    @BeforeEach
    void setUp() {
        // Given - Setup test data
        sampleTimestamp = "2025-07-17T10:30:00Z";

        // Create TimeDetail objects
        List<CrfOptimizationResults.OptimizationResults.Exact.TimeDetail> timeDetails = Arrays.asList(
                new CrfOptimizationResults.OptimizationResults.Exact.TimeDetail(
                        "component_A", "component_B", 150L, "position_1", "position_2"
                ),
                new CrfOptimizationResults.OptimizationResults.Exact.TimeDetail(
                        "component_C", "component_D", 200L, "position_3", "position_4"
                )
        );

        // Create Exact
        CrfOptimizationResults.OptimizationResults.Exact exact =
                new CrfOptimizationResults.OptimizationResults.Exact(2500L, timeDetails);

        // Create OptimizationResults
        CrfOptimizationResults.OptimizationResults optimizationResults =
                new CrfOptimizationResults.OptimizationResults(exact, 15.5f);

        // Create entity
        sampleEntity = new CrfOptimizationResults(
                "1", sampleTimestamp, "Optimization completed successfully", "test_module",
                optimizationResults, true, 3000L, 5000L
        );

        // Create corresponding DTO
        sampleDto = createSampleDto();
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results")
    class RetrieveLatestOptimizationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingOptimizationResults_whenRetrieveLatestOptimizationResults_thenReturnsLatestResult() {
            // Given
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            CrfOptimizationResultsDto result = crfOptimizationService.retrieveLatestOptimizationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Optimization completed successfully");
            assertThat(result.getOptimizationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(3000L);
            assertThat(result.getTotalTime()).isEqualTo(5000L);
            assertThat(result.getOptimizationResults()).isNotNull();

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoOptimizationResults_whenRetrieveLatestOptimizationResults_thenThrowsResourceNotFoundException() {
            // Given
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Optimization Results found");

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResults_thenThrowsModelMappingException() {
            // Given
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Optimization Results to DTO - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Optimization Results to DTO - Error: Mapping error occurred");

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResults_thenThrowsException() {
            // Given
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results by Production Module")
    class RetrieveLatestOptimizationResultsByProductionModule {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenExistingModuleResults_whenRetrieveLatestOptimizationResultsByProductionModule_thenReturnsLatestResult() {
            // Given
            String productionModule = "crf_module_1";
            when(crfOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            CrfOptimizationResultsDto result = crfOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Optimization completed successfully");
            assertThat(result.getOptimizationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(3000L);
            assertThat(result.getTotalTime()).isEqualTo(5000L);
            assertThat(result.getOptimizationResults()).isNotNull();

            verify(crfOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsResourceNotFoundException() {
            // Given
            String productionModule = "non_existing_module";
            when(crfOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Optimization Results for Module: " + productionModule + " found");

            verify(crfOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsModelMappingException() {
            // Given
            String productionModule = "crf_module_1";
            when(crfOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(crfOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsException() {
            // Given
            String productionModule = "crf_module_1";
            when(crfOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(crfOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    /*
     * Helper Methods
     */
    private CrfOptimizationResultsDto createSampleDto() {
        List<CrfOptimizationResultsDto.OptimizationResults.Exact.TimeDetail> timeDetailsDto = Arrays.asList(
                new CrfOptimizationResultsDto.OptimizationResults.Exact.TimeDetail(
                        "component_A", "component_B", 150L, "position_1", "position_2"
                ),
                new CrfOptimizationResultsDto.OptimizationResults.Exact.TimeDetail(
                        "component_C", "component_D", 200L, "position_3", "position_4"
                )
        );

        CrfOptimizationResultsDto.OptimizationResults.Exact exactDto =
                new CrfOptimizationResultsDto.OptimizationResults.Exact(2500L, timeDetailsDto);

        CrfOptimizationResultsDto.OptimizationResults optimizationResultsDto =
                new CrfOptimizationResultsDto.OptimizationResults(exactDto, 15.5f);

        return CrfOptimizationResultsDto.builder()
                .id("1")
                .timestamp(sampleTimestamp)
                .message("Optimization completed successfully")
                .optimizationResults(optimizationResultsDto)
                .optimizationRun(true)
                .solutionTime(3000L)
                .totalTime(5000L)
                .build();
    }
}
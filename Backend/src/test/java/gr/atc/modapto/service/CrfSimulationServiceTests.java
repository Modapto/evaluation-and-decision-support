package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.CrfSimulationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.CrfSimulationResults;
import gr.atc.modapto.repository.CrfSimulationResultsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrfSimulationService Unit Tests")
class CrfSimulationServiceTests {

    @Mock
    private CrfSimulationResultsRepository crfSimulationResultsRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CrfSimulationService crfSimulationResultsService;

    private CrfSimulationResults sampleEntity;
    private CrfSimulationResultsDto sampleDto;
    private String sampleTimestamp;

    @BeforeEach
    void setUp() {
        // Given
        sampleTimestamp = "2025-07-17T10:30:00Z";

        // Create GR sequence data
        List<Map<String, String>> grSequence = Arrays.asList(
                Map.of("operation", "cutting", "order", "1"),
                Map.of("operation", "sewing", "order", "2")
        );

        // Create Baseline
        CrfSimulationResults.Baseline.Exact exact = new CrfSimulationResults.Baseline.Exact("1250.50");
        CrfSimulationResults.Baseline.Linear linear = new CrfSimulationResults.Baseline.Linear("1300.75");
        CrfSimulationResults.Baseline baseline = new CrfSimulationResults.Baseline(exact, linear, grSequence);

        // Create BestPhase
        CrfSimulationResults.BestPhase bestPhase = new CrfSimulationResults.BestPhase(
                "1100.25", grSequence, 12.5f, 15.3f, 3L
        );

        // Create entity
        sampleEntity = new CrfSimulationResults(
                "1", sampleTimestamp, "Simulation completed successfully", "test_module",
                true, 5000L, 8000L, baseline, bestPhase
        );

        // Create corresponding DTO
        sampleDto = createSampleDto();
    }

    @Nested
    @DisplayName("Retrieve Latest Simulation Results")
    class RetrieveLatestSimulationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingSimulationResults_whenRetrieveLatestSimulationResults_thenReturnsLatestResult() {
            // Given
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            CrfSimulationResultsDto result = crfSimulationResultsService.retrieveLatestSimulationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Simulation completed successfully");
            assertThat(result.getSimulationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(5000L);
            assertThat(result.getTotalTime()).isEqualTo(8000L);
            assertThat(result.getBaseline()).isNotNull();
            assertThat(result.getBestPhase()).isNotNull();

            verify(crfSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoSimulationResults_whenRetrieveLatestSimulationResults_thenThrowsResourceNotFoundException() {
            // Given
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Simulation Results found");

            verify(crfSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResults_thenThrowsModelMappingException() {
            // Given
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Simulation Results to DTO - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Simulation Results to DTO - Error: Mapping error occurred");

            verify(crfSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResults_thenThrowsException() {
            // Given
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResults())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(crfSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Simulation Results by Production Module")
    class RetrieveLatestSimulationResultsByProductionModule {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenExistingModuleResults_whenRetrieveLatestSimulationResultsByProductionModule_thenReturnsLatestResult() {
            // Given
            String productionModule = "crf_module_1";
            when(crfSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            CrfSimulationResultsDto result = crfSimulationResultsService.retrieveLatestSimulationResultsByProductionModule(productionModule);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Simulation completed successfully");
            assertThat(result.getSimulationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(5000L);
            assertThat(result.getTotalTime()).isEqualTo(8000L);
            assertThat(result.getBaseline()).isNotNull();
            assertThat(result.getBestPhase()).isNotNull();

            verify(crfSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsResourceNotFoundException() {
            // Given
            String productionModule = "non_existing_module";
            when(crfSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Simulation Results for Module: " + productionModule + " found");

            verify(crfSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsModelMappingException() {
            // Given
            String productionModule = "crf_module_1";
            when(crfSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(crfSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsException() {
            // Given
            String productionModule = "crf_module_1";
            when(crfSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(crfSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    /*
     * Helper methods
     */
    private CrfSimulationResultsDto createSampleDto() {
        List<Map<String, String>> grSequenceDto = Arrays.asList(
                Map.of("operation", "cutting", "order", "1"),
                Map.of("operation", "sewing", "order", "2")
        );

        CrfSimulationResultsDto.Baseline.Exact exactDto = new CrfSimulationResultsDto.Baseline.Exact("1250.50");
        CrfSimulationResultsDto.Baseline.Linear linearDto = new CrfSimulationResultsDto.Baseline.Linear("1300.75");
        CrfSimulationResultsDto.Baseline baselineDto = new CrfSimulationResultsDto.Baseline(exactDto, linearDto, grSequenceDto);

        CrfSimulationResultsDto.BestPhase bestPhaseDto = new CrfSimulationResultsDto.BestPhase(
                "1100.25", grSequenceDto, 12.5f, 15.3f, 3L
        );

        return CrfSimulationResultsDto.builder()
                .id("1")
                .timestamp(sampleTimestamp)
                .message("Simulation completed successfully")
                .simulationRun(true)
                .solutionTime(5000L)
                .totalTime(8000L)
                .baseline(baselineDto)
                .bestPhase(bestPhaseDto)
                .build();
    }
}
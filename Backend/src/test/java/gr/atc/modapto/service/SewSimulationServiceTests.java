package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.SewSimulationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.SewSimulationResults;
import gr.atc.modapto.repository.SewSimulationResultsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SewSimulationResultsService Unit Tests")
class SewSimulationServiceTests {

    @Mock
    private SewSimulationResultsRepository sewSimulationResultsRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SewSimulationService sewSimulationService;

    private SewSimulationResults sampleEntity;
    private SewSimulationResultsDto sampleDto;
    private String sampleTimestamp;

    @BeforeEach
    void setUp() {
        // Given - Setup test data
        sampleTimestamp = "2025-07-17T10:30:00Z";

        // Create MetricComparison objects for the entity
        SewSimulationResults.MetricComparison makespanMetric = new SewSimulationResults.MetricComparison(
                240.0, 220.0, -20.0, -8.33
        );

        SewSimulationResults.MetricComparison machineUtilizationMetric = new SewSimulationResults.MetricComparison(
                85.5, 92.3, 6.8, 7.95
        );

        SewSimulationResults.MetricComparison throughputStdevMetric = new SewSimulationResults.MetricComparison(
                12.5, 8.7, -3.8, -30.4
        );

        // Create SimulationData
        SewSimulationResults.SimulationData simulationData = new SewSimulationResults.SimulationData(
                makespanMetric, machineUtilizationMetric, throughputStdevMetric
        );

        // Create main entity
        sampleEntity = new SewSimulationResults("1", sampleTimestamp, simulationData, "test_module");

        // Create corresponding DTO structure
        sampleDto = createSampleDto();
    }

    @Nested
    @DisplayName("Retrieve Latest Simulation Results")
    class RetrieveLatestSimulationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingSimulationResults_whenRetrieveLatestSimulationResults_thenReturnsLatestResult() {
            // Given
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            SewSimulationResultsDto result = sewSimulationService.retrieveLatestSimulationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getSimulationData()).isNotNull();

            verify(sewSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoSimulationResults_whenRetrieveLatestSimulationResults_thenThrowsResourceNotFoundException() {
            // Given
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Simulation Results found");

            verify(sewSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResults_thenThrowsModelMappingException() {
            // Given
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Simulation Results to DTO - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Simulation Results to DTO - Error: Mapping error occurred");

            verify(sewSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResults_thenThrowsException() {
            // Given
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResults())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewSimulationResultsRepository).findFirstByOrderByTimestampDesc();
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
            String productionModule = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            SewSimulationResultsDto result = sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getSimulationData()).isNotNull();

            verify(sewSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsResourceNotFoundException() {
            // Given
            String productionModule = "non_existing_module";
            when(sewSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Simulation Results for Module: " + productionModule + " found");

            verify(sewSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsModelMappingException() {
            // Given
            String productionModule = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(sewSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsException() {
            // Given
            String productionModule = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewSimulationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    /*
     * Helper methods
     */
    private SewSimulationResultsDto createSampleDto() {
        SewSimulationResultsDto.MetricComparison makespan = new SewSimulationResultsDto.MetricComparison(240.0, 220.0, -20.0, -8.33);
        SewSimulationResultsDto.MetricComparison utilization = new SewSimulationResultsDto.MetricComparison(85.5, 92.3, 6.8, 7.95);
        SewSimulationResultsDto.MetricComparison throughput = new SewSimulationResultsDto.MetricComparison(12.5, 8.7, -3.8, -30.4);
        SewSimulationResultsDto.SimulationData data = new SewSimulationResultsDto.SimulationData(makespan, utilization, throughput);
        return SewSimulationResultsDto.builder().id("1").timestamp(sampleTimestamp).simulationData(data).build();
    }
}
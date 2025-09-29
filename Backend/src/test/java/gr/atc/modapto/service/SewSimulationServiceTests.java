package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
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
        sampleTimestamp = "2025-07-17T10:30:00Z";

        SewSimulationResults.KpiMetric makespanMetric = new SewSimulationResults.KpiMetric(
                240.0, 220.0, -20.0, -8.33
        );

        SewSimulationResults.KpiMetric machineUtilizationMetric = new SewSimulationResults.KpiMetric(
                85.5, 92.3, 6.8, 7.95
        );

        SewSimulationResults.KpiMetric throughputStdevMetric = new SewSimulationResults.KpiMetric(
                12.5, 8.7, -3.8, -30.4
        );

        SewSimulationResults.SimulationData simulationData = new SewSimulationResults.SimulationData(
                makespanMetric, machineUtilizationMetric, throughputStdevMetric
        );

        sampleEntity = new SewSimulationResults("1", sampleTimestamp, simulationData, "test_module");

        sampleDto = createSampleDto();
    }

    @Nested
    @DisplayName("Retrieve Latest Simulation Results")
    class RetrieveLatestSimulationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingSimulationResults_whenRetrieveLatestSimulationResults_thenReturnsLatestResult() {
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            SewSimulationResultsDto result = sewSimulationService.retrieveLatestSimulationResults();

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
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Simulation Results found");

            verify(sewSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResults_thenThrowsModelMappingException() {
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Simulation Results to DTO - Error: Mapping error occurred"));

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Simulation Results to DTO - Error: Mapping error occurred");

            verify(sewSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResults_thenThrowsException() {
            when(sewSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

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
            String productionModule = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            SewSimulationResultsDto result = sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getSimulationData()).isNotNull();

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsResourceNotFoundException() {
            String productionModule = "non_existing_module";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Simulation Results for Module: " + productionModule + " found");

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsModelMappingException() {
            String productionModule = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsException() {
            String productionModule = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByProductionModule(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    /*
     * Helper methods
     */
    private SewSimulationResultsDto createSampleDto() {
        SewSimulationResultsDto.KpiMetric makespan = new SewSimulationResultsDto.KpiMetric(240.0, 220.0, -20.0, -8.33);
        SewSimulationResultsDto.KpiMetric utilization = new SewSimulationResultsDto.KpiMetric(85.5, 92.3, 6.8, 7.95);
        SewSimulationResultsDto.KpiMetric throughput = new SewSimulationResultsDto.KpiMetric(12.5, 8.7, -3.8, -30.4);
        SewSimulationResultsDto.SimulationData data = new SewSimulationResultsDto.SimulationData(makespan, utilization, throughput);
        return SewSimulationResultsDto.builder().id("1").timestamp(sampleTimestamp).simulationData(data).build();
    }
}
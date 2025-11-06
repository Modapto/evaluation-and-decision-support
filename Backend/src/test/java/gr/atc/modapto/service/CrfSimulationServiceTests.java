package gr.atc.modapto.service;

import gr.atc.modapto.dto.crf.CrfSimulationKittingConfigDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.crf.CrfSimulationKittingConfig;
import gr.atc.modapto.model.serviceResults.CrfSimulationResults;
import gr.atc.modapto.repository.CrfSimulationKittingConfigRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrfSimulationService Unit Tests")
class CrfSimulationServiceTests {

    @Mock
    private CrfSimulationResultsRepository crfSimulationResultsRepository;

    @Mock
    private CrfSimulationKittingConfigRepository crfSimulationKittingConfigRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ExceptionHandlerService exceptionHandlerService;

    @InjectMocks
    private CrfSimulationService crfSimulationResultsService;

    private CrfSimulationResults sampleEntity;
    private CrfSimulationResultsDto sampleDto;
    private LocalDateTime sampleTimestamp;

    @BeforeEach
    void setUp() {
        sampleTimestamp = LocalDateTime.of(2025, 7, 17, 10, 30, 0);

        Object baseline = new Object();
        Object bestPhase = new Object();

        sampleEntity = new CrfSimulationResults(
                "1", sampleTimestamp, "Simulation completed successfully", "test_module",
                true, 5000L, 8000L, baseline, bestPhase
        );

        sampleDto = createSampleDto();
    }

    @Nested
    @DisplayName("Retrieve Latest Simulation Results")
    class RetrieveLatestSimulationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingSimulationResults_whenRetrieveLatestSimulationResults_thenReturnsLatestResult() {
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            CrfSimulationResultsDto result = crfSimulationResultsService.retrieveLatestSimulationResults();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Simulation completed successfully");
            assertThat(result.getSimulationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(5000L);
            assertThat(result.getTotalTime()).isEqualTo(8000L);

            verify(crfSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoSimulationResults_whenRetrieveLatestSimulationResults_thenThrowsResourceNotFoundException() {
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Simulation Results found");

            verify(crfSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResults_thenThrowsModelMappingException() {
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Simulation Results to DTO - Error: Mapping error occurred"));

            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Simulation Results to DTO - Error: Mapping error occurred");

            verify(crfSimulationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResults_thenThrowsException() {
            when(crfSimulationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

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
            String productionModule = "crf_module_1";
            when(crfSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            CrfSimulationResultsDto result = crfSimulationResultsService.retrieveLatestSimulationResultsByModule(productionModule);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Simulation completed successfully");
            assertThat(result.getSimulationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(5000L);
            assertThat(result.getTotalTime()).isEqualTo(8000L);

            verify(crfSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsResourceNotFoundException() {
            String productionModule = "non_existing_module";
            when(crfSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResultsByModule(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Simulation Results for Module: " + productionModule + " found");

            verify(crfSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsModelMappingException() {
            String productionModule = "crf_module_1";
            when(crfSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResultsByModule(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Simulation Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(crfSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResultsByProductionModule_thenThrowsException() {
            String productionModule = "crf_module_1";
            when(crfSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> crfSimulationResultsService.retrieveLatestSimulationResultsByModule(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(crfSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Retrieve Simulation Kitting Config")
    class RetrieveSimulationKittingConfig {

        @Test
        @DisplayName("Retrieve simulation kitting config : Success")
        void givenExistingConfig_whenRetrieveSimulationKittingConfig_thenReturnsConfig() {
            // Given
            CrfSimulationKittingConfig configEntity = new CrfSimulationKittingConfig();
            configEntity.setId("sim-current");
            configEntity.setFilename("simulation-config.json");
            configEntity.setUploadedAt("2025-01-30T14:30:00Z");
            configEntity.setConfigCase("testing");

            CrfSimulationKittingConfigDto configDto = new CrfSimulationKittingConfigDto();
            configDto.setId("sim-current");
            configDto.setFilename("simulation-config.json");
            configDto.setUploadedAt("2025-01-30T14:30:00Z");
            configDto.setConfigCase("testing");

            when(exceptionHandlerService.handleOperation(any(), eq("retrieveSimulationKittingConfig")))
                    .thenReturn(configDto);

            // When
            CrfSimulationKittingConfigDto result = crfSimulationResultsService.retrieveSimulationKittingConfig();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("sim-current");
            assertThat(result.getFilename()).isEqualTo("simulation-config.json");
            assertThat(result.getConfigCase()).isEqualTo("testing");
            verify(exceptionHandlerService).handleOperation(any(), eq("retrieveSimulationKittingConfig"));
        }

        @Test
        @DisplayName("Retrieve simulation kitting config : Handles exception via exception handler")
        void givenExceptionHandlerError_whenRetrieveSimulationKittingConfig_thenPropagatesException() {
            // Given
            when(exceptionHandlerService.handleOperation(any(), eq("retrieveSimulationKittingConfig")))
                    .thenThrow(new CustomExceptions.ResourceNotFoundException("Config not found"));

            // When & Then
            assertThatThrownBy(() -> crfSimulationResultsService.retrieveSimulationKittingConfig())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("Config not found");

            verify(exceptionHandlerService).handleOperation(any(), eq("retrieveSimulationKittingConfig"));
        }
    }

    /*
     * Helper methods
     */
    private CrfSimulationResultsDto createSampleDto() {
        Object baseline = new Object();
        Object bestPhase = new Object();

        return CrfSimulationResultsDto.builder()
                .id("1")
                .timestamp(sampleTimestamp)
                .message("Simulation completed successfully")
                .simulationRun(true)
                .solutionTime(5000L)
                .totalTime(8000L)
                .baseline(baseline)
                .bestPhase(bestPhase)
                .build();
    }
}
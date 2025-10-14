package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.dto.sew.SewPlantEnvironmentDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.SewSimulationResults;
import gr.atc.modapto.model.sew.SewPlantEnvironment;
import gr.atc.modapto.repository.SewPlantEnvironmentRepository;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SewSimulationResultsService Unit Tests")
class SewSimulationServiceTests {

    @Mock
    private SewSimulationResultsRepository sewSimulationResultsRepository;

    @Mock
    private SewPlantEnvironmentRepository sewPlantEnvironmentRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @InjectMocks
    private SewSimulationService sewSimulationService;

    private SewSimulationResults sampleEntity;
    private SewSimulationResultsDto sampleDto;
    private String sampleTimestamp;
    private SewPlantEnvironment sampleEnvironmentEntity;
    private SewPlantEnvironmentDto sampleEnvironmentDto;

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
    class RetrieveLatestSimulationResultsbyModule {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenExistingModuleResults_whenRetrieveLatestSimulationResultsbyModule_thenReturnsLatestResult() {
            String module = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(module))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenReturn(sampleDto);

            SewSimulationResultsDto result = sewSimulationService.retrieveLatestSimulationResultsByModule(module);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getSimulationData()).isNotNull();

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(module);
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestSimulationResultsbyModule_thenThrowsResourceNotFoundException() {
            String module = "non_existing_module";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(module))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByModule(module))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Simulation Results for Module: " + module + " found");

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(module);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestSimulationResultsbyModule_thenThrowsModelMappingException() {
            String module = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(module))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewSimulationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Simulation Results to DTO for Module: " + module + " - Error: Mapping error occurred"));

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByModule(module))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Simulation Results to DTO for Module: " + module + " - Error: Mapping error occurred");

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(module);
            verify(modelMapper).map(sampleEntity, SewSimulationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestSimulationResultsbyModule_thenThrowsException() {
            String module = "sewing_module_1";
            when(sewSimulationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(module))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> sewSimulationService.retrieveLatestSimulationResultsByModule(module))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewSimulationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(module);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Upload Plant Environment")
    class UploadPlantEnvironment {

        @Test
        @DisplayName("Upload environment : Success")
        void givenValidEnvironment_whenUploadPlantEnvironment_thenSavesSuccessfully() {
            //Given
            Map<String, Object> cells = new HashMap<>();
            cells.put("Cell1", new HashMap<>());

            SewPlantEnvironmentDto.StageDto stageDto = SewPlantEnvironmentDto.StageDto.builder()
                    .wipIn(5)
                    .wipOut(3)
                    .modules("ModuleA")
                    .cells(cells)
                    .build();

            Map<String, SewPlantEnvironmentDto.StageDto> stages = new HashMap<>();
            stages.put("Stage1", stageDto);

            Map<String, Map<String, Integer>> transTimes = new HashMap<>();
            SewPlantEnvironmentDto environmentDto = SewPlantEnvironmentDto.builder()
                    .stages(stages)
                    .transTimes(transTimes)
                    .build();

            SewPlantEnvironment environmentEntity = SewPlantEnvironment.builder()
                    .id("ENV_1")
                    .timestampCreated(LocalDateTime.now())
                    .stages(new HashMap<>())
                    .transTimes(transTimes)
                    .build();

            when(modelMapper.map(environmentDto, SewPlantEnvironment.class)).thenReturn(environmentEntity);
            when(sewPlantEnvironmentRepository.save(any(SewPlantEnvironment.class))).thenReturn(environmentEntity);

            //When
            sewSimulationService.uploadPlantEnvironment(environmentDto);

            //Then
            verify(modelMapper).map(environmentDto, SewPlantEnvironment.class);
            verify(sewPlantEnvironmentRepository).save(any(SewPlantEnvironment.class));
        }

        @Test
        @DisplayName("Upload environment : Mapping exception")
        void givenMappingError_whenUploadPlantEnvironment_thenThrowsModelMappingException() {
            //Given
            SewPlantEnvironmentDto environmentDto = SewPlantEnvironmentDto.builder()
                    .stages(new HashMap<>())
                    .transTimes(new HashMap<>())
                    .build();

            when(modelMapper.map(environmentDto, SewPlantEnvironment.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Mapping error occurred"));

            //When & Then
            assertThatThrownBy(() -> sewSimulationService.uploadPlantEnvironment(environmentDto))
                    .isInstanceOf(CustomExceptions.ServiceOperationException.class)
                    .hasMessage("Failed to upload SEW Current Environment - Error: Mapping error occurred");

            verify(modelMapper).map(environmentDto, SewPlantEnvironment.class);
            verify(sewPlantEnvironmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Plant Environment")
    class RetrieveLatestPlantEnvironment {

        @Test
        @DisplayName("Retrieve latest environment : Success")
        void givenExistingEnvironment_whenRetrieveLatestPlantEnvironment_thenReturnsLatestEnvironment() {
            //Given
            SewPlantEnvironment environmentEntity = SewPlantEnvironment.builder()
                    .id("ENV_1")
                    .timestampCreated(LocalDateTime.now())
                    .stages(new HashMap<>())
                    .transTimes(new HashMap<>())
                    .build();

            SewPlantEnvironmentDto environmentDto = SewPlantEnvironmentDto.builder()
                    .stages(new HashMap<>())
                    .transTimes(new HashMap<>())
                    .build();

            when(sewPlantEnvironmentRepository.findFirstByOrderByTimestampCreatedDesc())
                    .thenReturn(Optional.of(environmentEntity));
            when(modelMapper.map(environmentEntity, SewPlantEnvironmentDto.class)).thenReturn(environmentDto);

            //When
            SewPlantEnvironmentDto result = sewSimulationService.retrieveLatestPlantEnvironment();

            //Then
            assertThat(result).isNotNull();
            verify(sewPlantEnvironmentRepository).findFirstByOrderByTimestampCreatedDesc();
            verify(modelMapper).map(environmentEntity, SewPlantEnvironmentDto.class);
        }

        @Test
        @DisplayName("Retrieve latest environment : No environment found")
        void givenNoEnvironment_whenRetrieveLatestPlantEnvironment_thenThrowsResourceNotFoundException() {
            //Given
            when(sewPlantEnvironmentRepository.findFirstByOrderByTimestampCreatedDesc())
                    .thenReturn(Optional.empty());

            //When & Then
            assertThatThrownBy(() -> sewSimulationService.retrieveLatestPlantEnvironment())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Current Environment found");

            verify(sewPlantEnvironmentRepository).findFirstByOrderByTimestampCreatedDesc();
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
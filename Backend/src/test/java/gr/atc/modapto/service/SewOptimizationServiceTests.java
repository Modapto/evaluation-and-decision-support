package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.serviceResults.SewOptimizationResults;
import gr.atc.modapto.repository.SewOptimizationResultsRepository;
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
@DisplayName("SewOptimizationService Unit Tests")
class SewOptimizationServiceTests {

    @Mock
    private SewOptimizationResultsRepository sewOptimizationResultsRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SewOptimizationService sewOptimizationService;

    private SewOptimizationResults sampleEntity;
    private SewOptimizationResultsDto sampleDto;
    private String sampleTimestamp;

    @BeforeEach
    void setUp() {
        // Given - Setup test data
        sampleTimestamp = "2025-07-17T10:30:00Z";

        // Create nested data structures
        SewOptimizationResults.TimeRange timeRange1 = new SewOptimizationResults.TimeRange("08:00", "10:30");
        SewOptimizationResults.TimeRange timeRange2 = new SewOptimizationResults.TimeRange("10:30", "12:00");

        SewOptimizationResults.MetricsData metrics = new SewOptimizationResults.MetricsData("240");

        // Create seq map
        Map<String, Map<String, String>> seq = new HashMap<>();
        Map<String, String> seqItem1 = new HashMap<>();
        seqItem1.put("operation", "cutting");
        seqItem1.put("duration", "30");
        seq.put("seq_1", seqItem1);

        // Create orders map
        Map<String, Map<String, Map<String, Map<String, SewOptimizationResults.TimeRange>>>> orders = new HashMap<>();
        Map<String, Map<String, Map<String, SewOptimizationResults.TimeRange>>> orderLevel1 = new HashMap<>();
        Map<String, Map<String, SewOptimizationResults.TimeRange>> orderLevel2 = new HashMap<>();
        Map<String, SewOptimizationResults.TimeRange> orderLevel3 = new HashMap<>();
        orderLevel3.put("task_1", timeRange1);
        orderLevel3.put("task_2", timeRange2);
        orderLevel2.put("machine_1", orderLevel3);
        orderLevel1.put("order_1", orderLevel2);
        orders.put("production_line_1", orderLevel1);

        // Create init order
        List<String> initOrder = Arrays.asList("order_1", "order_2", "order_3");

        SewOptimizationResults.SolutionData solutionData = new SewOptimizationResults.SolutionData(
                metrics, seq, orders, initOrder
        );

        // Create data map
        Map<String, SewOptimizationResults.SolutionData> data = new HashMap<>();
        data.put("solution_1", solutionData);

        // Create entity
        sampleEntity = new SewOptimizationResults("1", sampleTimestamp, "test_module", data);

        // Create corresponding DTO
        sampleDto = SewOptimizationResultsDto.builder()
                .id("1")
                .timestamp(sampleTimestamp)
                .data(createSampleDtoData())
                .build();
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results")
    class RetrieveLatestOptimizationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingOptimizationResults_whenRetrieveLatestOptimizationResults_thenReturnsLatestResult() {
            // Given
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData()).containsKey("solution_1");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Success with complex data structure")
        void givenComplexOptimizationResults_whenRetrieveLatestOptimizationResults_thenReturnsCompleteStructure() {
            // Given
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData()).hasSize(1);

            // Verify
            assertThat(result.getData().get("solution_1")).isNotNull();

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoOptimizationResults_whenRetrieveLatestOptimizationResults_thenThrowsResourceNotFoundException() {
            // Given
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Optimization Results found");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResults_thenThrowsModelMappingException() {
            // Given
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Optimization Results to DTO - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Optimization Results to DTO - Error: Mapping error occurred");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResults_thenThrowsException() {
            // Given
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Multiple solutions in data")
        void givenMultipleSolutionsInData_whenRetrieveLatestOptimizationResults_thenReturnsAllSolutions() {
            // Given
            Map<String, SewOptimizationResults.SolutionData> multipleData = new HashMap<>();
            multipleData.put("solution_1", createSampleSolutionData("180"));
            multipleData.put("solution_2", createSampleSolutionData("220"));

            SewOptimizationResults entityWithMultipleSolutions = new SewOptimizationResults("3", sampleTimestamp, "test_module", multipleData);
            SewOptimizationResultsDto dtoWithMultipleSolutions = SewOptimizationResultsDto.builder()
                    .id("3")
                    .timestamp(sampleTimestamp)
                    .data(createMultipleSolutionDtoData())
                    .build();

            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(entityWithMultipleSolutions));

            when(modelMapper.map(entityWithMultipleSolutions, SewOptimizationResultsDto.class))
                    .thenReturn(dtoWithMultipleSolutions);

            // When
            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("3");
            assertThat(result.getData()).hasSize(2);
            assertThat(result.getData()).containsKeys("solution_1", "solution_2");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(entityWithMultipleSolutions, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Latest timestamp ordering")
        void givenMultipleResultsWithDifferentTimestamps_whenRetrieveLatestOptimizationResults_thenReturnsLatestByTimestamp() {
            // Given
            String laterTimestamp = "2024-01-16T12:00:00Z";
            SewOptimizationResults latestEntity = new SewOptimizationResults("4", laterTimestamp, "test_module", createSampleDataMap());
            SewOptimizationResultsDto latestDto = SewOptimizationResultsDto.builder()
                    .id("4")
                    .timestamp(laterTimestamp)
                    .data(createSampleDtoData())
                    .build();

            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(latestEntity));

            when(modelMapper.map(latestEntity, SewOptimizationResultsDto.class))
                    .thenReturn(latestDto);

            // When
            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("4");
            assertThat(result.getTimestamp()).isEqualTo(laterTimestamp);
            assertThat(result.getData()).isNotNull();

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(latestEntity, SewOptimizationResultsDto.class);
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results by Production Module")
    class RetrieveLatestOptimizationResultsByProductionModule {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenExistingModuleResults_whenRetrieveLatestOptimizationResultsByProductionModule_thenReturnsLatestResult() {
            // Given
            String productionModule = "sewing_module_1";
            when(sewOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            // When
            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getData()).isNotNull();

            verify(sewOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsResourceNotFoundException() {
            // Given
            String productionModule = "non_existing_module";
            when(sewOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Optimization Results for Module: " + productionModule + " found");

            verify(sewOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsModelMappingException() {
            // Given
            String productionModule = "sewing_module_1";
            when(sewOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            // When & Then
            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(sewOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsException() {
            // Given
            String productionModule = "sewing_module_1";
            when(sewOptimizationResultsRepository.findFirstByProductionModuleOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResultsByProductionModule(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewOptimizationResultsRepository).findFirstByProductionModuleOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    /*
     * Helper methods
     */
    private SewOptimizationResults.SolutionData createSampleSolutionData(String makespan) {
        SewOptimizationResults.MetricsData metrics = new SewOptimizationResults.MetricsData(makespan);

        Map<String, Map<String, String>> seq = new HashMap<>();
        Map<String, String> seqItem = new HashMap<>();
        seqItem.put("operation", "sewing");
        seqItem.put("duration", "45");
        seq.put("seq_1", seqItem);

        Map<String, Map<String, Map<String, Map<String, SewOptimizationResults.TimeRange>>>> orders = new HashMap<>();
        List<String> initOrder = Arrays.asList("order_1", "order_2");

        return new SewOptimizationResults.SolutionData(metrics, seq, orders, initOrder);
    }

    private Map<String, SewOptimizationResults.SolutionData> createSampleDataMap() {
        Map<String, SewOptimizationResults.SolutionData> data = new HashMap<>();
        data.put("solution_1", createSampleSolutionData("200"));
        return data;
    }

    private Map<String, SewOptimizationResultsDto.SolutionData> createSampleDtoData() {
        // Create DTO structure matching the exact model structure
        Map<String, SewOptimizationResultsDto.SolutionData> data = new HashMap<>();

        // Create MetricsData for DTO
        SewOptimizationResultsDto.MetricsData metricsDto = new SewOptimizationResultsDto.MetricsData("240");

        // Create seq map for DTO
        Map<String, Map<String, String>> seqDto = new HashMap<>();
        Map<String, String> seqItemDto = new HashMap<>();
        seqItemDto.put("operation", "cutting");
        seqItemDto.put("duration", "30");
        seqDto.put("seq_1", seqItemDto);

        // Create orders map for DTO
        Map<String, Map<String, Map<String, Map<String, SewOptimizationResultsDto.TimeRange>>>> ordersDto = new HashMap<>();

        // Create init order for DTO
        List<String> initOrderDto = Arrays.asList("order_1", "order_2", "order_3");

        // Create SolutionData for DTO
        SewOptimizationResultsDto.SolutionData solutionDataDto = new SewOptimizationResultsDto.SolutionData(
                metricsDto, seqDto, ordersDto, initOrderDto
        );

        data.put("solution_1", solutionDataDto);
        return data;
    }

    private Map<String, SewOptimizationResultsDto.SolutionData> createMultipleSolutionDtoData() {
        Map<String, SewOptimizationResultsDto.SolutionData> data = new HashMap<>();

        // Solution 1
        SewOptimizationResultsDto.MetricsData metrics1 = new SewOptimizationResultsDto.MetricsData("180");
        SewOptimizationResultsDto.SolutionData solution1 = new SewOptimizationResultsDto.SolutionData(
                metrics1, new HashMap<>(), new HashMap<>(), new ArrayList<>()
        );

        // Solution 2
        SewOptimizationResultsDto.MetricsData metrics2 = new SewOptimizationResultsDto.MetricsData("220");
        SewOptimizationResultsDto.SolutionData solution2 = new SewOptimizationResultsDto.SolutionData(
                metrics2, new HashMap<>(), new HashMap<>(), new ArrayList<>()
        );

        data.put("solution_1", solution1);
        data.put("solution_2", solution2);
        return data;
    }
}
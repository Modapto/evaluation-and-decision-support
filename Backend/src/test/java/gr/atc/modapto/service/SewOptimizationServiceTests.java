package gr.atc.modapto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.serviceInvocations.SewOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.sew.ProductionSchedule;
import gr.atc.modapto.model.serviceResults.SewOptimizationResults;
import gr.atc.modapto.repository.ProductionScheduleRepository;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SewOptimizationService Unit Tests")
class SewOptimizationServiceTests {

    @Mock
    private SewOptimizationResultsRepository sewOptimizationResultsRepository;

    @Mock
    private ProductionScheduleRepository productionScheduleRepository;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @Mock
    private ExceptionHandlerService exceptionHandlerService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SewOptimizationService sewOptimizationService;

    private SewOptimizationResults sampleEntity;
    private SewOptimizationResultsDto sampleDto;
    private String sampleTimestamp;
    private SewProductionScheduleDto sampleScheduleDto;
    private ProductionSchedule sampleScheduleEntity;
    private SewOptimizationInputDto sampleOptimizationInput;

    @BeforeEach
    void setUp() {
        sampleTimestamp = "2025-07-17T10:30:00Z";
        SewOptimizationResults.MetricsData metrics = new SewOptimizationResults.MetricsData("240");

        Map<String, Map<String, String>> seq = new HashMap<>();
        Map<String, String> seqItem = new HashMap<>();
        seqItem.put("operation", "sewing");
        seq.put("seq_1", seqItem);

        Map<String, Map<String, Map<String, Map<String, SewOptimizationResults.TimeRange>>>> orders = new HashMap<>();

        List<String> initOrder = Arrays.asList("order_1", "order_2");

        SewOptimizationResults.SolutionData solutionData = new SewOptimizationResults.SolutionData(
                metrics, seq, orders, initOrder
        );

        Map<String, SewOptimizationResults.SolutionData> data = new HashMap<>();
        data.put("solution_1", solutionData);

        sampleEntity = new SewOptimizationResults("1", sampleTimestamp, "test_module", data);

        sampleDto = SewOptimizationResultsDto.builder()
                .id("1")
                .timestamp(sampleTimestamp)
                .data(createSampleDtoData())
                .build();

        Map<String, SewProductionScheduleDto.DailyDataDto> scheduleDtoData = new HashMap<>();
        SewProductionScheduleDto.DailyDataDto dailyDataDto = new SewProductionScheduleDto.DailyDataDto();
        dailyDataDto.setGivenOrder(Arrays.asList("order1", "order2"));
        scheduleDtoData.put("day1", dailyDataDto);

        sampleScheduleDto = new SewProductionScheduleDto();
        sampleScheduleDto.setData(scheduleDtoData);

        Map<String, ProductionSchedule.DailyData> scheduleEntityData = new HashMap<>();
        ProductionSchedule.DailyData dailyDataEntity = new ProductionSchedule.DailyData();
        dailyDataEntity.setGivenOrder(Arrays.asList("order1", "order2"));
        scheduleEntityData.put("day1", dailyDataEntity);

        sampleScheduleEntity = new ProductionSchedule();
        sampleScheduleEntity.setId("latest-production-schedule");
        sampleScheduleEntity.setData(scheduleEntityData);

        SewOptimizationInputDto.Config config = SewOptimizationInputDto.Config.builder()
                .timeLimit(30.0)
                .returnedSols(2)
                .kpis(Set.of("makespan", "machine_utilization"))
                .build();

        Map<String, SewProductionScheduleDto.DailyDataDto> inputData = new HashMap<>();
        SewProductionScheduleDto.DailyDataDto dailyData = new SewProductionScheduleDto.DailyDataDto();
        dailyData.setGivenOrder(Arrays.asList("order1", "order2"));
        inputData.put("day1", dailyData);

        sampleOptimizationInput = SewOptimizationInputDto.builder()
                .moduleId("TEST_MODULE")
                .smartServiceId("SEW_OPT_SERVICE")
                .config(config)
                .input(inputData)
                .build();
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results")
    class RetrieveLatestOptimizationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingOptimizationResults_whenRetrieveLatestOptimizationResults_thenReturnsLatestResult() {
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

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
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getData()).isNotNull();
            assertThat(result.getData()).hasSize(1);

            assertThat(result.getData().get("solution_1")).isNotNull();

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoOptimizationResults_whenRetrieveLatestOptimizationResults_thenThrowsResourceNotFoundException() {
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Optimization Results found");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResults_thenThrowsModelMappingException() {
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Optimization Results to DTO - Error: Mapping error occurred"));

            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Optimization Results to DTO - Error: Mapping error occurred");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResults_thenThrowsException() {
            when(sewOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Multiple solutions in data")
        void givenMultipleSolutionsInData_whenRetrieveLatestOptimizationResults_thenReturnsAllSolutions() {
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

            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

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

            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResults();

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
            String productionModule = "sewing_module_1";
            when(sewOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            SewOptimizationResultsDto result = sewOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getData()).isNotNull();

            verify(sewOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsResourceNotFoundException() {
            String productionModule = "non_existing_module";
            when(sewOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No SEW Optimization Results for Module: " + productionModule + " found");

            verify(sewOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsModelMappingException() {
            String productionModule = "sewing_module_1";
            when(sewOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, SewOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse SEW Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse SEW Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(sewOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, SewOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResultsByProductionModule_thenThrowsException() {
            String productionModule = "sewing_module_1";
            when(sewOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(sewOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
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
        Map<String, SewOptimizationResultsDto.SolutionData> data = new HashMap<>();

        SewOptimizationResultsDto.KpiData kpiData = new SewOptimizationResultsDto.KpiData();
        kpiData.setMakespan("240");
        kpiData.setMachineUtilization(0.85);

        Map<String, Map<String, SewOptimizationResultsDto.OrderData>> scheduleData = new HashMap<>();
        Map<String, SewOptimizationResultsDto.OrderData> daySchedule = new HashMap<>();

        SewOptimizationResultsDto.OrderData orderData = new SewOptimizationResultsDto.OrderData();
        orderData.setOrderId("order_1");

        orderData.setMachines(new HashMap<>());

        daySchedule.put("order_1", orderData);
        scheduleData.put("day_1", daySchedule);

        SewOptimizationResultsDto.SolutionData solutionData = new SewOptimizationResultsDto.SolutionData();
        solutionData.setKpis(kpiData);
        solutionData.setSchedule(scheduleData);

        data.put("solution_1", solutionData);
        return data;
    }

    private Map<String, SewOptimizationResultsDto.SolutionData> createMultipleSolutionDtoData() {
        Map<String, SewOptimizationResultsDto.SolutionData> data = new HashMap<>();

        // Solution 1
        SewOptimizationResultsDto.KpiData kpi1 = new SewOptimizationResultsDto.KpiData();
        kpi1.setMakespan("180");
        kpi1.setMachineUtilization(0.80);
        SewOptimizationResultsDto.SolutionData solution1 = new SewOptimizationResultsDto.SolutionData();
        solution1.setKpis(kpi1);
        solution1.setSchedule(new HashMap<>());

        // Solution 2
        SewOptimizationResultsDto.KpiData kpi2 = new SewOptimizationResultsDto.KpiData();
        kpi2.setMakespan("220");
        kpi2.setMachineUtilization(0.75);
        SewOptimizationResultsDto.SolutionData solution2 = new SewOptimizationResultsDto.SolutionData();
        solution2.setKpis(kpi2);
        solution2.setSchedule(new HashMap<>());

        data.put("solution_1", solution1);
        data.put("solution_2", solution2);
        return data;
    }

    @Nested
    @DisplayName("Upload Production Schedule")
    class UploadProductionSchedule {

        @Test
        @DisplayName("Upload production schedule : Success")
        void givenValidScheduleDto_whenUploadProductionSchedule_thenSavesSuccessfully() {
            when(modelMapper.map(sampleScheduleDto, ProductionSchedule.class))
                    .thenReturn(sampleScheduleEntity);
            when(exceptionHandlerService.handleOperation(any(), eq("uploadProductionSchedule")))
                    .thenAnswer(invocation -> {
                        return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
                    });
            when(productionScheduleRepository.save(any(ProductionSchedule.class)))
                    .thenReturn(sampleScheduleEntity);

            assertDoesNotThrow(() -> sewOptimizationService.uploadProductionSchedule(sampleScheduleDto));

            verify(modelMapper).map(sampleScheduleDto, ProductionSchedule.class);
            verify(productionScheduleRepository).save(any(ProductionSchedule.class));
            verify(exceptionHandlerService).handleOperation(any(), eq("uploadProductionSchedule"));
        }

        @Test
        @DisplayName("Upload production schedule : Exception handler catches error")
        void givenExceptionDuringUpload_whenUploadProductionSchedule_thenExceptionHandlerCatchesError() {
            when(exceptionHandlerService.handleOperation(any(), eq("uploadProductionSchedule")))
                    .thenThrow(new RuntimeException("Database error"));

            assertThatThrownBy(() -> sewOptimizationService.uploadProductionSchedule(sampleScheduleDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(exceptionHandlerService).handleOperation(any(), eq("uploadProductionSchedule"));
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Production Schedule")
    class RetrieveLatestProductionSchedule {

        @Test
        @DisplayName("Retrieve latest production schedule : Success")
        void givenExistingProductionSchedule_whenRetrieveLatestProductionSchedule_thenReturnsSchedule() {
            when(productionScheduleRepository.findById("latest-production-schedule"))
                    .thenReturn(Optional.of(sampleScheduleEntity));
            when(modelMapper.map(sampleScheduleEntity, SewProductionScheduleDto.class))
                    .thenReturn(sampleScheduleDto);

            SewProductionScheduleDto result = sewOptimizationService.retrieveLatestProductionSchedule();

            assertThat(result).isNotNull();
            assertThat(result.getData()).isNotEmpty();
            verify(productionScheduleRepository).findById("latest-production-schedule");
            verify(modelMapper).map(sampleScheduleEntity, SewProductionScheduleDto.class);
        }

        @Test
        @DisplayName("Retrieve latest production schedule : No schedule found")
        void givenNoProductionSchedule_whenRetrieveLatestProductionSchedule_thenThrowsResourceNotFoundException() {
            when(productionScheduleRepository.findById("latest-production-schedule"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sewOptimizationService.retrieveLatestProductionSchedule())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("There is no stored production schedule in the DB");

            verify(productionScheduleRepository).findById("latest-production-schedule");
            verify(modelMapper, never()).map(any(), eq(SewProductionScheduleDto.class));
        }
    }

    @Nested
    @DisplayName("Invoke Optimization of Production Schedules")
    class InvokeOptimizationOfProductionSchedules {

        @Test
        @DisplayName("Invoke optimization : Success with provided data")
        void givenInvocationDataWithProvidedData_whenInvokeOptimization_thenInvokesSmartService() throws Exception {

            sewOptimizationService.invokeOptimizationOfProductionSchedules(sampleOptimizationInput);

            verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                    eq(sampleOptimizationInput),
                    eq("hffs"),
                    eq("SEW Optimization of Production Schedules")
            );
            verify(productionScheduleRepository, never()).findById(any());
            verify(objectMapper, never()).valueToTree(any());
        }

        @Test
        @DisplayName("Invoke optimization : Success with empty input retrieves from DB")
        void givenInvocationDataWithEmptyInput_whenInvokeOptimization_thenRetrievesFromDbAndInvokes() throws Exception {
            sampleOptimizationInput.setInput(new HashMap<>());
            when(productionScheduleRepository.findById("latest-production-schedule"))
                    .thenReturn(Optional.of(sampleScheduleEntity));
            when(modelMapper.map(sampleScheduleEntity, SewProductionScheduleDto.class))
                    .thenReturn(sampleScheduleDto);
            JsonNode jsonData = objectMapper.createObjectNode();

            sewOptimizationService.invokeOptimizationOfProductionSchedules(sampleOptimizationInput);

            verify(productionScheduleRepository).findById("latest-production-schedule");
            verify(modelMapper).map(sampleScheduleEntity, SewProductionScheduleDto.class);
            verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                    eq(sampleOptimizationInput),
                    eq("hffs"),
                    eq("SEW Optimization of Production Schedules")
            );
        }

        @Test
        @DisplayName("Invoke optimization : ResourceNotFoundException when no schedule found")
        void givenEmptyInputAndNoScheduleInDb_whenInvokeOptimization_thenThrowsResourceNotFoundException() {
            sampleOptimizationInput.setInput(new HashMap<>());
            when(productionScheduleRepository.findById("latest-production-schedule"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sewOptimizationService.invokeOptimizationOfProductionSchedules(sampleOptimizationInput))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("There is no stored production schedule in the DB");

            verify(productionScheduleRepository).findById("latest-production-schedule");
            verify(smartServicesInvocationService, never()).formulateAndImplementSmartServiceRequest(any(), any(), any());
        }
    }

}
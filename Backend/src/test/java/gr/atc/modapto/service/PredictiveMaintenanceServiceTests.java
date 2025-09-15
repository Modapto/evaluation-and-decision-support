package gr.atc.modapto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.dt.DtInputDto;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.serviceInvocations.SewGroupingPredictiveMaintenanceInputDataDto;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.dto.serviceResults.sew.SewThresholdBasedPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import gr.atc.modapto.dto.sew.SewComponentInfoDto;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.model.MaintenanceData;
import gr.atc.modapto.model.sew.SewComponentInfo;
import gr.atc.modapto.repository.*;
import gr.atc.modapto.service.processors.NoOpResponseProcessor;
import gr.atc.modapto.service.processors.ThresholdBasedMaintenanceResponseProcessor;
import gr.atc.modapto.exception.CustomExceptions.FileHandlingException;
import gr.atc.modapto.exception.CustomExceptions.ModelMappingException;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.util.ExcelFilesUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PredictiveMaintenanceService Unit Tests")
class PredictiveMaintenanceServiceTests {

    @Mock
    private MaintenanceDataRepository maintenanceDataRepository;
    
    @Mock
    private SewComponentInfoRepository componentInfoRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;
    
    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;
    
    @Mock
    private ThresholdBasedMaintenanceResponseProcessor thresholdMaintenanceResponseProcessor;
    
    @Mock
    private NoOpResponseProcessor noOpResponseProcessor;

    @Mock
    private SewGroupingBasedPredictiveMaintenanceRepository sewGroupingBasedPredictiveMaintenanceRepository;

    @Mock
    private SewThresholdBasedPredictiveMaintenanceRepository sewThresholdBasedPredictiveMaintenanceRepository;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private gr.atc.modapto.kafka.KafkaMessageProducer kafkaMessageProducer;

    @InjectMocks
    private PredictiveMaintenanceService predictiveMaintenanceService;

    private MaintenanceDataDto sampleDto;
    private MaintenanceData sampleEntity;
    private List<MaintenanceDataDto> sampleDtoList;
    private List<MaintenanceData> sampleEntityList;

    @BeforeEach
    void setUp() {
        // Given - Setup test data
        sampleDto = MaintenanceDataDto.builder()
                .stage("TestStage")
                .cell("TestCell")
                .component("TestComponent")
                .failureType("TestFailureType")
                .tsRequestCreation(LocalDateTime.parse("2024-01-15T10:30:00", DateTimeFormatter.ISO_DATE_TIME))
                .build();

        sampleEntity = new MaintenanceData();
        sampleEntity.setId("1");
        sampleEntity.setStage("TestStage");
        sampleEntity.setCell("TestCell");
        sampleEntity.setComponent("TestComponent");
        sampleEntity.setFailureType("TestFailureType");
        sampleEntity.setTsRequestCreation(LocalDateTime.parse("2024-01-15T10:30:00", DateTimeFormatter.ISO_DATE_TIME));

        sampleDtoList = Arrays.asList(
                MaintenanceDataDto.builder()
                        .stage("Stage1")
                        .cell("Cell1")
                        .component("Component1")
                        .tsRequestCreation(LocalDateTime.parse("2024-01-15T10:30:00", DateTimeFormatter.ISO_DATE_TIME))
                        .build(),
                MaintenanceDataDto.builder()
                        .stage("Stage2")
                        .cell("Cell2")
                        .component("Component2")
                        .tsRequestCreation(LocalDateTime.parse("2024-01-16T11:30:00", DateTimeFormatter.ISO_DATE_TIME))
                        .build()
        );

        sampleEntityList = Arrays.asList(
                createMaintenanceData("1", "Stage1", "Cell1", "Component1", "2024-01-15T10:30:00"),
                createMaintenanceData("2", "Stage2", "Cell2", "Component2", "2024-01-16T11:30:00")
        );
    }

    @Nested
    @DisplayName("Store CORIM Data")
    class StoreCorimData {

        @Test
        @DisplayName("Store CORIM data : Success")
        void givenValidCorimFile_whenStoreCorimData_thenSavesDataSuccessfully() {
            // Given
            try (MockedStatic<ExcelFilesUtils> mockedStatic = mockStatic(ExcelFilesUtils.class)) {
                mockedStatic.when(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile))
                        .thenReturn(sampleDtoList);

                when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                        .thenReturn(sampleEntity);

                when(maintenanceDataRepository.saveAll(anyList()))
                        .thenReturn(sampleEntityList);

                // When
                predictiveMaintenanceService.storeCorimData(multipartFile);

                // Then
                mockedStatic.verify(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile));
                verify(modelMapper, times(2)).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
                verify(maintenanceDataRepository).saveAll(anyList());
            }
        }

        @Test
        @DisplayName("Store CORIM data : Large dataset batch processing")
        void givenLargeDataset_whenStoreCorimData_thenProcessesInBatches() {
            // Given
            List<MaintenanceDataDto> largeDataset = createLargeDataset(2500); // More than batch size
            
            try (MockedStatic<ExcelFilesUtils> mockedStatic = mockStatic(ExcelFilesUtils.class)) {
                mockedStatic.when(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile))
                        .thenReturn(largeDataset);

                when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                        .thenReturn(sampleEntity);

                when(maintenanceDataRepository.saveAll(anyList()))
                        .thenReturn(sampleEntityList);

                // When
                predictiveMaintenanceService.storeCorimData(multipartFile);

                // Then
                mockedStatic.verify(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile));
                verify(modelMapper, times(2500)).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
                verify(maintenanceDataRepository, times(3)).saveAll(anyList()); // 3 batches (1000 + 1000 + 500)
            }
        }

        @Test
        @DisplayName("Store CORIM data : Empty dataset")
        void givenEmptyDataset_whenStoreCorimData_thenHandlesGracefully() {
            // Given
            try (MockedStatic<ExcelFilesUtils> mockedStatic = mockStatic(ExcelFilesUtils.class)) {
                mockedStatic.when(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile))
                        .thenReturn(Collections.emptyList());

                // When
                predictiveMaintenanceService.storeCorimData(multipartFile);

                // Then
                mockedStatic.verify(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile));
                verify(modelMapper, never()).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
                verify(maintenanceDataRepository, never()).saveAll(anyList());
            }
        }

        @Test
        @DisplayName("Store CORIM data : Excel processing failure")
        void givenExcelProcessingFailure_whenStoreCorimData_thenThrowsFileHandlingException() {
            // Given
            try (MockedStatic<ExcelFilesUtils> mockedStatic = mockStatic(ExcelFilesUtils.class)) {
                mockedStatic.when(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile))
                        .thenThrow(new RuntimeException("Excel processing error"));

                // When & Then
                assertThatThrownBy(() -> predictiveMaintenanceService.storeCorimData(multipartFile))
                        .isInstanceOf(FileHandlingException.class)
                        .hasMessage("Error processing Excel file");

                verify(modelMapper, never()).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
                verify(maintenanceDataRepository, never()).saveAll(anyList());
            }
        }

        @Test
        @DisplayName("Store CORIM data : Database save failure")
        void givenDatabaseSaveFailure_whenStoreCorimData_thenThrowsFileHandlingException() {
            // Given
            try (MockedStatic<ExcelFilesUtils> mockedStatic = mockStatic(ExcelFilesUtils.class)) {
                mockedStatic.when(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile))
                        .thenReturn(sampleDtoList);

                when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                        .thenReturn(sampleEntity);

                when(maintenanceDataRepository.saveAll(anyList()))
                        .thenThrow(new RuntimeException("Database error"));

                // When & Then
                assertThatThrownBy(() -> predictiveMaintenanceService.storeCorimData(multipartFile))
                        .isInstanceOf(FileHandlingException.class)
                        .hasMessage("Error processing Excel file");

                verify(maintenanceDataRepository).saveAll(anyList());
            }
        }

        @Test
        @DisplayName("Store CORIM data : Mapping error")
        void givenMappingError_whenStoreCorimData_thenThrowsFileHandlingException() {
            // Given
            try (MockedStatic<ExcelFilesUtils> mockedStatic = mockStatic(ExcelFilesUtils.class)) {
                mockedStatic.when(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile))
                        .thenReturn(sampleDtoList);

                when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                        .thenThrow(new RuntimeException("Mapping error"));

                // When & Then
                assertThatThrownBy(() -> predictiveMaintenanceService.storeCorimData(multipartFile))
                        .isInstanceOf(FileHandlingException.class)
                        .hasMessage("Error processing Excel file");

                verify(modelMapper).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
                verify(maintenanceDataRepository, never()).saveAll(anyList());
            }
        }

        @Test
        @DisplayName("Store CORIM data : Exact batch size")
        void givenExactBatchSize_whenStoreCorimData_thenProcessesCorrectly() {
            // Given
            List<MaintenanceDataDto> exactBatchSize = createLargeDataset(1000); // Exactly batch size
            
            try (MockedStatic<ExcelFilesUtils> mockedStatic = mockStatic(ExcelFilesUtils.class)) {
                mockedStatic.when(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(multipartFile))
                        .thenReturn(exactBatchSize);

                when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                        .thenReturn(sampleEntity);

                when(maintenanceDataRepository.saveAll(anyList()))
                        .thenReturn(sampleEntityList);

                // When
                predictiveMaintenanceService.storeCorimData(multipartFile);

                // Then
                verify(maintenanceDataRepository, times(1)).saveAll(anyList()); // Exactly 1 batch
                verify(modelMapper, times(1000)).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
            }
        }
    }

    @Nested
    @DisplayName("Retrieve Maintenance Data by Date Range")
    class RetrieveMaintenanceDataByDateRange {

        @Test
        @DisplayName("Retrieve maintenance data : Success with date range")
        void givenStartAndEndDates_whenRetrieveMaintenanceData_thenReturnsFilteredData() {
            // Given
            SearchHits<MaintenanceData> mockSearchHits = createMockSearchHits(sampleEntityList);
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(mockSearchHits);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange("2024-01-01", "2024-01-31");

            // Then
            assertThat(result).hasSize(2);
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, times(2)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Success with start date only")
        void givenStartDateOnly_whenRetrieveMaintenanceData_thenReturnsFilteredData() {
            // Given
            SearchHits<MaintenanceData> mockSearchHits = createMockSearchHits(sampleEntityList);
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(mockSearchHits);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange("2024-01-01", null);

            // Then
            assertThat(result).hasSize(2);
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, times(2)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Success with end date only")
        void givenEndDateOnly_whenRetrieveMaintenanceData_thenReturnsFilteredData() {
            // Given
            SearchHits<MaintenanceData> mockSearchHits = createMockSearchHits(sampleEntityList);
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(mockSearchHits);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange(null, "2024-01-31");

            // Then
            assertThat(result).hasSize(2);
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, times(2)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Success with no dates")
        void givenNoDates_whenRetrieveMaintenanceData_thenReturnsAllData() {
            // Given
            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange(null, null);

            // Then
            assertThat(result).hasSize(2);
            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(elasticsearchOperations, never()).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, times(2)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Empty result")
        void givenNoDataFound_whenRetrieveMaintenanceData_thenReturnsEmptyList() {
            // Given
            SearchHits<MaintenanceData> emptySearchHits = createMockSearchHits(Collections.emptyList());
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(emptySearchHits);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange("2024-01-01", "2024-01-31");

            // Then
            assertThat(result).isEmpty();
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, never()).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Elasticsearch error")
        void givenElasticsearchError_whenRetrieveMaintenanceData_thenThrowsException() {
            // Given
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenThrow(new RuntimeException("Elasticsearch error"));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange("2024-01-01", "2024-01-31"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Elasticsearch error");

            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, never()).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Repository error")
        void givenRepositoryError_whenRetrieveAllData_thenThrowsException() {
            // Given
            when(maintenanceDataRepository.findAll(any(Pageable.class)))
                    .thenThrow(new RuntimeException("Repository error"));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange(null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Repository error");

            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(modelMapper, never()).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Date format edge cases")
        void givenDateFormatEdgeCases_whenRetrieveMaintenanceData_thenHandlesGracefully() {
            // Given
            SearchHits<MaintenanceData> mockSearchHits = createMockSearchHits(sampleEntityList);
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(mockSearchHits);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange("2024-01-01T00:00:00", "2024-01-31T23:59:59");

            // Then
            assertThat(result).hasSize(2);
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, times(2)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Large result set performance")
        void givenLargeResultSet_whenRetrieveMaintenanceData_thenProcessesEfficiently() {
            // Given
            List<MaintenanceData> largeDataset = createLargeMaintenanceDataset();
            SearchHits<MaintenanceData> mockSearchHits = createMockSearchHits(largeDataset);
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(mockSearchHits);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            long startTime = System.currentTimeMillis();
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange("2024-01-01", "2024-12-31");
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(result).hasSize(5000);
            assertThat(endTime - startTime).isLessThan(5000); // Should complete within 5 seconds
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, times(5000)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }
    }

    @Nested
    @DisplayName("Integration and Edge Cases")
    class IntegrationAndEdgeCases {

        @Test
        @DisplayName("Store CORIM data : Null file")
        void givenNullFile_whenStoreCorimData_thenThrowsFileHandlingException() {
            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.storeCorimData(null))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessage("Error processing Excel file");
        }

        @Test
        @DisplayName("Retrieve maintenance data : Null date parameters")
        void givenNullDateParameters_whenRetrieveMaintenanceData_thenReturnsAllData() {
            // Given
            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange(null, null);

            // Then
            assertThat(result).hasSize(2);
            verify(maintenanceDataRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Retrieve maintenance data : Empty string date parameters")
        void givenEmptyStringDateParameters_whenRetrieveMaintenanceData_thenReturnsAllData() {
            // Given
            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            List<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange(null,null);

            // Then
            assertThat(result).hasSize(2);
            verify(maintenanceDataRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Performance : Concurrent access")
        void givenConcurrentAccess_whenRetrieveMaintenanceData_thenHandlesProperlyOptimized() throws InterruptedException {
            // Given
            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            Runnable retrievalTask = () -> predictiveMaintenanceService
                    .retrieveMaintenanceDataByDateRange(null, null);

            Thread thread1 = new Thread(retrievalTask);
            Thread thread2 = new Thread(retrievalTask);

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            // Then
            verify(maintenanceDataRepository, times(2)).findAll(any(Pageable.class));
            verify(modelMapper, times(4)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }
    }

    @Nested
    @DisplayName("Invoke Threshold-Based Predictive Maintenance")
    class InvokeThresholdBasedPredictiveMaintenance {

        @Test
        @DisplayName("Invoke threshold-based maintenance : Success")
        void givenValidInput_whenInvokeThresholdMaintenance_thenReturnsResult() throws JsonProcessingException {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();

            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            ResponseEntity<DtResponseDto> mockResponse = new ResponseEntity<>(new DtResponseDto(), HttpStatus.OK);
            when(smartServicesInvocationService.invokeSmartService(anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenReturn(mockResponse);

            SewThresholdBasedPredictiveMaintenanceOutputDto expectedOutput = SewThresholdBasedPredictiveMaintenanceOutputDto.builder()
                    .id("test-id")
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();
            when(thresholdMaintenanceResponseProcessor.processResponse(any(), anyString(), anyString()))
                    .thenReturn(expectedOutput);

            // Mock ObjectMapper to return valid JSON string
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");


            // When
            SewThresholdBasedPredictiveMaintenanceOutputDto result = predictiveMaintenanceService
                    .invokeThresholdBasedPredictiveMaintenance(inputData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo("TEST_MODULE");
            assertThat(result.getSmartServiceId()).isEqualTo("THRESHOLD_SERVICE");
            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService).invokeSmartService(eq("THRESHOLD_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), any(ModaptoHeader.class));
            verify(thresholdMaintenanceResponseProcessor).processResponse(mockResponse, "TEST_MODULE", "THRESHOLD_SERVICE");
        }

        @Test
        @DisplayName("Invoke threshold-based maintenance : Mapping exception")
        void givenMappingError_whenInvokeThresholdMaintenance_thenThrowsModelMappingException() {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();

            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenThrow(new MappingException(List.of(new ErrorMessage("Mapping error"))));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.invokeThresholdBasedPredictiveMaintenance(inputData))
                    .isInstanceOf(ModelMappingException.class)
                    .hasMessageContaining("Exception occurred while mapping Threshold Based Predictive Maintenance Entity to DTO");

            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService, never()).invokeSmartService(anyString(), anyString(), any(), any());
        }

        @Test
        @DisplayName("Invoke threshold-based maintenance : Empty maintenance data")
        void givenEmptyMaintenanceData_whenInvokeThresholdMaintenance_thenProceedsSuccessfully() throws JsonProcessingException {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();

            Page<MaintenanceData> emptyPage = new PageImpl<>(Collections.emptyList());
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            ResponseEntity<DtResponseDto> mockResponse = new ResponseEntity<>(new DtResponseDto(), HttpStatus.OK);
            when(smartServicesInvocationService.invokeSmartService(anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenReturn(mockResponse);

            SewThresholdBasedPredictiveMaintenanceOutputDto expectedOutput = SewThresholdBasedPredictiveMaintenanceOutputDto.builder()
                    .id("test-id")
                    .build();
            when(thresholdMaintenanceResponseProcessor.processResponse(any(), anyString(), anyString()))
                    .thenReturn(expectedOutput);
            // Mock ObjectMapper to return valid JSON string
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");

            // When
            SewThresholdBasedPredictiveMaintenanceOutputDto result = predictiveMaintenanceService
                    .invokeThresholdBasedPredictiveMaintenance(inputData);

            // Then
            assertThat(result).isNotNull();
            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService).invokeSmartService(eq("THRESHOLD_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), any(ModaptoHeader.class));
            verify(thresholdMaintenanceResponseProcessor).processResponse(mockResponse, "TEST_MODULE", "THRESHOLD_SERVICE");
        }
    }

    @Nested
    @DisplayName("Invoke Grouping Predictive Maintenance")
    class InvokeGroupingPredictiveMaintenance {

        @Test
        @DisplayName("Invoke grouping maintenance : Success")
        void givenValidInput_whenInvokeGroupingMaintenance_thenInvokesSuccessfully() throws JsonProcessingException {
            // Given
            SewGroupingPredictiveMaintenanceInputDataDto inputData = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("GROUPING_SERVICE")
                    .setupCost(500.0)
                    .downtimeCostRate(150.0)
                    .noRepairmen(3)
                    .build();

            List<SewComponentInfo> componentEntities = Arrays.asList(
                    createSewComponentInfo("1", "Stage1", "Cell1", "Module1", "MOD1"),
                    createSewComponentInfo("2", "Stage2", "Cell2", "Module2", "MOD2")
            );
            Page<SewComponentInfo> mockPage = new PageImpl<>(componentEntities);
            when(componentInfoRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            SewComponentInfoDto componentDto = SewComponentInfoDto.builder()
                    .stage("Stage1")
                    .cell("Cell1")
                    .module("Module1")
                    .moduleId("MOD1")
                    .build();
            when(modelMapper.map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class)))
                    .thenReturn(componentDto);

            ResponseEntity<DtResponseDto> mockResponse = new ResponseEntity<>(new DtResponseDto(), HttpStatus.OK);
            when(smartServicesInvocationService.invokeSmartService(anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenReturn(mockResponse);

            when(noOpResponseProcessor.processResponse(any(), anyString(), anyString()))
                    .thenReturn(null);

            // Mock ObjectMapper to return valid JSON string
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");

            // When
            predictiveMaintenanceService.invokeGroupingPredictiveMaintenance(inputData);

            // Then
            verify(componentInfoRepository).findAll(any(Pageable.class));
            verify(modelMapper, times(2)).map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class));
            verify(smartServicesInvocationService).invokeSmartService(eq("GROUPING_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), any(ModaptoHeader.class));
            verify(noOpResponseProcessor).processResponse(mockResponse, "TEST_MODULE", "GROUPING_SERVICE");
            
            // Verify component list was set
            assertThat(inputData.getComponentList()).hasSize(2);
        }

        @Test
        @DisplayName("Invoke grouping maintenance : Component mapping exception")
        void givenComponentMappingError_whenInvokeGroupingMaintenance_thenThrowsModelMappingException() {
            // Given
            SewGroupingPredictiveMaintenanceInputDataDto inputData = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("GROUPING_SERVICE")
                    .build();

            List<SewComponentInfo> componentEntities = List.of(createSewComponentInfo("1", "Stage1", "Cell1", "Module1", "MOD1"));
            Page<SewComponentInfo> mockPage = new PageImpl<>(componentEntities);
            when(componentInfoRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

            when(modelMapper.map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class)))
                    .thenThrow(new MappingException(List.of(new ErrorMessage("Component mapping error"))));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.invokeGroupingPredictiveMaintenance(inputData))
                    .isInstanceOf(ModelMappingException.class)
                    .hasMessageContaining("Exception occurred while mapping Grouping Predictive Maintenance Entity to DTO");

            verify(componentInfoRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService, never()).invokeSmartService(anyString(), anyString(), any(), any());
        }

        @Test
        @DisplayName("Invoke grouping maintenance : Empty component list")
        void givenEmptyComponentList_whenInvokeGroupingMaintenance_thenProceedsSuccessfully() throws JsonProcessingException {
            // Given
            SewGroupingPredictiveMaintenanceInputDataDto inputData = SewGroupingPredictiveMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("GROUPING_SERVICE")
                    .build();

            Page<SewComponentInfo> emptyPage = new PageImpl<>(Collections.emptyList());
            when(componentInfoRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            ResponseEntity<DtResponseDto> mockResponse = new ResponseEntity<>(new DtResponseDto(), HttpStatus.OK);
            when(smartServicesInvocationService.invokeSmartService(anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenReturn(mockResponse);

            when(noOpResponseProcessor.processResponse(any(), anyString(), anyString()))
                    .thenReturn(null);

            // Mock ObjectMapper to return valid JSON string
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");

            // When
            predictiveMaintenanceService.invokeGroupingPredictiveMaintenance(inputData);

            // Then
            verify(componentInfoRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService).invokeSmartService(eq("GROUPING_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), any(ModaptoHeader.class));
            verify(noOpResponseProcessor).processResponse(mockResponse, "TEST_MODULE", "GROUPING_SERVICE");
            
            // Verify empty component list was set
            assertThat(inputData.getComponentList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Process Drift Management")
    class ProcessDriftManagement {

        @Test
        @DisplayName("Declare process drift : Success with timestamps set automatically")
        void givenValidProcessDriftData_whenDeclareProcessDrift_thenReturnsId() {
            // Given
            MaintenanceDataDto inputData = MaintenanceDataDto.builder()
                    .stage("TestStage")
                    .cell("TestCell")
                    .module("TestModule")
                    .moduleId("MOD001")
                    .component("TestComponent")
                    .failureType("ProcessDrift")
                    .build();

            MaintenanceData savedEntity = new MaintenanceData();
            savedEntity.setId("generated-id");
            savedEntity.setStage("TestStage");
            savedEntity.setCell("TestCell");
            savedEntity.setModule("TestModule");
            savedEntity.setModuleId("MOD001");
            savedEntity.setComponent("TestComponent");
            savedEntity.setFailureType("ProcessDrift");

            when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                    .thenReturn(savedEntity);
            when(maintenanceDataRepository.save(any(MaintenanceData.class)))
                    .thenReturn(savedEntity);

            // When
            String result = predictiveMaintenanceService.declareProcessDrift(inputData);

            // Then
            assertThat(result).isEqualTo("generated-id");
            verify(modelMapper).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
            verify(maintenanceDataRepository).save(any(MaintenanceData.class));
        }

        @Test
        @DisplayName("Declare process drift : Success with pre-existing timestamps")
        void givenProcessDriftDataWithTimestamps_whenDeclareProcessDrift_thenPreservesTimestamps() {
            // Given
            LocalDateTime existingTimestamp = LocalDateTime.now().minusHours(1);
            MaintenanceDataDto inputData = MaintenanceDataDto.builder()
                    .stage("TestStage")
                    .cell("TestCell")
                    .component("TestComponent")
                    .failureType("ProcessDrift")
                    .tsRequestCreation(existingTimestamp)
                    .tsInterventionStarted(existingTimestamp)
                    .build();

            MaintenanceData savedEntity = new MaintenanceData();
            savedEntity.setId("generated-id");

            when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                    .thenReturn(savedEntity);
            when(maintenanceDataRepository.save(any(MaintenanceData.class)))
                    .thenReturn(savedEntity);

            // When
            String result = predictiveMaintenanceService.declareProcessDrift(inputData);

            // Then
            assertThat(result).isEqualTo("generated-id");
            assertThat(inputData.getTsRequestCreation()).isEqualTo(existingTimestamp);
            assertThat(inputData.getTsInterventionStarted()).isEqualTo(existingTimestamp);
            verify(maintenanceDataRepository).save(any(MaintenanceData.class));
        }

        @Test
        @DisplayName("Declare process drift : Mapping exception")
        void givenMappingError_whenDeclareProcessDrift_thenThrowsModelMappingException() {
            // Given
            MaintenanceDataDto inputData = MaintenanceDataDto.builder()
                    .stage("TestStage")
                    .cell("TestCell")
                    .component("TestComponent")
                    .build();

            when(modelMapper.map(any(MaintenanceDataDto.class), eq(MaintenanceData.class)))
                    .thenThrow(new MappingException(List.of(new ErrorMessage("Mapping error"))));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.declareProcessDrift(inputData))
                    .isInstanceOf(ModelMappingException.class)
                    .hasMessageContaining("Unable to parse SEW Predictive Maintenance Results to DTO or vice-versa");

            verify(modelMapper).map(any(MaintenanceDataDto.class), eq(MaintenanceData.class));
            verify(maintenanceDataRepository, never()).save(any(MaintenanceData.class));
        }

        @Test
        @DisplayName("Retrieve process drift by ID : Success")
        void givenValidProcessDriftId_whenRetrieveProcessDriftById_thenReturnsDto() {
            // Given
            String driftId = "test-drift-id";
            MaintenanceData entity = new MaintenanceData();
            entity.setId(driftId);
            entity.setStage("TestStage");
            entity.setCell("TestCell");
            entity.setComponent("TestComponent");
            entity.setFailureType("ProcessDrift");

            MaintenanceDataDto expectedDto = MaintenanceDataDto.builder()
                    .stage("TestStage")
                    .cell("TestCell")
                    .component("TestComponent")
                    .failureType("ProcessDrift")
                    .build();

            when(maintenanceDataRepository.findById(driftId))
                    .thenReturn(Optional.of(entity));
            when(modelMapper.map(entity, MaintenanceDataDto.class))
                    .thenReturn(expectedDto);

            // When
            MaintenanceDataDto result = predictiveMaintenanceService.retrieveProcessDriftById(driftId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStage()).isEqualTo("TestStage");
            assertThat(result.getCell()).isEqualTo("TestCell");
            assertThat(result.getComponent()).isEqualTo("TestComponent");
            assertThat(result.getFailureType()).isEqualTo("ProcessDrift");
            verify(maintenanceDataRepository).findById(driftId);
            verify(modelMapper).map(entity, MaintenanceDataDto.class);
        }

        @Test
        @DisplayName("Retrieve process drift by ID : Not found")
        void givenNonExistentId_whenRetrieveProcessDriftById_thenThrowsResourceNotFoundException() {
            // Given
            String driftId = "non-existent-id";
            when(maintenanceDataRepository.findById(driftId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.retrieveProcessDriftById(driftId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Process drift with ID = 'non-existent-id' not found in PKB");

            verify(maintenanceDataRepository).findById(driftId);
            verify(modelMapper, never()).map(any(), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve process drift by ID : Mapping exception")
        void givenMappingError_whenRetrieveProcessDriftById_thenThrowsModelMappingException() {
            // Given
            String driftId = "test-drift-id";
            MaintenanceData entity = new MaintenanceData();
            entity.setId(driftId);

            when(maintenanceDataRepository.findById(driftId))
                    .thenReturn(Optional.of(entity));
            when(modelMapper.map(entity, MaintenanceDataDto.class))
                    .thenThrow(new MappingException(List.of(new ErrorMessage("Mapping error"))));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.retrieveProcessDriftById(driftId))
                    .isInstanceOf(ModelMappingException.class)
                    .hasMessageContaining("Unable to parse SEW Predictive Maintenance Results to DTO or vice-versa");

            verify(maintenanceDataRepository).findById(driftId);
            verify(modelMapper).map(entity, MaintenanceDataDto.class);
        }

        @Test
        @DisplayName("Complete process drift : Success")
        void givenValidIdAndEndTime_whenCompleteProcessDrift_thenUpdatesEntity() {
            // Given
            String driftId = "test-drift-id";
            LocalDateTime endTime = LocalDateTime.now();
            MaintenanceData entity = new MaintenanceData();
            entity.setId(driftId);
            entity.setStage("TestStage");

            when(maintenanceDataRepository.findById(driftId))
                    .thenReturn(Optional.of(entity));
            when(maintenanceDataRepository.save(any(MaintenanceData.class)))
                    .thenReturn(entity);

            // When
            predictiveMaintenanceService.completeProcessDrift(driftId, endTime);

            // Then
            verify(maintenanceDataRepository).findById(driftId);
            verify(maintenanceDataRepository).save(any(MaintenanceData.class));
        }

        @Test
        @DisplayName("Complete process drift : Not found")
        void givenNonExistentId_whenCompleteProcessDrift_thenThrowsResourceNotFoundException() {
            // Given
            String driftId = "non-existent-id";
            LocalDateTime endTime = LocalDateTime.now();
            when(maintenanceDataRepository.findById(driftId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.completeProcessDrift(driftId, endTime))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Process drift with ID = 'non-existent-id' not found in PKB");

            verify(maintenanceDataRepository).findById(driftId);
            verify(maintenanceDataRepository, never()).save(any(MaintenanceData.class));
        }

        @Test
        @DisplayName("Retrieve paginated uncompleted process drifts : Success")
        void givenValidPageable_whenRetrievePaginatedUncompletedProcessDrifts_thenReturnsPagedResults() {
            // Given
            Pageable pageable = Pageable.ofSize(10);
            List<MaintenanceData> entities = Arrays.asList(
                    createMaintenanceData("1", "Stage1", "Cell1", "Component1", "2024-01-15T10:30:00"),
                    createMaintenanceData("2", "Stage2", "Cell2", "Component2", "2024-01-16T11:30:00")
            );

            SearchHits<MaintenanceData> mockSearchHits = createMockSearchHits(entities);
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(mockSearchHits);
            when(mockSearchHits.getTotalHits()).thenReturn(2L);

            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            // When
            Page<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrievePaginatedUncompletedProcessDrifts(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2L);
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, times(2)).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }

        @Test
        @DisplayName("Retrieve paginated uncompleted process drifts : Empty result")
        void givenNoUncompletedDrifts_whenRetrievePaginatedUncompletedProcessDrifts_thenReturnsEmptyPage() {
            // Given
            Pageable pageable = Pageable.ofSize(10);
            SearchHits<MaintenanceData> emptySearchHits = createMockSearchHits(Collections.emptyList());
            when(elasticsearchOperations.search(any(CriteriaQuery.class), eq(MaintenanceData.class)))
                    .thenReturn(emptySearchHits);
            when(emptySearchHits.getTotalHits()).thenReturn(0L);

            // When
            Page<MaintenanceDataDto> result = predictiveMaintenanceService
                    .retrievePaginatedUncompletedProcessDrifts(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0L);
            verify(elasticsearchOperations).search(any(CriteriaQuery.class), eq(MaintenanceData.class));
            verify(modelMapper, never()).map(any(MaintenanceData.class), eq(MaintenanceDataDto.class));
        }
    }

    @Nested
    @DisplayName("Invoke and Register Threshold-Based Predictive Maintenance")
    class InvokeAndRegisterThresholdBasedPredictiveMaintenance {

        @Test
        @DisplayName("Invoke and register threshold maintenance : Success")
        void givenValidInput_whenInvokeAndRegisterThresholdMaintenance_thenReturnsResultAndRegistersTask() throws JsonProcessingException {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .frequencyType(gr.atc.modapto.enums.FrequencyType.HOURS)
                    .frequencyValue(24)
                    .build();

            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            ResponseEntity<DtResponseDto> mockResponse = new ResponseEntity<>(new DtResponseDto(), HttpStatus.OK);
            when(smartServicesInvocationService.invokeSmartService(anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenReturn(mockResponse);

            SewThresholdBasedPredictiveMaintenanceOutputDto expectedOutput = SewThresholdBasedPredictiveMaintenanceOutputDto.builder()
                    .id("test-id")
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .build();
            when(thresholdMaintenanceResponseProcessor.processResponse(any(), anyString(), anyString()))
                    .thenReturn(expectedOutput);

            when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");

            // When
            SewThresholdBasedPredictiveMaintenanceOutputDto result = predictiveMaintenanceService
                    .invokeAndRegisterThresholdBasedPredictiveMaintenance(inputData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo("TEST_MODULE");
            assertThat(result.getSmartServiceId()).isEqualTo("THRESHOLD_SERVICE");
            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService).invokeSmartService(eq("THRESHOLD_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), any(ModaptoHeader.class));
            verify(thresholdMaintenanceResponseProcessor).processResponse(mockResponse, "TEST_MODULE", "THRESHOLD_SERVICE");
            verify(eventPublisher).publishEvent(any());
            
            // Verify that events were cleared from input data
            assertThat(inputData.getEvents()).isNull();
        }

        @Test
        @DisplayName("Invoke and register threshold maintenance : Service invocation failure")
        void givenServiceInvocationFailure_whenInvokeAndRegisterThresholdMaintenance_thenThrowsExceptionWithoutRegistering() {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .frequencyType(gr.atc.modapto.enums.FrequencyType.HOURS)
                    .frequencyValue(24)
                    .build();

            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            when(smartServicesInvocationService.invokeSmartService(anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenThrow(new RuntimeException("Service invocation failed"));

            try {
                when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");
            } catch (JsonProcessingException e) {
                // This won't happen in this test case
            }

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService
                    .invokeAndRegisterThresholdBasedPredictiveMaintenance(inputData))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Service invocation failed");

            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService).invokeSmartService(eq("THRESHOLD_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), any(ModaptoHeader.class));
            verify(thresholdMaintenanceResponseProcessor, never()).processResponse(any(), anyString(), anyString());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Invoke and register threshold maintenance : Processing failure")
        void givenProcessingFailure_whenInvokeAndRegisterThresholdMaintenance_thenThrowsExceptionWithoutRegistering() {
            // Given
            SewThresholdBasedMaintenanceInputDataDto inputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                    .moduleId("TEST_MODULE")
                    .smartServiceId("THRESHOLD_SERVICE")
                    .frequencyType(gr.atc.modapto.enums.FrequencyType.HOURS)
                    .frequencyValue(24)
                    .build();

            Page<MaintenanceData> mockPage = new PageImpl<>(sampleEntityList);
            when(maintenanceDataRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
            when(modelMapper.map(any(MaintenanceData.class), eq(MaintenanceDataDto.class)))
                    .thenReturn(sampleDto);

            ResponseEntity<DtResponseDto> mockResponse = new ResponseEntity<>(new DtResponseDto(), HttpStatus.OK);
            when(smartServicesInvocationService.invokeSmartService(anyString(), anyString(), any(DtInputDto.class), any(ModaptoHeader.class)))
                    .thenReturn(mockResponse);

            when(thresholdMaintenanceResponseProcessor.processResponse(any(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Processing failed"));

            try {
                when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");
            } catch (JsonProcessingException e) {
                // This won't happen in this test case
            }

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService
                    .invokeAndRegisterThresholdBasedPredictiveMaintenance(inputData))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Processing failed");

            verify(maintenanceDataRepository).findAll(any(Pageable.class));
            verify(smartServicesInvocationService).invokeSmartService(eq("THRESHOLD_SERVICE"), eq("TEST_MODULE"), any(DtInputDto.class), any(ModaptoHeader.class));
            verify(thresholdMaintenanceResponseProcessor).processResponse(mockResponse, "TEST_MODULE", "THRESHOLD_SERVICE");
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Store Components List Data")
    class StoreComponentsListData {

        @Test
        @DisplayName("Store components list : Success")
        void givenValidComponentsList_whenStoreComponentsListData_thenStoresSuccessfully() {
            // Given
            List<SewComponentInfoDto> componentsList = Arrays.asList(
                    SewComponentInfoDto.builder()
                            .stage("Stage1")
                            .cell("Cell1")
                            .module("Module1")
                            .moduleId("MOD1")
                            .build(),
                    SewComponentInfoDto.builder()
                            .stage("Stage2")
                            .cell("Cell2")
                            .module("Module2")
                            .moduleId("MOD2")
                            .build()
            );

            SewComponentInfo mappedComponent = new SewComponentInfo();
            mappedComponent.setStage("Stage1");
            mappedComponent.setCell("Cell1");
            mappedComponent.setModule("Module1");
            mappedComponent.setModuleId("MOD1");

            when(modelMapper.map(any(SewComponentInfoDto.class), eq(SewComponentInfo.class)))
                    .thenReturn(mappedComponent);
            when(componentInfoRepository.saveAll(anyList()))
                    .thenReturn(Arrays.asList(mappedComponent));

            // When
            predictiveMaintenanceService.storeComponentsListData(componentsList);

            // Then
            verify(componentInfoRepository).deleteAll();
            verify(modelMapper, times(2)).map(any(SewComponentInfoDto.class), eq(SewComponentInfo.class));
            verify(componentInfoRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Store components list : Empty list")
        void givenEmptyComponentsList_whenStoreComponentsListData_thenHandlesGracefully() {
            // Given
            List<SewComponentInfoDto> emptyList = Collections.emptyList();
            when(componentInfoRepository.saveAll(anyList()))
                    .thenReturn(Collections.emptyList());

            // When
            predictiveMaintenanceService.storeComponentsListData(emptyList);

            // Then
            verify(componentInfoRepository).deleteAll();
            verify(modelMapper, never()).map(any(SewComponentInfoDto.class), eq(SewComponentInfo.class));
            verify(componentInfoRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Store components list : Mapping exception")
        void givenMappingError_whenStoreComponentsListData_thenThrowsModelMappingException() {
            // Given
            List<SewComponentInfoDto> componentsList = Arrays.asList(
                    SewComponentInfoDto.builder()
                            .stage("Stage1")
                            .cell("Cell1")
                            .module("Module1")
                            .moduleId("MOD1")
                            .build()
            );

            when(modelMapper.map(any(SewComponentInfoDto.class), eq(SewComponentInfo.class)))
                    .thenThrow(new MappingException(List.of(new ErrorMessage("Mapping error"))));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService.storeComponentsListData(componentsList))
                    .isInstanceOf(ModelMappingException.class)
                    .hasMessageContaining("Unable to parse DTO SewComponentInfo to Model");

            verify(componentInfoRepository).deleteAll();
            verify(modelMapper).map(any(SewComponentInfoDto.class), eq(SewComponentInfo.class));
            verify(componentInfoRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("Retrieve Component List Given Filter Attributes")
    class RetrieveComponentListGivenFilterAttributes {

        @Test
        @DisplayName("Retrieve component list : Success")
        void givenValidFilterAttributes_whenRetrieveComponentList_thenReturnsFilteredComponents() {
            // Given
            String stage = "Stage1";
            String cell = "Cell1";
            String module = "Module1";
            String moduleId = "MOD1";

            List<SewComponentInfo> entities = Arrays.asList(
                    createSewComponentInfo("1", stage, cell, module, moduleId),
                    createSewComponentInfo("2", stage, cell, module, moduleId)
            );

            SewComponentInfoDto expectedDto = SewComponentInfoDto.builder()
                    .stage(stage)
                    .cell(cell)
                    .module(module)
                    .moduleId(moduleId)
                    .build();

            when(componentInfoRepository.findByStageAndCellAndModuleAndModuleId(stage, cell, module, moduleId))
                    .thenReturn(entities);
            when(modelMapper.map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class)))
                    .thenReturn(expectedDto);

            // When
            List<SewComponentInfoDto> result = predictiveMaintenanceService
                    .retrieveComponentListGivenFilterAttributes(stage, cell, module, moduleId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStage()).isEqualTo(stage);
            assertThat(result.get(0).getCell()).isEqualTo(cell);
            assertThat(result.get(0).getModule()).isEqualTo(module);
            assertThat(result.get(0).getModuleId()).isEqualTo(moduleId);
            verify(componentInfoRepository).findByStageAndCellAndModuleAndModuleId(stage, cell, module, moduleId);
            verify(modelMapper, times(2)).map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class));
        }

        @Test
        @DisplayName("Retrieve component list : Empty result")
        void givenNoMatchingComponents_whenRetrieveComponentList_thenReturnsEmptyList() {
            // Given
            String stage = "NonExistentStage";
            String cell = "NonExistentCell";
            String module = "NonExistentModule";
            String moduleId = "NON_EXISTENT";

            when(componentInfoRepository.findByStageAndCellAndModuleAndModuleId(stage, cell, module, moduleId))
                    .thenReturn(Collections.emptyList());

            // When
            List<SewComponentInfoDto> result = predictiveMaintenanceService
                    .retrieveComponentListGivenFilterAttributes(stage, cell, module, moduleId);

            // Then
            assertThat(result).isEmpty();
            verify(componentInfoRepository).findByStageAndCellAndModuleAndModuleId(stage, cell, module, moduleId);
            verify(modelMapper, never()).map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class));
        }

        @Test
        @DisplayName("Retrieve component list : Mapping exception")
        void givenMappingError_whenRetrieveComponentList_thenThrowsModelMappingException() {
            // Given
            String stage = "Stage1";
            String cell = "Cell1";
            String module = "Module1";
            String moduleId = "MOD1";

            List<SewComponentInfo> entities = Arrays.asList(
                    createSewComponentInfo("1", stage, cell, module, moduleId)
            );

            when(componentInfoRepository.findByStageAndCellAndModuleAndModuleId(stage, cell, module, moduleId))
                    .thenReturn(entities);
            when(modelMapper.map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class)))
                    .thenThrow(new MappingException(List.of(new ErrorMessage("Component mapping error"))));

            // When & Then
            assertThatThrownBy(() -> predictiveMaintenanceService
                    .retrieveComponentListGivenFilterAttributes(stage, cell, module, moduleId))
                    .isInstanceOf(ModelMappingException.class)
                    .hasMessageContaining("Unable to parse DTO SewComponentInfo to Model or vice-versa");

            verify(componentInfoRepository).findByStageAndCellAndModuleAndModuleId(stage, cell, module, moduleId);
            verify(modelMapper).map(any(SewComponentInfo.class), eq(SewComponentInfoDto.class));
        }
    }

    // Helper methods

    private MaintenanceData createMaintenanceData(String id, String stage, String cell, String component, String tsRequestCreation) {
        MaintenanceData data = new MaintenanceData();
        data.setId(id);
        data.setStage(stage);
        data.setCell(cell);
        data.setComponent(component);
        data.setTsRequestCreation(LocalDateTime.parse(tsRequestCreation));
        return data;
    }

    private List<MaintenanceDataDto> createLargeDataset(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> MaintenanceDataDto.builder()
                        .stage("Stage" + i)
                        .cell("Cell" + i)
                        .component("Component" + i)
                        .tsRequestCreation(LocalDateTime.parse("2024-01-15T10:30:00", DateTimeFormatter.ISO_DATE_TIME))
                        .build())
                .toList();
    }

    private List<MaintenanceData> createLargeMaintenanceDataset() {
        return java.util.stream.IntStream.range(0, 5000)
                .mapToObj(i -> createMaintenanceData(
                        String.valueOf(i),
                        "Stage" + i,
                        "Cell" + i,
                        "Component" + i,
                        "2024-01-15T10:30:00"))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private SearchHits<MaintenanceData> createMockSearchHits(List<MaintenanceData> data) {
        SearchHits<MaintenanceData> searchHits = mock(SearchHits.class);
        List<SearchHit<MaintenanceData>> searchHitList = data.stream()
                .map(item -> {
                    SearchHit<MaintenanceData> searchHit = mock(SearchHit.class);
                    when(searchHit.getContent()).thenReturn(item);
                    return searchHit;
                })
                .toList();
        
        when(searchHits.getSearchHits()).thenReturn(searchHitList);
        return searchHits;
    }

    private SewComponentInfo createSewComponentInfo(String id, String stage, String cell, String module, String moduleId) {
        SewComponentInfo componentInfo = new SewComponentInfo();
        componentInfo.setId(id);
        componentInfo.setStage(stage);
        componentInfo.setCell(cell);
        componentInfo.setModule(module);
        componentInfo.setModuleId(moduleId);
        return componentInfo;
    }
}
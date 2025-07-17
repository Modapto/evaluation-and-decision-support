package gr.atc.modapto.service;

import gr.atc.modapto.dto.files.MaintenanceDataDto;
import gr.atc.modapto.model.MaintenanceData;
import gr.atc.modapto.repository.MaintenanceDataRepository;
import gr.atc.modapto.exception.CustomExceptions.FileHandlingException;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PredictiveMaintenanceService Unit Tests")
class PredictiveMaintenanceServiceTests {

    @Mock
    private MaintenanceDataRepository maintenanceDataRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private MultipartFile multipartFile;

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
                .tsRequestCreation("2024-01-15 10:30:00")
                .build();

        sampleEntity = new MaintenanceData();
        sampleEntity.setId("1");
        sampleEntity.setStage("TestStage");
        sampleEntity.setCell("TestCell");
        sampleEntity.setComponent("TestComponent");
        sampleEntity.setFailureType("TestFailureType");
        sampleEntity.setTsRequestCreation("2024-01-15 10:30:00");

        sampleDtoList = Arrays.asList(
                MaintenanceDataDto.builder()
                        .stage("Stage1")
                        .cell("Cell1")
                        .component("Component1")
                        .tsRequestCreation("2024-01-15 10:30:00")
                        .build(),
                MaintenanceDataDto.builder()
                        .stage("Stage2")
                        .cell("Cell2")
                        .component("Component2")
                        .tsRequestCreation("2024-01-16 11:30:00")
                        .build()
        );

        sampleEntityList = Arrays.asList(
                createMaintenanceData("1", "Stage1", "Cell1", "Component1", "2024-01-15 10:30:00"),
                createMaintenanceData("2", "Stage2", "Cell2", "Component2", "2024-01-16 11:30:00")
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
            List<MaintenanceData> largeDataset = createLargeMaintenanceDataset(5000);
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

    // Helper methods

    private MaintenanceData createMaintenanceData(String id, String stage, String cell, String component, String tsRequestCreation) {
        MaintenanceData data = new MaintenanceData();
        data.setId(id);
        data.setStage(stage);
        data.setCell(cell);
        data.setComponent(component);
        data.setTsRequestCreation(tsRequestCreation);
        return data;
    }

    private List<MaintenanceDataDto> createLargeDataset(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> MaintenanceDataDto.builder()
                        .stage("Stage" + i)
                        .cell("Cell" + i)
                        .component("Component" + i)
                        .tsRequestCreation("2024-01-15 10:30:00")
                        .build())
                .toList();
    }

    private List<MaintenanceData> createLargeMaintenanceDataset(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> createMaintenanceData(
                        String.valueOf(i),
                        "Stage" + i,
                        "Cell" + i,
                        "Component" + i,
                        "2024-01-15 10:30:00"))
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
}
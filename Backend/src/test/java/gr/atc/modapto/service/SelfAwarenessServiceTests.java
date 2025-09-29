package gr.atc.modapto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessRealTimeMonitoringInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessRealTimeMonitoringResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessMonitoringKpisResults;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessRealTimeMonitoringResults;
import gr.atc.modapto.model.sew.SewMonitorKpisComponents;
import gr.atc.modapto.repository.SewMonitorKpisComponentsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessMonitoringKpisResultsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessRealTimeMonitoringResultsRepository;
import gr.atc.modapto.service.processors.NoOpResponseProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SelfAwarenessService Tests")
class SelfAwarenessServiceTests {

    @Mock
    private SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository;

    @Mock
    private SewSelfAwarenessRealTimeMonitoringResultsRepository sewSelfAwarenessRealTimeMonitoringResultsRepository;

    @Mock
    private SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @Mock
    private ExceptionHandlerService exceptionHandler;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NoOpResponseProcessor noOpResponseProcessor;

    @InjectMocks
    private SewSelfAwarenessService selfAwarenessService;

    private SewSelfAwarenessMonitoringKpisInputDto monitoringKpisInput;
    private SewSelfAwarenessRealTimeMonitoringInputDto realTimeMonitoringInput;
    private SewMonitorKpisComponentsDto componentsDto;
    private SewMonitorKpisComponents componentsEntity;
    private SewSelfAwarenessMonitoringKpisResults monitoringKpisEntity;
    private SewSelfAwarenessMonitoringKpisResultsDto monitoringKpisDto;
    private SewSelfAwarenessRealTimeMonitoringResults realTimeMonitoringEntity;
    private SewSelfAwarenessRealTimeMonitoringResultsDto realTimeMonitoringDto;

    @BeforeEach
    void setUp() {
        monitoringKpisInput = SewSelfAwarenessMonitoringKpisInputDto.builder()
                .smartServiceId("SA1_SERVICE_ID")
                .moduleId("TEST_MODULE")
                .startDate("2025-01-01T00:00:00")
                .endDate("2025-01-31T23:59:59")
                .components(new ArrayList<>())
                .build();

        realTimeMonitoringInput = SewSelfAwarenessRealTimeMonitoringInputDto.builder()
                .smartServiceId("SA2_SERVICE_ID")
                .moduleId("TEST_MODULE")
                .components(new ArrayList<>())
                .build();

        componentsDto = SewMonitorKpisComponentsDto.builder()
                .id("TEST_ID")
                .moduleId("TEST_MODULE")
                .timestampCreated(LocalDateTime.now())
                .components(new ArrayList<>())
                .build();

        componentsEntity = SewMonitorKpisComponents.builder()
                .id("TEST_ID")
                .moduleId("TEST_MODULE")
                .timestampCreated(LocalDateTime.now())
                .components(new ArrayList<>())
                .build();

        monitoringKpisEntity = SewSelfAwarenessMonitoringKpisResults.builder()
                .id("RESULT_ID")
                .moduleId("TEST_MODULE")
                .timestamp(LocalDateTime.now())
                .smartServiceId("SA1_SERVICE_ID")
                .ligne("LINE_001")
                .component("COMPONENT_001")
                .variable("VARIABLE_001")
                .startingDate("2025-01-01T00:00:00")
                .endingDate("2025-01-31T23:59:59")
                .dataSource("TEST_SOURCE")
                .bucket("TEST_BUCKET")
                .data(List.of(10.5, 20.3, 15.7))
                .build();

        monitoringKpisDto = SewSelfAwarenessMonitoringKpisResultsDto.builder()
                .id("RESULT_ID")
                .moduleId("TEST_MODULE")
                .timestamp(LocalDateTime.now())
                .smartServiceId("SA1_SERVICE_ID")
                .ligne("LINE_001")
                .component("COMPONENT_001")
                .variable("VARIABLE_001")
                .startingDate("2025-01-01T00:00:00")
                .endingDate("2025-01-31T23:59:59")
                .dataSource("TEST_SOURCE")
                .bucket("TEST_BUCKET")
                .data(List.of(10.5, 20.3, 15.7))
                .build();

        realTimeMonitoringEntity = SewSelfAwarenessRealTimeMonitoringResults.builder()
                .id("RT_RESULT_ID")
                .moduleId("TEST_MODULE")
                .timestamp(LocalDateTime.now())
                .smartServiceId("SA2_SERVICE_ID")
                .ligne("LINE_001")
                .component("COMPONENT_001")
                .variable("VARIABLE_001")
                .startingDate("2025-01-01T00:00:00")
                .endingDate("2025-01-31T23:59:59")
                .dataSource("TEST_SOURCE")
                .bucket("TEST_BUCKET")
                .data(List.of(10.5, 20.3, 15.7))
                .build();

        realTimeMonitoringDto = SewSelfAwarenessRealTimeMonitoringResultsDto.builder()
                .id("RT_RESULT_ID")
                .moduleId("TEST_MODULE")
                .timestamp(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
                .smartServiceId("SA2_SERVICE_ID")
                .ligne("LINE_001")
                .component("COMPONENT_001")
                .variable("VARIABLE_001")
                .startingDate("2025-01-01T00:00:00")
                .endingDate("2025-01-31T23:59:59")
                .dataSource("TEST_SOURCE")
                .bucket("TEST_BUCKET")
                .data(List.of(10.5, 20.3, 15.7))
                .build();
    }

    @Nested
    @DisplayName("When invoking Self-Awareness algorithms")
    class AlgorithmInvocation {

        @Test
        @DisplayName("Invoke Self-Awareness Monitoring KPIs algorithm : Success")
        void givenValidInput_whenInvokeMonitoringKpisAlgorithm_thenInvokeSuccessfully() throws JsonProcessingException {
            lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq("TEST_MODULE"))).thenReturn(Optional.of(componentsEntity));
            lenient().when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });

            assertDoesNotThrow(() -> selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(monitoringKpisInput));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq("TEST_MODULE"));
            verify(smartServicesInvocationService, times(1)).formulateAndImplementSmartServiceRequest(
                    eq(monitoringKpisInput),
                    eq(null),
                    eq("Self-Awareness Monitoring KPIs")
            );
        }

        @Test
        @DisplayName("Invoke Self-Awareness Real-Time Monitoring algorithm : Success")
        void givenValidInput_whenInvokeRealTimeMonitoringAlgorithm_thenInvokeSuccessfully() throws JsonProcessingException {
            lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq("TEST_MODULE"))).thenReturn(Optional.of(componentsEntity));
            lenient().when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });

            assertDoesNotThrow(() -> selfAwarenessService.invokeSelfAwarenessRealTimeMonitoringAlgorithm(realTimeMonitoringInput));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq("TEST_MODULE"));
            verify(smartServicesInvocationService, times(1)).formulateAndImplementSmartServiceRequest(
                    eq(realTimeMonitoringInput),
                    eq(null),
                    eq("Self-Awareness Real-Time Monitoring")
            );
        }

        @Test
        @DisplayName("Invoke algorithm when components not found : Throw exception")
        void givenModuleWithoutComponents_whenInvokeAlgorithm_thenThrowException() {
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq("TEST_MODULE"))).thenReturn(Optional.empty());
            lenient().when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });

            assertThrows(CustomExceptions.SmartServiceInvocationException.class, () ->
                selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(monitoringKpisInput));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq("TEST_MODULE"));
            verify(smartServicesInvocationService, never()).formulateAndImplementSmartServiceRequest(any(), any(), any());
        }

        @Test
        @DisplayName("Handle SmartServicesInvocationService failure during algorithm invocation")
        void givenSmartServiceInvocationFailure_whenInvokeAlgorithm_thenPropagatesException() throws Exception {
            lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq("TEST_MODULE"))).thenReturn(Optional.of(componentsEntity));
            lenient().when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            doThrow(new RuntimeException("Smart service invocation failed"))
                    .when(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(any(), any(), any());

            assertThrows(RuntimeException.class, () ->
                selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(monitoringKpisInput));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq("TEST_MODULE"));
            verify(smartServicesInvocationService, times(1)).formulateAndImplementSmartServiceRequest(
                    eq(monitoringKpisInput),
                    eq(null),
                    eq("Self-Awareness Monitoring KPIs")
            );
        }

        @Test
        @DisplayName("Verify correct algorithm name passed for real-time monitoring")
        void givenRealTimeMonitoringInput_whenInvokeAlgorithm_thenPassesCorrectAlgorithmName() throws Exception {
            lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"mockJson\":\"data\"}");
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq("TEST_MODULE"))).thenReturn(Optional.of(componentsEntity));
            lenient().when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });

            assertDoesNotThrow(() -> selfAwarenessService.invokeSelfAwarenessRealTimeMonitoringAlgorithm(realTimeMonitoringInput));

            verify(smartServicesInvocationService, times(1)).formulateAndImplementSmartServiceRequest(
                    eq(realTimeMonitoringInput),
                    eq(null),
                    eq("Self-Awareness Real-Time Monitoring")
            );
        }
    }

    @Nested
    @DisplayName("When retrieving Monitoring KPIs results")
    class MonitoringKpisResultsRetrieval {

        @Test
        @DisplayName("Retrieve latest Monitoring KPIs results : Success")
        void whenRetrieveLatestMonitoringKpisResults_thenReturnResults() {
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findFirstByOrderByTimestampDesc()).thenReturn(Optional.of(monitoringKpisEntity));
            when(modelMapper.map(eq(monitoringKpisEntity), eq(SewSelfAwarenessMonitoringKpisResultsDto.class))).thenReturn(monitoringKpisDto);

            SewSelfAwarenessMonitoringKpisResultsDto result = selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults();

            assertNotNull(result);
            assertEquals("RESULT_ID", result.getId());
            assertEquals("TEST_MODULE", result.getModuleId());
            verify(sewSelfAwarenessMonitoringKpisResultsRepository, times(1)).findFirstByOrderByTimestampDesc();
            verify(modelMapper, times(1)).map(eq(monitoringKpisEntity), eq(SewSelfAwarenessMonitoringKpisResultsDto.class));
        }

        @Test
        @DisplayName("Retrieve latest Monitoring KPIs results by module ID : Success")
        void givenModuleId_whenRetrieveLatestMonitoringKpisResultsByModuleId_thenReturnResults() {
            String moduleId = "TEST_MODULE";
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findFirstByModuleIdOrderByTimestampDesc(eq(moduleId))).thenReturn(Optional.of(monitoringKpisEntity));
            when(modelMapper.map(eq(monitoringKpisEntity), eq(SewSelfAwarenessMonitoringKpisResultsDto.class))).thenReturn(monitoringKpisDto);

            SewSelfAwarenessMonitoringKpisResultsDto result = selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);

            assertNotNull(result);
            assertEquals("RESULT_ID", result.getId());
            assertEquals("TEST_MODULE", result.getModuleId());
            verify(sewSelfAwarenessMonitoringKpisResultsRepository, times(1)).findFirstByModuleIdOrderByTimestampDesc(eq(moduleId));
        }

        @Test
        @DisplayName("Retrieve all Monitoring KPIs results : Success")
        void whenRetrieveAllMonitoringKpisResults_thenReturnResultsList() {
            Page<SewSelfAwarenessMonitoringKpisResults> page = new PageImpl<>(List.of(monitoringKpisEntity));
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(modelMapper.map(eq(monitoringKpisEntity), eq(SewSelfAwarenessMonitoringKpisResultsDto.class))).thenReturn(monitoringKpisDto);

            List<SewSelfAwarenessMonitoringKpisResultsDto> results = selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults();

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("RESULT_ID", results.get(0).getId());
            verify(sewSelfAwarenessMonitoringKpisResultsRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Retrieve all Monitoring KPIs results by module ID : Success")
        void givenModuleId_whenRetrieveAllMonitoringKpisResultsByModuleId_thenReturnResultsList() {
            String moduleId = "TEST_MODULE";
            Page<SewSelfAwarenessMonitoringKpisResults> page = new PageImpl<>(List.of(monitoringKpisEntity));
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findByModuleId(eq(moduleId), any(Pageable.class))).thenReturn(page);
            when(modelMapper.map(eq(monitoringKpisEntity), eq(SewSelfAwarenessMonitoringKpisResultsDto.class))).thenReturn(monitoringKpisDto);

            List<SewSelfAwarenessMonitoringKpisResultsDto> results = selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("RESULT_ID", results.get(0).getId());
            verify(sewSelfAwarenessMonitoringKpisResultsRepository, times(1)).findByModuleId(eq(moduleId), any(Pageable.class));
        }

        @Test
        @DisplayName("Retrieve latest results when none found : Throw exception")
        void whenRetrieveLatestResults_andNoneFound_thenThrowException() {
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findFirstByOrderByTimestampDesc()).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults());

            verify(sewSelfAwarenessMonitoringKpisResultsRepository, times(1)).findFirstByOrderByTimestampDesc();
        }

        @Test
        @DisplayName("Retrieve results with empty list : Return empty list")
        void whenRetrieveAllResults_andRepositoryReturnsEmpty_thenReturnEmptyList() {
            Page<SewSelfAwarenessMonitoringKpisResults> emptyPage = new PageImpl<>(Collections.emptyList());
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            List<SewSelfAwarenessMonitoringKpisResultsDto> results = selfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults();

            assertNotNull(results);
            assertTrue(results.isEmpty());
            verify(sewSelfAwarenessMonitoringKpisResultsRepository, times(1)).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When retrieving Real-Time Monitoring results")
    class RealTimeMonitoringResultsRetrieval {

        @Test
        @DisplayName("Retrieve all Real-Time Monitoring results : Success")
        void whenRetrieveAllRealTimeMonitoringResults_thenReturnResultsList() {
            Page<SewSelfAwarenessRealTimeMonitoringResults> page = new PageImpl<>(List.of(realTimeMonitoringEntity));
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessRealTimeMonitoringResultsRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(modelMapper.map(eq(realTimeMonitoringEntity), eq(SewSelfAwarenessRealTimeMonitoringResultsDto.class))).thenReturn(realTimeMonitoringDto);

            List<SewSelfAwarenessRealTimeMonitoringResultsDto> results = selfAwarenessService.retrieveAllSelfAwarenessRealTimeMonitoringResults();

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("RT_RESULT_ID", results.get(0).getId());
            verify(sewSelfAwarenessRealTimeMonitoringResultsRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Retrieve all Real-Time Monitoring results by module ID : Success")
        void givenModuleId_whenRetrieveAllRealTimeMonitoringResultsByModuleId_thenReturnResultsList() {
            String moduleId = "TEST_MODULE";
            Page<SewSelfAwarenessRealTimeMonitoringResults> page = new PageImpl<>(List.of(realTimeMonitoringEntity));
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessRealTimeMonitoringResultsRepository.findByModuleId(eq(moduleId), any(Pageable.class))).thenReturn(page);
            when(modelMapper.map(eq(realTimeMonitoringEntity), eq(SewSelfAwarenessRealTimeMonitoringResultsDto.class))).thenReturn(realTimeMonitoringDto);

            List<SewSelfAwarenessRealTimeMonitoringResultsDto> results = selfAwarenessService.retrieveAllSelfAwarenessRealTimeMonitoringResultsByModuleId(moduleId);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("RT_RESULT_ID", results.get(0).getId());
            verify(sewSelfAwarenessRealTimeMonitoringResultsRepository, times(1)).findByModuleId(eq(moduleId), any(Pageable.class));
        }

        @Test
        @DisplayName("Retrieve Real-Time Monitoring results with empty list : Return empty list")
        void whenRetrieveAllRealTimeResults_andRepositoryReturnsEmpty_thenReturnEmptyList() {
            Page<SewSelfAwarenessRealTimeMonitoringResults> emptyPage = new PageImpl<>(Collections.emptyList());
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessRealTimeMonitoringResultsRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            List<SewSelfAwarenessRealTimeMonitoringResultsDto> results = selfAwarenessService.retrieveAllSelfAwarenessRealTimeMonitoringResults();

            assertNotNull(results);
            assertTrue(results.isEmpty());
            verify(sewSelfAwarenessRealTimeMonitoringResultsRepository, times(1)).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When managing component lists")
    class ComponentListManagement {

        @Test
        @DisplayName("Upload component list for new module : Success")
        void givenNewModuleComponents_whenUploadComponentList_thenSaveSuccessfully() {
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq("TEST_MODULE"))).thenReturn(Optional.empty());
            when(modelMapper.map(eq(componentsDto), eq(SewMonitorKpisComponents.class))).thenReturn(componentsEntity);
            when(sewMonitorKpisComponentsRepository.save(any(SewMonitorKpisComponents.class))).thenReturn(componentsEntity);

            assertDoesNotThrow(() -> selfAwarenessService.uploadModuleComponentsList(componentsDto));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq("TEST_MODULE"));
            verify(sewMonitorKpisComponentsRepository, never()).deleteByModuleId(any());
            verify(sewMonitorKpisComponentsRepository, times(1)).save(any(SewMonitorKpisComponents.class));
        }

        @Test
        @DisplayName("Upload component list for existing module : Replace existing")
        void givenExistingModuleComponents_whenUploadComponentList_thenReplaceExisting() {
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq("TEST_MODULE"))).thenReturn(Optional.of(componentsEntity));
            when(modelMapper.map(eq(componentsDto), eq(SewMonitorKpisComponents.class))).thenReturn(componentsEntity);
            when(sewMonitorKpisComponentsRepository.save(any(SewMonitorKpisComponents.class))).thenReturn(componentsEntity);

            assertDoesNotThrow(() -> selfAwarenessService.uploadModuleComponentsList(componentsDto));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq("TEST_MODULE"));
            verify(sewMonitorKpisComponentsRepository, times(1)).deleteByModuleId(eq("TEST_MODULE"));
            verify(sewMonitorKpisComponentsRepository, times(1)).save(any(SewMonitorKpisComponents.class));
        }

        @Test
        @DisplayName("Retrieve component list by module ID : Success")
        void givenModuleId_whenRetrieveComponentList_thenReturnComponentList() {
            String moduleId = "TEST_MODULE";
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq(moduleId))).thenReturn(Optional.of(componentsEntity));
            when(modelMapper.map(eq(componentsEntity), eq(SewMonitorKpisComponentsDto.class))).thenReturn(componentsDto);

            SewMonitorKpisComponentsDto result = selfAwarenessService.retrieveSelfAwarenessComponentListByModuleId(moduleId);

            assertNotNull(result);
            assertEquals("TEST_ID", result.getId());
            assertEquals("TEST_MODULE", result.getModuleId());
            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq(moduleId));
            verify(modelMapper, times(1)).map(eq(componentsEntity), eq(SewMonitorKpisComponentsDto.class));
        }

        @Test
        @DisplayName("Retrieve component list when not found : Throw exception")
        void givenNonexistentModuleId_whenRetrieveComponentList_thenThrowException() {
            String moduleId = "NONEXISTENT_MODULE";
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq(moduleId))).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                selfAwarenessService.retrieveSelfAwarenessComponentListByModuleId(moduleId));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq(moduleId));
        }

        @Test
        @DisplayName("Delete component list by module ID : Success")
        void givenModuleId_whenDeleteComponentList_thenDeleteSuccessfully() {
            String moduleId = "TEST_MODULE";
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq(moduleId))).thenReturn(Optional.of(componentsEntity));

            assertDoesNotThrow(() -> selfAwarenessService.deleteSelfAwarenessComponentListByModuleId(moduleId));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq(moduleId));
            verify(sewMonitorKpisComponentsRepository, times(1)).deleteByModuleId(eq(moduleId));
        }

        @Test
        @DisplayName("Delete component list when not found : Throw exception")
        void givenNonexistentModuleId_whenDeleteComponentList_thenThrowException() {
            String moduleId = "NONEXISTENT_MODULE";
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewMonitorKpisComponentsRepository.findByModuleId(eq(moduleId))).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                selfAwarenessService.deleteSelfAwarenessComponentListByModuleId(moduleId));

            verify(sewMonitorKpisComponentsRepository, times(1)).findByModuleId(eq(moduleId));
            verify(sewMonitorKpisComponentsRepository, never()).deleteByModuleId(any());
        }
    }

    @Nested
    @DisplayName("When handling edge cases and error scenarios")
    class EdgeCasesAndErrors {

        @Test
        @DisplayName("Handle null input parameters gracefully")
        void givenNullInput_whenInvokeAlgorithm_thenHandleGracefully() {
            assertThrows(CustomExceptions.SmartServiceInvocationException.class, () ->
                selfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(null));
        }

        @Test
        @DisplayName("Handle repository exceptions during retrieval")
        void whenRepositoryThrowsException_thenPropagateException() {
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database error"));

            assertThrows(RuntimeException.class, () ->
                selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults());
        }

        @Test
        @DisplayName("Handle mapping exceptions during entity conversion")
        void whenMappingFails_thenPropagateException() {
            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });
            when(sewSelfAwarenessMonitoringKpisResultsRepository.findFirstByOrderByTimestampDesc()).thenReturn(Optional.of(monitoringKpisEntity));
            when(modelMapper.map(eq(monitoringKpisEntity), eq(SewSelfAwarenessMonitoringKpisResultsDto.class)))
                    .thenThrow(new RuntimeException("Mapping error"));

            assertThrows(RuntimeException.class, () ->
                selfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults());
        }

        @Test
        @DisplayName("Handle component upload with null components list")
        void givenComponentsDataWithNullList_whenUploadComponentList_thenHandleGracefully() {
            SewMonitorKpisComponentsDto invalidDto = SewMonitorKpisComponentsDto.builder()
                    .moduleId("TEST_MODULE")
                    .components(null)
                    .build();

            when(modelMapper.map(invalidDto, SewMonitorKpisComponents.class)).thenReturn(null);

            when(exceptionHandler.handleOperation(any(), any())).thenAnswer(invocation -> {
                return ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            });

            assertThrows(NullPointerException.class, () ->
                selfAwarenessService.uploadModuleComponentsList(invalidDto));
        }
    }
}
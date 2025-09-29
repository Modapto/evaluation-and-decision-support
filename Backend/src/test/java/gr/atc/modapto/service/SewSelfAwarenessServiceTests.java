package gr.atc.modapto.service;

import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessRealTimeMonitoringInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessRealTimeMonitoringResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessMonitoringKpisResults;
import gr.atc.modapto.model.serviceResults.SewSelfAwarenessRealTimeMonitoringResults;
import gr.atc.modapto.model.sew.SewMonitorKpisComponents;
import gr.atc.modapto.repository.SewMonitorKpisComponentsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessMonitoringKpisResultsRepository;
import gr.atc.modapto.repository.SewSelfAwarenessRealTimeMonitoringResultsRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SewSelfAwarenessService Unit Tests")
class SewSelfAwarenessServiceTests {

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

    @InjectMocks
    private SewSelfAwarenessService sewSelfAwarenessService;

    private SewSelfAwarenessMonitoringKpisInputDto sampleInputData;
    private SewSelfAwarenessMonitoringKpisResults sampleEntity;
    private SewSelfAwarenessMonitoringKpisResultsDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleInputData = SewSelfAwarenessMonitoringKpisInputDto.builder()
                .moduleId("sew_module_1")
                .smartServiceId("service_1")
                .startDate("2023-01-01")
                .endDate("2023-01-31")
                .build();

        sampleEntity = new SewSelfAwarenessMonitoringKpisResults();
        sampleEntity.setId("test-id-1");
        sampleEntity.setModuleId("sew_module_1");
        sampleEntity.setTimestamp(LocalDateTime.now());

        sampleDto = SewSelfAwarenessMonitoringKpisResultsDto.builder()
                .id("test-id-1")
                .moduleId("sew_module_1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Invoke Self-Awareness Monitoring KPIs Algorithm")
    class InvokeSelfAwarenessMonitoringKpisAlgorithm {

        @Test
        @DisplayName("Invoke monitoring KPIs : Success")
        void givenValidInput_whenInvokeMonitoringKpis_thenCallsSmartServicesInvocationService() {
            SewMonitorKpisComponents mockComponent = new SewMonitorKpisComponents();
            mockComponent.setModuleId("sew_module_1");
            mockComponent.setComponents(Collections.emptyList());

            when(sewMonitorKpisComponentsRepository.findByModuleId("sew_module_1"))
                    .thenReturn(Optional.of(mockComponent));

            doNothing().when(smartServicesInvocationService)
                    .formulateAndImplementSmartServiceRequest(any(), eq(null), eq("Self-Awareness Monitoring KPIs"));

            sewSelfAwarenessService.invokeSelfAwarenessMonitoringKpisAlgorithm(sampleInputData);

            verify(sewMonitorKpisComponentsRepository).findByModuleId("sew_module_1");
            verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                    any(SewSelfAwarenessMonitoringKpisInputDto.class),
                    eq(null),
                    eq("Self-Awareness Monitoring KPIs")
            );
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Monitoring KPIs Results")
    class RetrieveLatestMonitoringKpisResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingResults_whenRetrieveLatest_thenReturnsLatestResult() {
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults")))
                    .thenReturn(sampleDto);

            SewSelfAwarenessMonitoringKpisResultsDto result = sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResults();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-id-1");
            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResults"));
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Monitoring KPIs Results by Module ID")
    class RetrieveLatestMonitoringKpisResultsByModuleId {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenValidModuleId_whenRetrieveLatestByModule_thenReturnsLatestResult() {
            String moduleId = "sew_module_1";
            when(exceptionHandler.handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId")))
                    .thenReturn(sampleDto);

            SewSelfAwarenessMonitoringKpisResultsDto result = sewSelfAwarenessService.retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(moduleId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("test-id-1");
            verify(exceptionHandler).handleOperation(any(), eq("retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId"));
        }
    }

    @Nested
    @DisplayName("Retrieve All Monitoring KPIs Results")
    class RetrieveAllMonitoringKpisResults {

        @Test
        @DisplayName("Retrieve all results : Success")
        void givenExistingResults_whenRetrieveAll_thenReturnsAllResults() {
            List<SewSelfAwarenessMonitoringKpisResultsDto> expectedResults = Arrays.asList(sampleDto);
            when(exceptionHandler.handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults")))
                    .thenReturn(expectedResults);

            List<SewSelfAwarenessMonitoringKpisResultsDto> result = sewSelfAwarenessService.retrieveAllSelfAwarenessMonitoringKpisResults();

            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            verify(exceptionHandler).handleOperation(any(), eq("retrieveAllSelfAwarenessMonitoringKpisResults"));
        }
    }

    @Nested
    @DisplayName("Upload Module Components List")
    class UploadModuleComponentsList {

        @Test
        @DisplayName("Upload components list : Success")
        void givenValidComponentsData_whenUploadComponentsList_thenCallsExceptionHandler() {
            SewMonitorKpisComponentsDto componentsData = SewMonitorKpisComponentsDto.builder()
                    .moduleId("sew_module_1")
                    .build();

            when(exceptionHandler.handleOperation(any(), eq("uploadModuleComponentsList")))
                    .thenReturn(null);

            sewSelfAwarenessService.uploadModuleComponentsList(componentsData);

            verify(exceptionHandler).handleOperation(any(), eq("uploadModuleComponentsList"));
        }
    }

    @Nested
    @DisplayName("Retrieve Component List by Module ID")
    class RetrieveComponentListByModuleId {

        @Test
        @DisplayName("Retrieve component list : Success")
        void givenValidModuleId_whenRetrieveComponentList_thenReturnsComponentsList() {
            String moduleId = "sew_module_1";
            SewMonitorKpisComponentsDto expectedDto = SewMonitorKpisComponentsDto.builder()
                    .moduleId(moduleId)
                    .build();

            when(exceptionHandler.handleOperation(any(), eq("retrieveSelfAwarenessComponentListByModuleId")))
                    .thenReturn(expectedDto);

            SewMonitorKpisComponentsDto result = sewSelfAwarenessService.retrieveSelfAwarenessComponentListByModuleId(moduleId);

            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo(moduleId);
            verify(exceptionHandler).handleOperation(any(), eq("retrieveSelfAwarenessComponentListByModuleId"));
        }
    }

    @Nested
    @DisplayName("Delete Component List by Module ID")
    class DeleteComponentListByModuleId {

        @Test
        @DisplayName("Delete component list : Success")
        void givenValidModuleId_whenDeleteComponentList_thenCallsExceptionHandler() {
            String moduleId = "sew_module_1";

            when(exceptionHandler.handleOperation(any(), eq("deleteSelfAwarenessComponentListByModuleId")))
                    .thenReturn(null);

            sewSelfAwarenessService.deleteSelfAwarenessComponentListByModuleId(moduleId);

            verify(exceptionHandler).handleOperation(any(), eq("deleteSelfAwarenessComponentListByModuleId"));
        }
    }
}
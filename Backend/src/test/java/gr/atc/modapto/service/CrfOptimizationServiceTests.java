package gr.atc.modapto.service;

import gr.atc.modapto.dto.crf.CrfOptimizationKittingConfigDto;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.model.crf.CrfOptimizationKittingConfig;
import gr.atc.modapto.model.serviceResults.CrfOptimizationResults;
import gr.atc.modapto.repository.CrfOptimizationKittingConfigRepository;
import gr.atc.modapto.repository.CrfOptimizationResultsRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrfOptimizationService Unit Tests")
class CrfOptimizationServiceTests {

    @Mock
    private CrfOptimizationResultsRepository crfOptimizationResultsRepository;

    @Mock
    private CrfOptimizationKittingConfigRepository crfOptimizationKittingConfigRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @Mock
    private ExceptionHandlerService exceptionHandlerService;

    @InjectMocks
    private CrfOptimizationService crfOptimizationService;

    private CrfOptimizationResults sampleEntity;
    private CrfOptimizationResultsDto sampleDto;
    private LocalDateTime sampleTimestamp;

    @BeforeEach
    void setUp() {
        sampleTimestamp = LocalDateTime.of(2025, 7, 17, 10, 30, 0);

        Object optimizationResults = new Object();

        sampleEntity = new CrfOptimizationResults(
                "1", sampleTimestamp, "Optimization completed successfully", "test_module",
                optimizationResults, true, 3000L, 5000L
        );

        sampleDto = createSampleDto();
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results")
    class RetrieveLatestOptimizationResults {

        @Test
        @DisplayName("Retrieve latest results : Success")
        void givenExistingOptimizationResults_whenRetrieveLatestOptimizationResults_thenReturnsLatestResult() {
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            CrfOptimizationResultsDto result = crfOptimizationService.retrieveLatestOptimizationResults();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Optimization completed successfully");
            assertThat(result.getOptimizationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(3000L);
            assertThat(result.getTotalTime()).isEqualTo(5000L);
            assertThat(result.getOptimizationResults()).isNotNull();

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : No results found")
        void givenNoOptimizationResults_whenRetrieveLatestOptimizationResults_thenThrowsResourceNotFoundException() {
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Optimization Results found");

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResults_thenThrowsModelMappingException() {
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Optimization Results to DTO - Error: Mapping error occurred"));

            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Optimization Results to DTO - Error: Mapping error occurred");

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResults_thenThrowsException() {
            when(crfOptimizationResultsRepository.findFirstByOrderByTimestampDesc())
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResults())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(crfOptimizationResultsRepository).findFirstByOrderByTimestampDesc();
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Retrieve Latest Optimization Results by Module ID")
    class RetrieveLatestOptimizationResultsByModuleId {

        @Test
        @DisplayName("Retrieve latest results by module : Success")
        void givenExistingModuleResults_whenRetrieveLatestOptimizationResultsByModuleId_thenReturnsLatestResult() {
            String productionModule = "crf_module_1";
            when(crfOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenReturn(sampleDto);

            CrfOptimizationResultsDto result = crfOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getTimestamp()).isEqualTo(sampleTimestamp);
            assertThat(result.getMessage()).isEqualTo("Optimization completed successfully");
            assertThat(result.getOptimizationRun()).isTrue();
            assertThat(result.getSolutionTime()).isEqualTo(3000L);
            assertThat(result.getTotalTime()).isEqualTo(5000L);
            assertThat(result.getOptimizationResults()).isNotNull();

            verify(crfOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : No results found")
        void givenNoModuleResults_whenRetrieveLatestOptimizationResultsByModuleId_thenThrowsResourceNotFoundException() {
            String productionModule = "non_existing_module";
            when(crfOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule))
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("No CRF Optimization Results for Module: " + productionModule + " found");

            verify(crfOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("Retrieve latest results by module : Mapping exception")
        void givenMappingError_whenRetrieveLatestOptimizationResultsByModuleId_thenThrowsModelMappingException() {
            String productionModule = "crf_module_1";
            when(crfOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenReturn(Optional.of(sampleEntity));

            when(modelMapper.map(sampleEntity, CrfOptimizationResultsDto.class))
                    .thenThrow(new CustomExceptions.ModelMappingException("Unable to parse CRF Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred"));

            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule))
                    .isInstanceOf(CustomExceptions.ModelMappingException.class)
                    .hasMessage("Unable to parse CRF Optimization Results to DTO for Module: " + productionModule + " - Error: Mapping error occurred");

            verify(crfOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper).map(sampleEntity, CrfOptimizationResultsDto.class);
        }

        @Test
        @DisplayName("Retrieve latest results by module : Repository exception")
        void givenRepositoryError_whenRetrieveLatestOptimizationResultsByModuleId_thenThrowsException() {
            String productionModule = "crf_module_1";
            when(crfOptimizationResultsRepository.findFirstByModuleIdOrderByTimestampDesc(productionModule))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> crfOptimizationService.retrieveLatestOptimizationResultsByModuleId(productionModule))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(crfOptimizationResultsRepository).findFirstByModuleIdOrderByTimestampDesc(productionModule);
            verify(modelMapper, never()).map(any(), any());
        }
    }

    @Nested
    @DisplayName("Invoke Optimization of KH Picking Sequence")
    class InvokeOptimizationOfKhPickingSequence {

        @Test
        @DisplayName("Invoke KH picking sequence optimization : Success")
        void givenValidInput_whenInvokeOptimizationOfKhPickingSequence_thenCallsSmartServicesInvocationService() {
            CrfInvocationInputDto inputDto = CrfInvocationInputDto.builder()
                    .moduleId("crf_module_1")
                    .smartServiceId("service_1")
                    .build();

            crfOptimizationService.invokeOptimizationOfKhPickingSequence(inputDto);

            verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                    inputDto,
                    "robot-picking-seq",
                    "CRF KH Picking Sequence Optimization"
            );
        }

        @Test
        @DisplayName("Invoke KH picking sequence optimization : Service exception")
        void givenServiceError_whenInvokeOptimizationOfKhPickingSequence_thenThrowsException() {
            CrfInvocationInputDto inputDto = CrfInvocationInputDto.builder()
                    .moduleId("crf_module_1")
                    .smartServiceId("service_1")
                    .build();

            doThrow(new RuntimeException("Service invocation failed"))
                    .when(smartServicesInvocationService)
                    .formulateAndImplementSmartServiceRequest(any(), any(), any());

            assertThatThrownBy(() -> crfOptimizationService.invokeOptimizationOfKhPickingSequence(inputDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Service invocation failed");

            verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                    inputDto,
                    "robot-picking-seq",
                    "CRF KH Picking Sequence Optimization"
            );
        }
    }

    @Nested
    @DisplayName("Retrieve Optimization Kitting Config")
    class RetrieveOptimizationKittingConfig {

        @Test
        @DisplayName("Retrieve kitting config : Success")
        void givenExistingConfig_whenRetrieveOptimizationKittingConfig_thenReturnsConfig() {
            // Given
            CrfOptimizationKittingConfig configEntity = new CrfOptimizationKittingConfig();
            configEntity.setId("opt-current");
            configEntity.setFilename("kitting-config.json");
            configEntity.setUploadedAt("2025-01-30T14:30:00Z");
            configEntity.setConfigCase("production");

            CrfOptimizationKittingConfigDto configDto = new CrfOptimizationKittingConfigDto();
            configDto.setId("opt-current");
            configDto.setFilename("kitting-config.json");
            configDto.setUploadedAt("2025-01-30T14:30:00Z");
            configDto.setConfigCase("production");

            when(exceptionHandlerService.handleOperation(any(), eq("retrieveOptimizationKittingConfig")))
                    .thenReturn(configDto);

            // When
            CrfOptimizationKittingConfigDto result = crfOptimizationService.retrieveOptimizationKittingConfig();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("opt-current");
            assertThat(result.getFilename()).isEqualTo("kitting-config.json");
            assertThat(result.getConfigCase()).isEqualTo("production");
            verify(exceptionHandlerService).handleOperation(any(), eq("retrieveOptimizationKittingConfig"));
        }

        @Test
        @DisplayName("Retrieve kitting config : Handles exception via exception handler")
        void givenExceptionHandlerError_whenRetrieveOptimizationKittingConfig_thenPropagatesException() {
            // Given
            when(exceptionHandlerService.handleOperation(any(), eq("retrieveOptimizationKittingConfig")))
                    .thenThrow(new CustomExceptions.ResourceNotFoundException("Config not found"));

            // When & Then
            assertThatThrownBy(() -> crfOptimizationService.retrieveOptimizationKittingConfig())
                    .isInstanceOf(CustomExceptions.ResourceNotFoundException.class)
                    .hasMessage("Config not found");

            verify(exceptionHandlerService).handleOperation(any(), eq("retrieveOptimizationKittingConfig"));
        }
    }

    /*
     * Helper Methods
     */
    private CrfOptimizationResultsDto createSampleDto() {
        Object optimizationResults = new Object();

        return CrfOptimizationResultsDto.builder()
                .id("1")
                .timestamp(sampleTimestamp)
                .message("Optimization completed successfully")
                .optimizationResults(optimizationResults)
                .optimizationRun(true)
                .solutionTime(3000L)
                .totalTime(5000L)
                .build();
    }
}
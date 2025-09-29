package gr.atc.modapto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.dto.crf.CrfSelfAwarenessParametersDto;
import gr.atc.modapto.dto.serviceInvocations.CrfSelfAwarenessInputDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfKhEventNotificationDto;
import gr.atc.modapto.kafka.KafkaMessageProducer;
import gr.atc.modapto.model.serviceResults.CrfKitHolderEvent;
import gr.atc.modapto.repository.CrfKitHolderEventRepository;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrfSelfAwarenessService Unit Tests")
class CrfSelfAwarenessServiceTests {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SmartServicesInvocationService smartServicesInvocationService;

    @Mock
    private ExceptionHandlerService exceptionHandler;

    @Mock
    private CrfKitHolderEventRepository crfKitHolderEventRepository;

    @Mock
    private KafkaMessageProducer kafkaMessageProducer;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CrfSelfAwarenessService crfSelfAwarenessService;

    private CrfSelfAwarenessParametersDto sampleParameters;
    private MockMultipartFile sampleCsvFile;

    @BeforeEach
    void setUp() {
        sampleParameters = CrfSelfAwarenessParametersDto.builder()
                .moduleId("crf_module_1")
                .smartServiceId("service_1")
                .threshold(16.0)
                .intervalMinutes(30)
                .modelPath("quadratic_model.json")
                .build();

        String csvContent = """
                eventType;rfidStation;timestamp;khType;khId
                1;3;1672531200;2;12345
                2;5;1672531260;1;67890
                """;
        sampleCsvFile = new MockMultipartFile(
                "events", "events.csv", "text/csv", csvContent.getBytes()
        );
    }

    @Nested
    @DisplayName("Invoke KH Self Awareness")
    class InvokeKhSelfAwareness {

        @Test
        @DisplayName("Invoke KH self awareness : Success")
        void givenValidFileAndParameters_whenInvokeKhSelfAwareness_thenCallsSmartServicesInvocationService() throws Exception {
            List<CrfKitHolderEventDto> mockEventData = Arrays.asList(
                    new CrfKitHolderEventDto(),
                    new CrfKitHolderEventDto()
            );

            try (MockedStatic<gr.atc.modapto.util.CsvFileUtils> csvUtilsMock = mockStatic(gr.atc.modapto.util.CsvFileUtils.class)) {
                csvUtilsMock.when(() -> gr.atc.modapto.util.CsvFileUtils.extractKhEventsDataFromCSV(sampleCsvFile))
                        .thenReturn(mockEventData);

                crfSelfAwarenessService.invokeKhSelfAwareness(sampleCsvFile, sampleParameters);

                verify(smartServicesInvocationService).formulateAndImplementSmartServiceRequest(
                        any(CrfSelfAwarenessInputDto.class),
                        eq(null),
                        eq("CRF Self Awareness Wear Monitoring")
                );
            }
        }
    }

    @Nested
    @DisplayName("Retrieve Paginated KH Event Results")
    class RetrievePaginatedKhEventResults {

        @Test
        @DisplayName("Retrieve paginated KH events : Success")
        void givenValidPageable_whenRetrievePaginatedKhEventResults_thenReturnsPagedResults() {
            List<CrfKitHolderEvent> entities = Arrays.asList(
                    new CrfKitHolderEvent(),
                    new CrfKitHolderEvent()
            );
            Page<CrfKitHolderEvent> entityPage = new PageImpl<>(entities);
            List<CrfKitHolderEventDto> dtos = Arrays.asList(
                    new CrfKitHolderEventDto(),
                    new CrfKitHolderEventDto()
            );
            Page<CrfKitHolderEventDto> expectedPage = new PageImpl<>(dtos);

            when(exceptionHandler.handleOperation(any(), eq("retrievePaginatedKhEventResultsByModuleId")))
                    .thenReturn(expectedPage);

            Page<CrfKitHolderEventDto> result = crfSelfAwarenessService.retrievePaginatedKhEventResultsPaginated(Pageable.unpaged());

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(exceptionHandler).handleOperation(any(), eq("retrievePaginatedKhEventResultsByModuleId"));
        }
    }

    @Nested
    @DisplayName("Register Kit Holder Event")
    class RegisterKitHolderEvent {

        @Test
        @DisplayName("Register kit holder event : Success")
        void givenValidEvent_whenRegisterKitHolderEvent_thenCallsExceptionHandler() {
            CrfKhEventNotificationDto event = new CrfKhEventNotificationDto();

            when(exceptionHandler.handleOperation(any(), eq("registerKitHolderEvent")))
                    .thenReturn(null);

            crfSelfAwarenessService.registerKitHolderEvent(event);

            verify(exceptionHandler).handleOperation(any(), eq("registerKitHolderEvent"));
        }
    }
}
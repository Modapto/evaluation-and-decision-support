package gr.atc.modapto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.dto.crf.CrfSelfAwarenessParametersDto;
import gr.atc.modapto.dto.serviceInvocations.CrfSelfAwarenessInputDto;
import gr.atc.modapto.enums.KafkaTopics;
import gr.atc.modapto.enums.MessagePriority;
import gr.atc.modapto.kafka.KafkaMessageProducer;
import gr.atc.modapto.model.serviceResults.CrfKitHolderEvent;
import gr.atc.modapto.repository.CrfKitHolderEventRepository;
import gr.atc.modapto.service.interfaces.ICrfSelfAwarenessService;
import gr.atc.modapto.util.CsvFileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CrfSelfAwarenessService implements ICrfSelfAwarenessService {

    private final ObjectMapper objectMapper;

    private final SmartServicesInvocationService smartServicesInvocationService;

    private final ExceptionHandlerService exceptionHandler;

    private final CrfKitHolderEventRepository crfKitHolderEventRepository;

    private final KafkaMessageProducer kafkaMessageProducer;

    private final ModelMapper modelMapper;

    public CrfSelfAwarenessService(KafkaMessageProducer kafkaMessageProducer, ObjectMapper objectMapper, SmartServicesInvocationService smartServicesInvocationService, CrfKitHolderEventRepository crfKitHolderEventRepository, ExceptionHandlerService exceptionHandler, ModelMapper modelMapper) {
        this.smartServicesInvocationService = smartServicesInvocationService;
        this.exceptionHandler = exceptionHandler;
        this.modelMapper = modelMapper;
        this.crfKitHolderEventRepository = crfKitHolderEventRepository;
        this.kafkaMessageProducer = kafkaMessageProducer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void invokeKhSelfAwareness(MultipartFile eventsFile, CrfSelfAwarenessParametersDto parameters) throws IOException {

        // Extract data from CSV into Dto
        List<CrfKitHolderEventDto> eventData = CsvFileUtils.extractKhEventsDataFromCSV(eventsFile);

        // Formulate the service input and invoke the service
        CrfSelfAwarenessInputDto invocationData = CrfSelfAwarenessInputDto.builder()
                .data(eventData)
                .parameters(parameters)
                .smartServiceId(parameters.getSmartServiceId())
                .moduleId(parameters.getModuleId())
                .build();

        smartServicesInvocationService.formulateAndImplementSmartServiceRequest(invocationData, null, "CRF Self Awareness Wear Monitoring");
    }

    /**
     * Retrieve paginated kit holder events

     * @param pageable : Pagination attributes
     * @return Page<CrfKhEventNotificationDto>
     */
    @Override
    public Page<CrfKitHolderEventDto> retrievePaginatedKhEventResultsPaginated(Pageable pageable) {
        return exceptionHandler.handleOperation(() -> {
            Page<CrfKitHolderEvent> entityPage = crfKitHolderEventRepository.findAll(pageable);

            return entityPage.map(event -> modelMapper.map(event, CrfKitHolderEventDto.class));
        }, "retrievePaginatedKhEventResultsByModuleId");
    }

    /**
     * Retrieve paginated kit holder events by module ID
     *
     * @param moduleId : Module ID
     * @param pageable : Pagination attributes
     * @return Page<CrfKitHolderEventDto>
     */
    @Override
    public Page<CrfKitHolderEventDto> retrievePaginatedKhEventResultsByModule(String moduleId, Pageable pageable) {
        return exceptionHandler.handleOperation(() -> {
            Page<CrfKitHolderEvent> events = crfKitHolderEventRepository.findByModuleId(moduleId, pageable);

            return events.map(event -> modelMapper.map(event, CrfKitHolderEventDto.class));
        }, "retrievePaginatedKhEventResultsByModule");
    }

    /**
     * Register Kit Holder Event Notification
     * @param event
     */
    @Override
    public void registerKitHolderEvent(CrfKitHolderEventDto event) {
        exceptionHandler.handleOperation(() -> {
            // Store the event
            CrfKitHolderEvent eventNotification = crfKitHolderEventRepository.save(modelMapper.map(event, CrfKitHolderEvent.class));

            // Publish event
            EventDto eventMessage = EventDto.builder()
                    .module(eventNotification.getModuleId())
                    .smartService(null)
                    .topic(KafkaTopics.CRF_SELF_AWARENESS_WEAR_DETECTION.toString())
                    .priority(MessagePriority.HIGH)
                    .description("A Kit-Holder Event with ID: '" + eventNotification.getId() + "' has been registered in MODAPTO for Kit-Holder: '" + eventNotification.getKhId() + "' and Type: '" + eventNotification.getKhType())
                    .sourceComponent("Evaluation and Decision Support")
                    .eventType("Kit-Holder Event Registration from Worker")
                    .timestamp(LocalDateTime.now())
                    .results(objectMapper.valueToTree(eventNotification))
                    .build();
            kafkaMessageProducer.sendMessage(KafkaTopics.CRF_SELF_AWARENESS_WEAR_DETECTION.toString(), eventMessage);
            return null;
        }, "registerKitHolderEvent");
    }
}

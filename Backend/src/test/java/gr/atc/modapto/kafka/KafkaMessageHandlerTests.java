package gr.atc.modapto.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.enums.MessagePriority;
import gr.atc.modapto.service.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaMessageHandlerTests {

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private KafkaMessageHandler kafkaMessageHandler;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private EventDto validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new EventDto();
        validEvent.setPriority(MessagePriority.valueOf("HIGH"));
        validEvent.setProductionModule("ModuleA");
        validEvent.setTopic("custom-internal-topic");
    }

    @DisplayName("Consume Event: Success")
    @Test
    void givenValidEvent_whenConsume_thenNotifyWebSocket() throws JsonProcessingException {
        String topic = "modapto-mqtt-topics";

        kafkaMessageHandler.consume(validEvent, topic);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

        verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

        assertEquals(objectMapper.writeValueAsString(validEvent), messageCaptor.getValue());
        assertEquals(validEvent.getTopic(), topicCaptor.getValue());
    }

    @DisplayName("Consume Event: Invalid Event")
    @Test
    void givenInvalidEvent_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
        EventDto invalidEvent = new EventDto();
        String topic = "predictive-maintenance";

        kafkaMessageHandler.consume(invalidEvent, topic);

        verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
    }
}

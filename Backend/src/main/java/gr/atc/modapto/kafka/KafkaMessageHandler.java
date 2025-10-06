package gr.atc.modapto.kafka;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaMessageHandler {

    @Value("${kafka.topics}")
    @SuppressWarnings("unused")
    private List<String> kafkaTopics;

    private final WebSocketService webSocketService;

    private static final String MODAPTO_MQTT_TOPIC = "modapto-mqtt-topics";

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public KafkaMessageHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    /**
     * Kafka consumer method to receive a JSON Event message - From Kafka Producers
     *
     * @param event: Event occurred in MODAPTO
     */
    @KafkaListener(topics = "#{'${kafka.topics}'.split(',')}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(EventDto event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,@Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey) {
        // Validate that same essential variables are present
        log.debug("Event message received on topic: {} with Event Data: {}", topic, event);
        if (event.getPriority() == null || event.getModule() == null || event.getTopic() == null) {
            log.error("Message discarded! Either priority, topic or production module are missing from the event. Message is discarded!");
            return;
        }

        // Check if message is from MQTT
        if (topic.equalsIgnoreCase(MODAPTO_MQTT_TOPIC))
            event.setTopic(messageKey);

        // If no results are present then consume the message and return
        if(event.getResults().isNull()){
            return;
        }
        try{
            // Route Topic Message to WebSocket message
            webSocketService.notifyInWebSocketTopic(objectMapper.writeValueAsString(event.getResults()), topic);
        } catch (JsonProcessingException e) {
            log.error("Unable to parse Event JSON or Results JSON to String Object - Error: {}", e.getMessage());
        }
    }
}
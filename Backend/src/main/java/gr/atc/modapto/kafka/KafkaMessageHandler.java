package gr.atc.modapto.kafka;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaMessageHandler {

    @Value("${kafka.topics}")
    @SuppressWarnings("unused")
    private List<String> kafkaTopics;

    private final WebSocketService webSocketService;

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
    public void consume(EventDto event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){
        // Validate that same essential variables are present
        if (event.getPriority() == null || event.getProductionModule() == null){
            log.error("Either priority or production module is missing from the event. Message is discarded!");
            return;
        }

        // Check if received message on MQTT Topic in Kafka and change the topic to the internal one
        if (topic.equalsIgnoreCase("modapto-mqtt-topics") && event.getTopic() != null)
            topic = event.getTopic();

        try {
            // Route Topic Message to WebSocket message
            webSocketService.notifyInWebSocketTopic(objectMapper.writeValueAsString(event), topic);
        } catch (JsonProcessingException e){
            log.error("Unable to parse Event to String Object");
        }
    }


}

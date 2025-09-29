package gr.atc.modapto.kafka;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.atc.modapto.dto.BaseEventResultsDto;
import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewGroupingPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.enums.WebSocketTopics;
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
    public void consume(EventDto event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        // Validate that same essential variables are present
        log.debug("Event message received on topic: {} with Event Data: {}", topic, event);
        if (event.getPriority() == null || event.getModule() == null || event.getTopic() == null) {
            log.error("Message discarded! Either priority, topic or production module are missing from the event. Message is discarded!");
            return;
        }

        String webSocketTopic;
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
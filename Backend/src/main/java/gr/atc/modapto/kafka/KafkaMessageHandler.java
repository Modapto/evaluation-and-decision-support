package gr.atc.modapto.kafka;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.atc.modapto.dto.BaseEventResultsDto;
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

        // Check if received message on MQTT Topic in Kafka and change the topic to the internal one
        if (topic.equalsIgnoreCase("modapto-mqtt-topics"))
            topic = event.getTopic();

        String webSocketTopic;
        try {
            // Check the instance of the Results
            BaseEventResultsDto result = objectMapper.treeToValue(event.getResults(), BaseEventResultsDto.class);

            switch (result) {
                case CrfSimulationResultsDto ignored ->
                        webSocketTopic = WebSocketTopics.CRF_SIMULATION_RESULTS.toString();
                case CrfOptimizationResultsDto ignored ->
                        webSocketTopic = WebSocketTopics.CRF_OPTIMIZATION_RESULTS.toString();
                case SewSimulationResultsDto ignored ->
                        webSocketTopic = WebSocketTopics.SEW_SIMULATION_RESULTS.toString();
                case SewOptimizationResultsDto ignored ->
                        webSocketTopic = WebSocketTopics.SEW_OPTIMIZATION_RESULTS.toString();
                case SewGroupingPredictiveMaintenanceOutputDto ignored ->
                        webSocketTopic = WebSocketTopics.SEW_GROUPING_PREDICTIVE_MAINTENANCE.toString();
                case SewSelfAwarenessMonitoringKpisResultsDto ignored ->
                        webSocketTopic = WebSocketTopics.SEW_SELF_AWARENESS_MONITORING_KPIS.toString();
                default -> {
                    log.error("Unknown results data format provided. Results are discarded");
                    return;
                }
            }

            // Route Topic Message to WebSocket message
            webSocketService.notifyInWebSocketTopic(objectMapper.writeValueAsString(result), webSocketTopic);
        } catch (JsonProcessingException e) {
            log.error("Unable to parse Event JSON or Results JSON to String Object - Error: {}", e.getMessage());
        }
    }
}
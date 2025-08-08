package gr.atc.modapto.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.enums.MessagePriority;
import gr.atc.modapto.enums.WebSocketTopics;
import gr.atc.modapto.service.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaMessageHandler Tests")
class KafkaMessageHandlerTests {

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private KafkaMessageHandler kafkaMessageHandler;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private EventDto baseEvent;

    @BeforeEach
    void setUp() {
        baseEvent = new EventDto();
        baseEvent.setPriority(MessagePriority.HIGH);
        baseEvent.setProductionModule("ModuleA");
        baseEvent.setTopic("modapto-mqtt-topics");
    }

    @Nested
    @DisplayName("CRF Simulation Results")
    class CrfSimulationResults {

        @Test
        @DisplayName("Consume CRF simulation event : Success")
        void givenValidCrfSimulationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            // Given
            CrfSimulationResultsDto simulationResult = CrfSimulationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .message("Simulation completed")
                    .simulationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);
            baseEvent.setResults(resultNode);
            String topic = "crf-simulation-topic";

            // When
            kafkaMessageHandler.consume(baseEvent, topic);

            // Then
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(simulationResult), messageCaptor.getValue());
            assertEquals(WebSocketTopics.CRF_SIMULATION_RESULTS.toString(), topicCaptor.getValue());
        }

        @Test
        @DisplayName("Consume CRF simulation event from MQTT : Success")
        void givenValidCrfSimulationEventFromMqtt_whenConsume_thenUsesEventTopicInsteadOfMqtt() throws JsonProcessingException {
            // Given
            CrfSimulationResultsDto simulationResult = CrfSimulationResultsDto.builder()
                    .id("2")
                    .timestamp("2024-01-16T12:00:00.000Z")
                    .message("MQTT simulation completed")
                    .simulationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);
            baseEvent.setResults(resultNode);
            String mqttTopic = "modapto-mqtt-topics";

            // When
            kafkaMessageHandler.consume(baseEvent, mqttTopic);

            // Then
            verify(webSocketService, times(1)).notifyInWebSocketTopic(anyString(), eq(WebSocketTopics.CRF_SIMULATION_RESULTS.toString()));
        }
    }

    @Nested
    @DisplayName("CRF Optimization Results")
    class CrfOptimizationResults {

        @Test
        @DisplayName("Consume CRF optimization event : Success")
        void givenValidCrfOptimizationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            // Given
            CrfOptimizationResultsDto optimizationResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .message("Optimization completed")
                    .optimizationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(optimizationResult);
            baseEvent.setResults(resultNode);
            String topic = "crf-optimization-topic";

            // When
            kafkaMessageHandler.consume(baseEvent, topic);

            // Then
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(optimizationResult), messageCaptor.getValue());
            assertEquals(WebSocketTopics.CRF_OPTIMIZATION_RESULTS.toString(), topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("SEW Simulation Results")
    class SewSimulationResults {

        @Test
        @DisplayName("Consume SEW simulation event : Success")
        void givenValidSewSimulationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            // Given
            SewSimulationResultsDto simulationResult = SewSimulationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .simulationData(createSampleSimulationData())
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);
            baseEvent.setResults(resultNode);
            String topic = "sew-simulation-topic";

            // When
            kafkaMessageHandler.consume(baseEvent, topic);

            // Then
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(simulationResult), messageCaptor.getValue());
            assertEquals(WebSocketTopics.SEW_SIMULATION_RESULTS.toString(), topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("SEW Optimization Results")
    class SewOptimizationResults {

        @Test
        @DisplayName("Consume SEW optimization event : Success")
        void givenValidSewOptimizationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            // Given
            SewOptimizationResultsDto optimizationResult = SewOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .data(createSampleOptimizationData())
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(optimizationResult);
            baseEvent.setResults(resultNode);
            String topic = "sew-optimization-topic";

            // When
            kafkaMessageHandler.consume(baseEvent, topic);

            // Then
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(optimizationResult), messageCaptor.getValue());
            assertEquals(WebSocketTopics.SEW_OPTIMIZATION_RESULTS.toString(), topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("Invalid Events")
    class InvalidEvents {

        @Test
        @DisplayName("Consume event with missing priority : Error logged and no notification")
        void givenEventWithMissingPriority_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
            // Given
            EventDto invalidEvent = new EventDto();
            invalidEvent.setProductionModule("ModuleA");
            invalidEvent.setTopic("test-topic");
            // Missing priority
            String topic = "test-kafka-topic";

            // When
            kafkaMessageHandler.consume(invalidEvent, topic);

            // Then
            verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
        }

        @Test
        @DisplayName("Consume event with missing production module : Error logged and no notification")
        void givenEventWithMissingProductionModule_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
            // Given
            EventDto invalidEvent = new EventDto();
            invalidEvent.setPriority(MessagePriority.HIGH);
            invalidEvent.setTopic("test-topic");
            // Missing productionModule
            String topic = "test-kafka-topic";

            // When
            kafkaMessageHandler.consume(invalidEvent, topic);

            // Then
            verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
        }

        @Test
        @DisplayName("Consume event with missing topic : Error logged and no notification")
        void givenEventWithMissingTopic_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
            // Given
            EventDto invalidEvent = new EventDto();
            invalidEvent.setPriority(MessagePriority.HIGH);
            invalidEvent.setProductionModule("ModuleA");
            // Missing topic
            String topic = "test-kafka-topic";

            // When
            kafkaMessageHandler.consume(invalidEvent, topic);

            // Then
            verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
        }

        @Test
        @DisplayName("Consume event with unknown result type : Error logged and no notification")
        void givenEventWithUnknownResultType_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
            // Given
            Map<String, Object> unknownResult = new HashMap<>();
            unknownResult.put("unknownField", "unknownValue");
            unknownResult.put("type", "UnknownType");

            JsonNode resultNode = objectMapper.valueToTree(unknownResult);
            baseEvent.setResults(resultNode);
            String topic = "test-kafka-topic";

            // When
            kafkaMessageHandler.consume(baseEvent, topic);

            // Then
            verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
        }
    }

    /*
     * Helper Methods
     */
    private SewSimulationResultsDto.SimulationData createSampleSimulationData() {
        SewSimulationResultsDto.MetricComparison makespan = new SewSimulationResultsDto.MetricComparison(
                240.0, 220.0, -20.0, -8.33
        );
        SewSimulationResultsDto.MetricComparison machineUtilization = new SewSimulationResultsDto.MetricComparison(
                85.5, 92.3, 6.8, 7.95
        );
        SewSimulationResultsDto.MetricComparison throughputStdev = new SewSimulationResultsDto.MetricComparison(
                12.5, 8.7, -3.8, -30.4
        );

        return new SewSimulationResultsDto.SimulationData(makespan, machineUtilization, throughputStdev);
    }

    private Map<String, SewOptimizationResultsDto.SolutionData> createSampleOptimizationData() {
        // Create DTO structure matching the exact model structure
        Map<String, SewOptimizationResultsDto.SolutionData> data = new HashMap<>();

        // Create MetricsData for DTO
        SewOptimizationResultsDto.MetricsData metricsDto = new SewOptimizationResultsDto.MetricsData("240");

        // Create seq map for DTO
        Map<String, Map<String, String>> seqDto = new HashMap<>();
        Map<String, String> seqItemDto = new HashMap<>();
        seqItemDto.put("operation", "cutting");
        seqItemDto.put("duration", "30");
        seqDto.put("seq_1", seqItemDto);

        // Create orders map for DTO
        Map<String, Map<String, Map<String, Map<String, SewOptimizationResultsDto.TimeRange>>>> ordersDto = new HashMap<>();

        // Create init order for DTO
        List<String> initOrderDto = Arrays.asList("order_1", "order_2", "order_3");

        // Create SolutionData for DTO
        SewOptimizationResultsDto.SolutionData solutionDataDto = new SewOptimizationResultsDto.SolutionData(
                metricsDto, seqDto, ordersDto, initOrderDto
        );

        data.put("solution_1", solutionDataDto);
        return data;
    }
}
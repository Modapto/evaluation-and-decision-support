package gr.atc.modapto.kafka;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewGroupingPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.enums.MessagePriority;
import gr.atc.modapto.service.WebSocketService;

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
        baseEvent.setModule("ModuleA");
        baseEvent.setTopic("test-topic");
    }

    @Nested
    @DisplayName("CRF Simulation Results")
    class CrfSimulationResults {

        @Test
        @DisplayName("Consume CRF simulation event : Success")
        void givenValidCrfSimulationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            CrfSimulationResultsDto simulationResult = CrfSimulationResultsDto.builder()
                    .id("1")
                    .timestamp(LocalDateTime.parse("2024-01-15T10:30:00"))
                    .message("Simulation completed")
                    .simulationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);
            baseEvent.setResults(resultNode);
            baseEvent.setTopic("crf-simulation-topic");
            String topic = "crf-simulation-topic";

            kafkaMessageHandler.consume(baseEvent, topic, "sampleKey");

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(simulationResult), messageCaptor.getValue());
            assertEquals("crf-simulation-topic", topicCaptor.getValue());
        }

        @Test
        @DisplayName("Consume CRF simulation event from MQTT : Success")
        void givenValidCrfSimulationEventFromMqtt_whenConsume_thenUsesEventTopicInsteadOfMqtt(){
            CrfSimulationResultsDto simulationResult = CrfSimulationResultsDto.builder()
                    .id("2")
                    .timestamp(LocalDateTime.parse("2024-01-15T10:30:00"))
                    .message("MQTT simulation completed")
                    .simulationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);
            baseEvent.setResults(resultNode);
            baseEvent.setTopic("original-event-topic");
            String mqttTopic = "modapto-mqtt-topics";

            kafkaMessageHandler.consume(baseEvent, mqttTopic, "sampleKey");

            verify(webSocketService, times(1)).notifyInWebSocketTopic(anyString(), eq("sampleKey"));
        }
    }

    @Nested
    @DisplayName("CRF Optimization Results")
    class CrfOptimizationResults {

        @Test
        @DisplayName("Consume CRF optimization event : Success")
        void givenValidCrfOptimizationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            CrfOptimizationResultsDto optimizationResult = CrfOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp(LocalDateTime.parse("2024-01-15T10:30:00"))
                    .message("Optimization completed")
                    .optimizationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(optimizationResult);
            baseEvent.setResults(resultNode);
            baseEvent.setTopic("crf-optimization-topic");
            String topic = "crf-optimization-topic";

            kafkaMessageHandler.consume(baseEvent, topic, "sampleKey");

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(optimizationResult), messageCaptor.getValue());
            assertEquals("crf-optimization-topic", topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("SEW Simulation Results")
    class SewSimulationResults {

        @Test
        @DisplayName("Consume SEW simulation event : Success")
        void givenValidSewSimulationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            SewSimulationResultsDto simulationResult = SewSimulationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .simulationData(createSampleSimulationData())
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);
            baseEvent.setResults(resultNode);
            baseEvent.setTopic("sew-simulation-topic");
            String topic = "sew-simulation-topic";

            kafkaMessageHandler.consume(baseEvent, topic, "sampleKey");

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(simulationResult), messageCaptor.getValue());
            assertEquals("sew-simulation-topic", topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("SEW Optimization Results")
    class SewOptimizationResults {

        @Test
        @DisplayName("Consume SEW optimization event : Success")
        void givenValidSewOptimizationEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            SewOptimizationResultsDto optimizationResult = SewOptimizationResultsDto.builder()
                    .id("1")
                    .timestamp("2024-01-15T10:30:00.000Z")
                    .data(createSampleOptimizationData())
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(optimizationResult);
            baseEvent.setResults(resultNode);
            baseEvent.setTopic("sew-optimization-topic");
            String topic = "sew-optimization-topic";

            kafkaMessageHandler.consume(baseEvent, topic, "sampleKey");

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(optimizationResult), messageCaptor.getValue());
            assertEquals("sew-optimization-topic", topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("Invalid Events")
    class InvalidEvents {

        @Test
        @DisplayName("Consume event with missing priority : Error logged and no notification")
        void givenEventWithMissingPriority_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
            EventDto invalidEvent = new EventDto();
            invalidEvent.setModule("ModuleA");
            invalidEvent.setTopic("test-topic");
            // Missing priority
            String topic = "test-kafka-topic";

            kafkaMessageHandler.consume(invalidEvent, topic, "sampleKey");

            verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
        }

        @Test
        @DisplayName("Consume event with missing module : Error logged and no notification")
        void givenEventWithMissingModule_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
            EventDto invalidEvent = new EventDto();
            invalidEvent.setPriority(MessagePriority.HIGH);
            invalidEvent.setTopic("test-topic");
            // Missing module
            String topic = "test-kafka-topic";

            kafkaMessageHandler.consume(invalidEvent, topic, "sampleKey");

            verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
        }

        @Test
        @DisplayName("Consume event with missing topic : Error logged and no notification")
        void givenEventWithMissingTopic_whenConsume_thenLogErrorAndDoNotNotifyWebSocket() {
            EventDto invalidEvent = new EventDto();
            invalidEvent.setPriority(MessagePriority.HIGH);
            invalidEvent.setModule("ModuleA");
            // Missing topic
            String topic = "test-kafka-topic";

            kafkaMessageHandler.consume(invalidEvent, topic, "sampleKey");

            verify(webSocketService, never()).notifyInWebSocketTopic(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("SEW Grouping Predictive Maintenance Results")
    class SewGroupingPredictiveMaintenanceResults {

        @Test
        @DisplayName("Consume SEW grouping predictive maintenance event : Success")
        void givenValidSewGroupingPredictiveMaintenanceEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            SewGroupingPredictiveMaintenanceOutputDto.MaintenanceComponent component1 = SewGroupingPredictiveMaintenanceOutputDto.MaintenanceComponent.builder()
                    .moduleId("12345")
                    .module("Motor Bearing Unit A1")
                    .replacementTime(48.5)
                    .duration(2.5)
                    .build();
                    
            SewGroupingPredictiveMaintenanceOutputDto.MaintenanceComponent component2 = SewGroupingPredictiveMaintenanceOutputDto.MaintenanceComponent.builder()
                    .moduleId("12346")
                    .module("Hydraulic Pump B2")
                    .replacementTime(72.0)
                    .duration(4.0)
                    .build();
                    
            Map<String, List<SewGroupingPredictiveMaintenanceOutputDto.MaintenanceComponent>> groupingMaintenanceData = new HashMap<>();
            groupingMaintenanceData.put("Stage1", Arrays.asList(component1));
            groupingMaintenanceData.put("Stage2", Arrays.asList(component2));
            
            Map<String, List<SewGroupingPredictiveMaintenanceOutputDto.MaintenanceComponent>> individualMaintenanceData = new HashMap<>();
            individualMaintenanceData.put("Individual_Stage1", Arrays.asList(component1));
            
            SewGroupingPredictiveMaintenanceOutputDto.TimeWindow timeWindow = SewGroupingPredictiveMaintenanceOutputDto.TimeWindow.builder()
                    .begin(LocalDateTime.parse("2024-01-15T08:00:00"))
                    .end(LocalDateTime.parse("2024-01-15T18:00:00"))
                    .build();
                    
            SewGroupingPredictiveMaintenanceOutputDto maintenanceResult = SewGroupingPredictiveMaintenanceOutputDto.builder()
                    .id("grouping-maintenance-001")
                    .moduleId("sew-module-1")
                    .smartServiceId("grouping-predictive-maintenance")
                    .costSavings(15000.75)
                    .timeWindow(timeWindow)
                    .groupingMaintenance(groupingMaintenanceData)
                    .individualMaintenance(individualMaintenanceData)
                    .timestamp(LocalDateTime.parse("2024-01-15T10:30:00"))
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(maintenanceResult);
            baseEvent.setResults(resultNode);
            baseEvent.setTopic("sew-grouping-predictive-maintenance-topic");
            String topic = "sew-grouping-predictive-maintenance-topic";

            kafkaMessageHandler.consume(baseEvent, topic, "sampleKey");

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(maintenanceResult), messageCaptor.getValue());
            assertEquals("sew-grouping-predictive-maintenance-topic", topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("SEW Self-Awareness Monitoring KPIs Results")
    class SewSelfAwarenessMonitoringKpisResults {

        @Test
        @DisplayName("Consume SEW self-awareness monitoring KPIs event : Success")
        void givenValidSewSelfAwarenessMonitoringKpisEvent_whenConsume_thenNotifyWebSocketWithCorrectTopic() throws JsonProcessingException {
            List<Double> dataList = Arrays.asList(85.5, 87.2, 89.1, 86.8, 88.5, 90.2, 89.7, 88.9);
            
            SewSelfAwarenessMonitoringKpisResultsDto kpisResult = SewSelfAwarenessMonitoringKpisResultsDto.builder()
                    .id("kpis-monitoring-001")
                    .timestamp(LocalDateTime.now())
                    .smartServiceId("self-awareness-monitoring-kpis")
                    .moduleId("sew-module-1")
                    .stage("Stage_1")
                    .cell("Cell_A")
                    .plc("Plc_1")
                    .module("Module_a")
                    .component("Cutting_Station_01")
                    .variable("Overall_Equipment_Effectiveness")
                    .startingDate("2024-01-15T08:00:00.000Z")
                    .endingDate("2024-01-15T16:00:00.000Z")
                    .dataSource("PLC_Sensors")
                    .bucket("hourly_aggregation")
                    .data(dataList)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(kpisResult);
            baseEvent.setResults(resultNode);
            baseEvent.setTopic("sew-self-awareness-monitoring-kpis-topic");
            String topic = "sew-self-awareness-monitoring-kpis-topic";

            kafkaMessageHandler.consume(baseEvent, topic, "sampleKey");

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

            verify(webSocketService, times(1)).notifyInWebSocketTopic(messageCaptor.capture(), topicCaptor.capture());

            assertEquals(objectMapper.writeValueAsString(kpisResult), messageCaptor.getValue());
            assertEquals("sew-self-awareness-monitoring-kpis-topic", topicCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("Enhanced Event Validation")
    class EnhancedEventValidation {

        @Test
        @DisplayName("Validate that module field is used instead of productionModule : Success")
        void givenEventWithModuleField_whenConsume_thenProcessesCorrectly(){
            EventDto eventWithModule = EventDto.builder()
                    .priority(MessagePriority.HIGH)
                    .module("TestModule")
                    .topic("test-topic")
                    .build();
            
            CrfSimulationResultsDto simulationResult = CrfSimulationResultsDto.builder()
                    .id("1")
                    .timestamp(LocalDateTime.parse("2024-01-15T10:30:00"))
                    .message("Test simulation")
                    .simulationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);
            eventWithModule.setResults(resultNode);
            String topic = "test-topic";

            kafkaMessageHandler.consume(eventWithModule, topic, "sampleKey");

            verify(webSocketService, times(1)).notifyInWebSocketTopic(anyString(), anyString());
        }

        @Test
        @DisplayName("Handle event with different priority levels : Success")
        void givenEventWithDifferentPriorities_whenConsume_thenProcessesAllCorrectly(){
            MessagePriority[] priorities = {MessagePriority.LOW, MessagePriority.MID, MessagePriority.HIGH};
            
            CrfSimulationResultsDto simulationResult = CrfSimulationResultsDto.builder()
                    .id("priority-test")
                    .timestamp(LocalDateTime.parse("2024-01-15T10:30:00"))
                    .message("Priority test")
                    .simulationRun(true)
                    .build();

            JsonNode resultNode = objectMapper.valueToTree(simulationResult);

            for (MessagePriority priority : priorities) {
                EventDto eventWithPriority = EventDto.builder()
                        .priority(priority)
                        .module("TestModule")
                        .topic("test-topic")
                        .results(resultNode)
                        .build();
                
                kafkaMessageHandler.consume(eventWithPriority, "test-topic", "sampleKey");
            }

            verify(webSocketService, times(priorities.length)).notifyInWebSocketTopic(anyString(), anyString());
        }
    }

    /*
     * Helper Methods
     */
    private Object createSampleSimulationData() {
        Map<String, Object> simulationData = new HashMap<>();
        simulationData.put("kpi", "performance_data");
        simulationData.put("metrics", 95.5);
        return simulationData;
    }

    private Map<String, Object> createSampleOptimizationData() {
        Map<String, Object> solution = new HashMap<>();
        solution.put("schedule", "optimized_schedule_data");
        solution.put("cost", 1000.0);

        Map<String, Object> data = new HashMap<>();
        data.put("solution_1", solution);
        return data;
    }
}
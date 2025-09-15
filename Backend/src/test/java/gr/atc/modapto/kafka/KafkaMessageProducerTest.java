package gr.atc.modapto.kafka;

import gr.atc.modapto.dto.EventDto;
import gr.atc.modapto.enums.KafkaTopics;
import gr.atc.modapto.enums.MessagePriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaMessageProducer Tests")
class KafkaMessageProducerTest {

    @Mock
    private KafkaTemplate<String, EventDto> kafkaTemplate;

    @InjectMocks
    private KafkaMessageProducer kafkaMessageProducer;

    private EventDto testEvent;

    @BeforeEach
    void setUp() {
        // Given - Create a test event
        testEvent = EventDto.builder()
                .id("test-event-1")
                .description("Test event for Kafka producer")
                .module("TestModule")
                .timestamp(LocalDateTime.now())
                .priority(MessagePriority.HIGH)
                .eventType("TEST_EVENT")
                .sourceComponent("TestComponent")
                .smartService("TestService")
                .topic("test-topic")
                .build();
    }

    @Nested
    @DisplayName("When sending messages to Kafka topics")
    class MessageSending {

        @Test
        @DisplayName("Send message to specified topic : Success")
        void givenValidEventAndTopic_whenSendMessage_thenMessageIsSentToKafka() {
            // Given
            String targetTopic = "test-kafka-topic";
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(targetTopic), eq(testEvent))).thenReturn(future);

            // When
            kafkaMessageProducer.sendMessage(targetTopic, testEvent);

            // Then
            verify(kafkaTemplate, times(1)).send(targetTopic, testEvent);
        }

        @Test
        @DisplayName("Send message using KafkaTopics enum : Success")
        void givenValidEventAndKafkaTopicEnum_whenSendMessage_thenMessageIsSentToCorrectTopic() {
            // Given
            String targetTopic = KafkaTopics.SEW_SIMULATION_RESULTS.toString();
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(targetTopic), eq(testEvent))).thenReturn(future);

            // When
            kafkaMessageProducer.sendMessage(targetTopic, testEvent);

            // Then
            verify(kafkaTemplate, times(1)).send(targetTopic, testEvent);
        }

        @ParameterizedTest(name = "Should send message to topic: {0}")
        @EnumSource(KafkaTopics.class)
        @DisplayName("Send messages to all supported Kafka topics : Success")
        void givenValidEvent_whenSendMessageToAllTopics_thenMessageIsSentSuccessfully(KafkaTopics kafkaTopic) {
            // Given
            String topicName = kafkaTopic.toString();
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(topicName), eq(testEvent))).thenReturn(future);

            // When
            kafkaMessageProducer.sendMessage(topicName, testEvent);

            // Then
            verify(kafkaTemplate, times(1)).send(topicName, testEvent);
        }

        @Test
        @DisplayName("Handle multiple messages sent sequentially : Success")
        void givenMultipleEvents_whenSendMessagesSequentially_thenAllMessagesAreSent() {
            // Given
            String topic1 = KafkaTopics.CRF_SIMULATION_RESULTS.toString();
            String topic2 = KafkaTopics.SEW_OPTIMIZATION_RESULTS.toString();
            EventDto event1 = EventDto.builder()
                    .id("event-1")
                    .module("CRF")
                    .priority(MessagePriority.HIGH)
                    .topic(topic1)
                    .build();
            EventDto event2 = EventDto.builder()
                    .id("event-2")
                    .module("SEW")
                    .priority(MessagePriority.MID)
                    .topic(topic2)
                    .build();

            CompletableFuture<SendResult<String, EventDto>> future1 = mock(CompletableFuture.class);
            CompletableFuture<SendResult<String, EventDto>> future2 = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(topic1), eq(event1))).thenReturn(future1);
            when(kafkaTemplate.send(eq(topic2), eq(event2))).thenReturn(future2);

            // When
            kafkaMessageProducer.sendMessage(topic1, event1);
            kafkaMessageProducer.sendMessage(topic2, event2);

            // Then
            verify(kafkaTemplate, times(1)).send(topic1, event1);
            verify(kafkaTemplate, times(1)).send(topic2, event2);
        }
    }

    @Nested
    @DisplayName("When handling edge cases and error scenarios")
    class EdgeCasesAndErrors {

        @Test
        @DisplayName("Handle null topic : Success")
        void givenNullTopic_whenSendMessage_thenKafkaTemplateIsCalledWithNull() {
            // Given
            String nullTopic = null;
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(nullTopic), eq(testEvent))).thenReturn(future);

            // When
            assertDoesNotThrow(() -> kafkaMessageProducer.sendMessage(nullTopic, testEvent));

            // Then
            verify(kafkaTemplate, times(1)).send(nullTopic, testEvent);
        }

        @Test
        @DisplayName("Handle null event : Success")
        void givenNullEvent_whenSendMessage_thenKafkaTemplateIsCalledWithNull() {
            // Given
            String topic = "test-topic";
            EventDto nullEvent = null;
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(topic), eq(nullEvent))).thenReturn(future);

            // When
            assertDoesNotThrow(() -> kafkaMessageProducer.sendMessage(topic, nullEvent));

            // Then
            verify(kafkaTemplate, times(1)).send(topic, nullEvent);
        }

        @Test
        @DisplayName("Handle empty topic string : Success")
        void givenEmptyTopic_whenSendMessage_thenKafkaTemplateIsCalledWithEmptyString() {
            // Given
            String emptyTopic = "";
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(emptyTopic), eq(testEvent))).thenReturn(future);

            // When
            assertDoesNotThrow(() -> kafkaMessageProducer.sendMessage(emptyTopic, testEvent));

            // Then
            verify(kafkaTemplate, times(1)).send(emptyTopic, testEvent);
        }

        @Test
        @DisplayName("Handle event with minimal required fields : Success")
        void givenEventWithMinimalFields_whenSendMessage_thenMessageIsSentSuccessfully() {
            // Given
            EventDto minimalEvent = EventDto.builder()
                    .module("MinimalModule")
                    .priority(MessagePriority.LOW)
                    .topic("minimal-topic")
                    .build();
            String topic = KafkaTopics.SEW_PROCESS_DRIFT.toString();
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(topic), eq(minimalEvent))).thenReturn(future);

            // When
            kafkaMessageProducer.sendMessage(topic, minimalEvent);

            // Then
            verify(kafkaTemplate, times(1)).send(topic, minimalEvent);
        }

        @Test
        @DisplayName("Handle event with all fields populated : Success")
        void givenEventWithAllFields_whenSendMessage_thenMessageIsSentSuccessfully() {
            // Given
            EventDto completeEvent = EventDto.builder()
                    .id("complete-event-123")
                    .description("Complete event with all fields")
                    .module("CompleteModule")
                    .timestamp(LocalDateTime.of(2024, 12, 1, 10, 30, 0))
                    .priority(MessagePriority.HIGH)
                    .eventType("COMPLETE_EVENT")
                    .sourceComponent("CompleteComponent")
                    .smartService("CompleteService")
                    .topic("complete-topic")
                    .build();
            String topic = KafkaTopics.CRF_SELF_AWARENESS.toString();
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(topic), eq(completeEvent))).thenReturn(future);

            // When
            kafkaMessageProducer.sendMessage(topic, completeEvent);

            // Then
            verify(kafkaTemplate, times(1)).send(topic, completeEvent);
        }
    }

    @Nested
    @DisplayName("When verifying component behavior")
    class ComponentBehavior {

        @Test
        @DisplayName("Initialize with KafkaTemplate dependency : Success")
        void givenKafkaTemplate_whenProducerIsCreated_thenDependencyIsInjected() {
            // Given & When - Constructor injection happens via @InjectMocks
            
            // Then
            assertNotNull(kafkaMessageProducer);
        }

        @Test
        @DisplayName("Maintain correct method signature for Spring integration : Success")
        void givenProducerInstance_whenCheckingMethodSignature_thenSignatureIsCorrect() {
            // Given
            String topic = "signature-test-topic";
            
            // When & Then - Verify method exists and can be called
            assertDoesNotThrow(() -> kafkaMessageProducer.sendMessage(topic, testEvent));
        }

        @Test
        @DisplayName("Preserve event object during sending : Success")
        void givenEvent_whenSendMessage_thenEventRemainsUnmodified() {
            // Given
            String originalId = testEvent.getId();
            String originalModule = testEvent.getModule();
            MessagePriority originalPriority = testEvent.getPriority();
            String topic = "immutability-test-topic";
            
            CompletableFuture<SendResult<String, EventDto>> future = mock(CompletableFuture.class);
            when(kafkaTemplate.send(eq(topic), eq(testEvent))).thenReturn(future);

            // When
            kafkaMessageProducer.sendMessage(topic, testEvent);

            // Then
            assertEquals(originalId, testEvent.getId());
            assertEquals(originalModule, testEvent.getModule());
            assertEquals(originalPriority, testEvent.getPriority());
        }
    }
}
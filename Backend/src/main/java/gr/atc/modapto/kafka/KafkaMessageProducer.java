package gr.atc.modapto.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import gr.atc.modapto.dto.EventDto;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaMessageProducer {

    private final KafkaTemplate<String, EventDto> kafkaTemplate;

    public KafkaMessageProducer(KafkaTemplate<String, EventDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, EventDto message) {
        log.debug("Sent message to topic {}: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}

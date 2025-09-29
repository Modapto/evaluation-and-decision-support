package gr.atc.modapto.config;

import gr.atc.modapto.dto.EventDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaProducerConfig Tests")
class KafkaProducerConfigTest {

    private KafkaProducerConfig kafkaProducerConfig;
    private static final String TEST_BOOTSTRAP_SERVERS = "localhost:9092,localhost:9093";

    @BeforeEach
    void setUp() {
        kafkaProducerConfig = new KafkaProducerConfig();
        ReflectionTestUtils.setField(kafkaProducerConfig, "kafkaBootstrapServers", TEST_BOOTSTRAP_SERVERS);
    }

    @Nested
    @DisplayName("When configuring ProducerFactory")
    class ProducerFactoryConfiguration {

        @Test
        @DisplayName("Create ProducerFactory with correct configuration properties : Success")
        void givenBootstrapServers_whenCreateProducerFactory_thenFactoryHasCorrectProperties() {
            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();

            assertNotNull(producerFactory);
            assertInstanceOf(DefaultKafkaProducerFactory.class, producerFactory);

            Map<String, Object> configProps = producerFactory.getConfigurationProperties();
            assertEquals(TEST_BOOTSTRAP_SERVERS, configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            assertEquals(StringSerializer.class, configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
            assertEquals(JsonSerializer.class, configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        }

        @Test
        @DisplayName("Use StringSerializer for keys : Success")
        void givenProducerFactory_whenCheckKeySerializer_thenUsesStringSerializer() {
            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();
            Map<String, Object> configProps = producerFactory.getConfigurationProperties();

            assertEquals(StringSerializer.class, configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        }

        @Test
        @DisplayName("Use JsonSerializer for EventDto values : Success")
        void givenProducerFactory_whenCheckValueSerializer_thenUsesJsonSerializer() {
            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();
            Map<String, Object> configProps = producerFactory.getConfigurationProperties();

            assertEquals(JsonSerializer.class, configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        }

        @Test
        @DisplayName("Handle single bootstrap server : Success")
        void givenSingleBootstrapServer_whenCreateProducerFactory_thenConfigurationIsCorrect() {
            String singleServer = "localhost:9092";
            ReflectionTestUtils.setField(kafkaProducerConfig, "kafkaBootstrapServers", singleServer);

            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();

            Map<String, Object> configProps = producerFactory.getConfigurationProperties();
            assertEquals(singleServer, configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }

        @Test
        @DisplayName("Handle multiple bootstrap servers : Success")
        void givenMultipleBootstrapServers_whenCreateProducerFactory_thenConfigurationIsCorrect() {
            String multipleServers = "kafka1:9092,kafka2:9092,kafka3:9092";
            ReflectionTestUtils.setField(kafkaProducerConfig, "kafkaBootstrapServers", multipleServers);

            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();

            Map<String, Object> configProps = producerFactory.getConfigurationProperties();
            assertEquals(multipleServers, configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }

        @Test
        @DisplayName("Create new ProducerFactory instance on each call : Success")
        void givenConfiguration_whenCallProducerFactoryMultipleTimes_thenReturnsNewInstances() {
            ProducerFactory<String, EventDto> factory1 = kafkaProducerConfig.producerFactory();
            ProducerFactory<String, EventDto> factory2 = kafkaProducerConfig.producerFactory();

            assertNotNull(factory1);
            assertNotNull(factory2);
            assertNotSame(factory1, factory2, "ProducerFactory should create new instances");
        }
    }

    @Nested
    @DisplayName("When configuring KafkaTemplate")
    class KafkaTemplateConfiguration {

        @Test
        @DisplayName("Create KafkaTemplate with ProducerFactory : Success")
        void givenProducerFactory_whenCreateKafkaTemplate_thenTemplateIsCorrectlyConfigured() {
            KafkaTemplate<String, EventDto> kafkaTemplate = kafkaProducerConfig.kafkaTemplate();

            assertNotNull(kafkaTemplate);
            assertNotNull(kafkaTemplate.getProducerFactory());
        }

        @Test
        @DisplayName("Create KafkaTemplate with correct generic types : Success")
        void givenConfiguration_whenCreateKafkaTemplate_thenTemplateHasCorrectGenericTypes() {
            KafkaTemplate<String, EventDto> kafkaTemplate = kafkaProducerConfig.kafkaTemplate();

            assertNotNull(kafkaTemplate);
            assertInstanceOf(KafkaTemplate.class, kafkaTemplate);
        }

        @Test
        @DisplayName("Use same ProducerFactory configuration in KafkaTemplate : Success")
        void givenConfiguration_whenCreateKafkaTemplate_thenUsesCorrectProducerFactory() {
            ProducerFactory<String, EventDto> expectedFactory = kafkaProducerConfig.producerFactory();

            KafkaTemplate<String, EventDto> kafkaTemplate = kafkaProducerConfig.kafkaTemplate();

            assertNotNull(kafkaTemplate.getProducerFactory());
            
            Map<String, Object> templateFactoryConfig = kafkaTemplate.getProducerFactory().getConfigurationProperties();
            Map<String, Object> expectedFactoryConfig = expectedFactory.getConfigurationProperties();
            
            assertEquals(expectedFactoryConfig.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG), 
                        templateFactoryConfig.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            assertEquals(expectedFactoryConfig.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG), 
                        templateFactoryConfig.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
            assertEquals(expectedFactoryConfig.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG), 
                        templateFactoryConfig.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        }

        @Test
        @DisplayName("Create new KafkaTemplate instance on each call : Success")
        void givenConfiguration_whenCallKafkaTemplateMultipleTimes_thenReturnsNewInstances() {
            KafkaTemplate<String, EventDto> template1 = kafkaProducerConfig.kafkaTemplate();
            KafkaTemplate<String, EventDto> template2 = kafkaProducerConfig.kafkaTemplate();

            assertNotNull(template1);
            assertNotNull(template2);
            assertNotSame(template1, template2, "KafkaTemplate should create new instances");
        }
    }

    @Nested
    @DisplayName("When validating configuration integrity")
    class ConfigurationIntegrity {

        @Test
        @DisplayName("Maintain configuration consistency between ProducerFactory and KafkaTemplate : Success")
        void givenConfiguration_whenCompareBothBeans_thenConfigurationIsConsistent() {
            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();
            KafkaTemplate<String, EventDto> kafkaTemplate = kafkaProducerConfig.kafkaTemplate();

            Map<String, Object> factoryConfig = producerFactory.getConfigurationProperties();
            Map<String, Object> templateConfig = kafkaTemplate.getProducerFactory().getConfigurationProperties();

            assertEquals(factoryConfig.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG),
                        templateConfig.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            assertEquals(factoryConfig.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG),
                        templateConfig.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
            assertEquals(factoryConfig.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG),
                        templateConfig.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        }

        @Test
        @DisplayName("Have all required Kafka producer configuration properties : Success")
        void givenProducerFactory_whenCheckRequiredProperties_thenAllPropertiesArePresent() {
            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();
            Map<String, Object> configProps = producerFactory.getConfigurationProperties();

            assertNotNull(configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            assertNotNull(configProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
            assertNotNull(configProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        }

        @Test
        @DisplayName("Contain only expected configuration properties : Success")
        void givenProducerFactory_whenCheckConfigurationSize_thenContainsOnlyExpectedProperties() {
            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();
            Map<String, Object> configProps = producerFactory.getConfigurationProperties();

            assertEquals(3, configProps.size(), "Configuration should contain exactly 3 properties");
            assertTrue(configProps.containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            assertTrue(configProps.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
            assertTrue(configProps.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Handle empty bootstrap servers : Success")
        void givenEmptyBootstrapServers_whenCreateProducerFactory_thenConfigurationIsSet() {
            String emptyServers = "";
            ReflectionTestUtils.setField(kafkaProducerConfig, "kafkaBootstrapServers", emptyServers);

            ProducerFactory<String, EventDto> producerFactory = kafkaProducerConfig.producerFactory();

            Map<String, Object> configProps = producerFactory.getConfigurationProperties();
            assertEquals(emptyServers, configProps.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }
    }
}
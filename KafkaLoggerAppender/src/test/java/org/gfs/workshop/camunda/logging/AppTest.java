package org.gfs.workshop.camunda.logging;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.gfs.workshop.camunda.logging.schema.LogRecord;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Collections;
import java.util.Map;

@SpringBootTest(classes = {
        KafkaLoggingAutoConfiguration.class,
        KafkaAutoConfiguration.class
})
@EmbeddedKafka(partitions = 1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppTest {
    @Autowired
    private KafkaProperties kafkaProperties;
    @Autowired
    private KafkaTemplate<String, LogRecord> kafkaTemplate;

    @Test
    @Order(1)
    public void testContext() {
    }

    @Test
    @Order(2)
    public void testProperties() {
        assert kafkaProperties != null;
    }

    @Test
    @Order(3)
    public void checkKafkaProducer() {
        Map<String, Object> consumerConfig = kafkaProperties.buildConsumerProperties(null);
        consumerConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaProperties.getProperties().get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "component-test");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaConsumer<String, LogRecord> consumer = new KafkaConsumer<>(consumerConfig);
        consumer.subscribe(Collections.singleton(kafkaTemplate.getDefaultTopic()));
        assert KafkaTestUtils.getRecords(
                consumer) != null;
    }

}

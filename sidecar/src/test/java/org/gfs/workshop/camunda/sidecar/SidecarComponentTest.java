package org.gfs.workshop.camunda.sidecar;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.camunda.kafka.CamundaRequestEvent;
import org.camunda.kafka.FeedbackRecord;
import org.camunda.kafka.FeedbackTypeEnum;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class SidecarComponentTest {

    @LocalServerPort
    private int port;

    @Value("${camunda.server.url}")
    private String camundaServerUrl;

    @Value("${kafka.request-topic}")
    private String requestTopic;

    @Autowired
    private KafkaProperties kafkaProperties;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @Order(1)
    public void createCamundaEvent() {
        CamundaRequestEvent requestEvent = CamundaRequestEvent.newBuilder()
                .setWorkflowId(UUID.randomUUID().toString())
                .setTaskId(UUID.randomUUID().toString())
                .setFeedbackRequired(true)
                .setFeedback(FeedbackRecord.newBuilder()
                        .setFeedbackEvent("AuthorizedByOtherDevice")
                        .setFeedbackType(FeedbackTypeEnum.MESSAGE)
                        .build())
                .setData(Map.of("author", "gandelfwiz"))
                .build();
        RestTemplate restTemplate = new AppConfig().restTemplate();
        restTemplate.exchange(
                "http://localhost:" + port + "/kafka/events/camunda/publishing",
                HttpMethod.POST,
                new HttpEntity<>(requestEvent),
                Void.class
        ).getBody();

        Map<String, Object> config = kafkaProperties.buildConsumerProperties(null);
        config.put("group.id", "test");
        config.put("auto.offset.reset", "earliest");
        Consumer<String, CamundaRequestEvent> consumer = new KafkaConsumer<>(config);
        consumer.subscribe(Collections.singleton(requestTopic));
        ConsumerRecord<String, CamundaRequestEvent> eventConsumer =KafkaTestUtils.getSingleRecord(consumer, requestTopic);
        assertNotNull(eventConsumer);
        assertEquals(requestEvent, eventConsumer.value());
    }

    @Test
    @Order(2)
    public void testFeedback() throws InterruptedException {
        Thread.sleep(5000);
        Mockito.verify(restTemplate, Mockito.times(1)).postForObject(anyString(), any(), any());
    }
}

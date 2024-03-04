package org.gfs.workshop.camunda.sidecar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.camunda.kafka.CamundaFeedbackEvent;
import org.camunda.kafka.CamundaRequestEvent;
import org.camunda.kafka.ResultEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Profile("OUTGOING")
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    @Value("${kafka.request-topic}")
    private String requestTopic;

    @Value("${kafka.feedback-topic}")
    private String feedbackTopic;

    /**
     * Implement /kafka/events/camunda/publishing
     *
     * @param requestEvent a CamundaRequestEvent
     */
    public void publishCamundaEvent(CamundaRequestEvent requestEvent) {
        sendEvent(requestTopic, requestEvent);
    }

    /**
     * Implement /kafka/events/feedback/publishing.
     * This endpoint is useful to simulate the other device publishing
     *
     * @param requestEvent a feedback event for Camunda
     */
    public void publishFeedbackForCamundaEvent(CamundaFeedbackEvent requestEvent) {
        sendEvent(feedbackTopic, requestEvent);
    }

    /**
     * Generic method to send kafka message
     *
     * @param topic  topic name
     * @param record avro object
     */
    private void sendEvent(String topic, SpecificRecord record) {
        log.info("Sending message to kafka {}", record);
        CompletableFuture<SendResult<String, SpecificRecord>> sendingResult =
                kafkaTemplate.send(topic, record);

        sendingResult.whenComplete((result, exception) -> {
            if (Objects.isNull(exception)) {
                final RecordMetadata metadata = result.getRecordMetadata();
                log.info("Message successfully sent at {} _ {} bytes to topic {}.", metadata.timestamp(), metadata.serializedValueSize() + metadata.serializedKeySize(), metadata.topic());
            } else {
                log.error("Exception producing message {}", exception.toString());
            }
        });
    }

    /**
     * Message listener of Request coming from camunda. This is a mock of third party service.
     * Wait 5 seconds and send a response.
     *
     * @param requestEvent intercept request coming from camunda
     */
    @KafkaListener(topics = "${kafka.request-topic}", autoStartup = "${kafka.autostartup}")
    public void mockRequestHandler(CamundaRequestEvent requestEvent) {
        log.info("** MOCK SERVICE ** - Received message {}", requestEvent);
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(5000);
                CamundaFeedbackEvent.Builder feedbackEvent = CamundaFeedbackEvent
                        .newBuilder()
                        .setComponentName("sidecar")
                        .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .setResult(ResultEnum.OK)
                        .setTaskId(requestEvent.getTaskId())
                        .setWorkflowId(requestEvent.getWorkflowId())
                        .setData(requestEvent.getData());
                if (requestEvent.getFeedbackRequired()) {
                    feedbackEvent.setFeedback(requestEvent.getFeedback());
                }
                publishFeedbackForCamundaEvent(feedbackEvent.build());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

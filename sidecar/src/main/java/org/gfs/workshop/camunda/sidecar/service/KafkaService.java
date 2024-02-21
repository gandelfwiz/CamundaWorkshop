package org.gfs.workshop.camunda.sidecar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.camunda.kafka.CamundaFeedbackEvent;
import org.camunda.kafka.CamundaRequestEvent;
import org.camunda.kafka.ResultEnum;
import org.gfs.workshop.camunda.sidecar.model.camunda.CorrelationMessageDto;
import org.gfs.workshop.camunda.sidecar.model.camunda.MessageCorrelationResultWithVariableDto;
import org.gfs.workshop.camunda.sidecar.model.camunda.SignalDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final Function<CamundaFeedbackEvent, CorrelationMessageDto> mapFeedbackEventToCorrelationMessage =
            feedbackEvent ->
                    CorrelationMessageDto.builder()
                            .messageName(feedbackEvent.getFeedback().getFeedbackEvent())
                            .processInstanceId(feedbackEvent.getWorkflowId())
                            .build();

    private final Function<CamundaFeedbackEvent, SignalDto> mapFeedbackEventToCorrelationSignal =
            feedbackEvent ->
                    SignalDto.builder()
                            .name(feedbackEvent.getFeedback().getFeedbackEvent())
                            .build();
    @Value("${camunda.server.url}")
    private String camundaServerUrl;

    @Value("${kafka.request-topic}")
    private String requestTopic;

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
        sendEvent("workshop_camunda_feedback_topic", requestEvent);
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
     * Message listener of Feedbacks. Call Camunda only when a feedback is provided
     *
     * @param feedbackEvent feedback event for Camunda received from topic
     */
    @KafkaListener(topics = "${kafka.feedback-topic}")
    public void feedbackHandler(CamundaFeedbackEvent feedbackEvent) {
        log.info("Received message {}", feedbackEvent);
        if (Objects.nonNull(feedbackEvent.getFeedback())) {
            switch (feedbackEvent.getFeedback().getFeedbackType()) {
                case SIGNAL -> callCamunda(mapFeedbackEventToCorrelationSignal.apply(feedbackEvent),
                        Void.class);
                case MESSAGE -> callCamunda(mapFeedbackEventToCorrelationMessage.apply(feedbackEvent),
                        MessageCorrelationResultWithVariableDto.class);
            }
        }
    }


    /**
     * Message listener of Request coming from camunda. This is a mock of third party service.
     * Wait 5 seconds and send a response.
     *
     * @param requestEvent intercept request coming from camunda
     */
    @KafkaListener(topics = "${kafka.request-topic}")
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

    private <T, S> void callCamunda(T messageDto, Class<S> responseType) {
        log.info("Correlate message to Camunda {}", messageDto);
        try {
            restTemplate.postForObject(
                    camundaServerUrl + "/message",
                    messageDto,
                    responseType
            );
        } catch (Exception e) {
            log.error("Exception calling Camunda engine: ", e);
        }
    }
}

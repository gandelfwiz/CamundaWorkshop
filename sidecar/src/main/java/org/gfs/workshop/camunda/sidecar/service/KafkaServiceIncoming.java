package org.gfs.workshop.camunda.sidecar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.kafka.CamundaFeedbackEvent;
import org.gfs.workshop.camunda.sidecar.model.camunda.CorrelationMessageDto;
import org.gfs.workshop.camunda.sidecar.model.camunda.MessageCorrelationResultWithVariableDto;
import org.gfs.workshop.camunda.sidecar.model.camunda.SignalDto;
import org.gfs.workshop.camunda.sidecar.model.workflow.TriggerType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Function;

@Service
@Profile("INCOMING")
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceIncoming {

    private final CamundaService camundaService;

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

    @Value("${kafka.feedback-topic:}")
    private String feedbackTopic;

    /**
     * Message listener of Feedbacks. Call Camunda only when a feedback is provided
     *
     * @param feedbackEvent feedback event for Camunda received from topic
     */
    @KafkaListener(topics = "${kafka.feedback-topic}", autoStartup = "${kafka.autostartup}")
    public void feedbackHandler(CamundaFeedbackEvent feedbackEvent) {
        log.info("Received message {}", feedbackEvent);
        if (Objects.nonNull(feedbackEvent.getFeedback())) {
            switch (feedbackEvent.getFeedback().getFeedbackType()) {
                case SIGNAL -> camundaService.correlate(mapFeedbackEventToCorrelationSignal.apply(feedbackEvent),
                        Void.class,
                        TriggerType.valueOf(feedbackEvent.getFeedback().getFeedbackType().toString()));
                case MESSAGE -> camundaService.correlate(mapFeedbackEventToCorrelationMessage.apply(feedbackEvent),
                        MessageCorrelationResultWithVariableDto.class,
                        TriggerType.valueOf(feedbackEvent.getFeedback().getFeedbackType().toString()));
            }
        }
    }


}

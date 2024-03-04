package org.gfs.workshop.camunda.sidecar.controller;

import lombok.AllArgsConstructor;
import org.camunda.kafka.CamundaFeedbackEvent;
import org.camunda.kafka.CamundaRequestEvent;
import org.gfs.workshop.camunda.sidecar.service.KafkaService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Profile("OUTGOING")
@RequestMapping("/kafka")
@AllArgsConstructor
public class KafkaController {
    private final KafkaService kafkaService;

    @PostMapping("/events/camunda/publishing")
    public ResponseEntity<Void> publishCamundaEvent(@RequestBody CamundaRequestEvent requestEvent) {
        kafkaService.publishCamundaEvent(requestEvent);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/events/feedback/publishing")
    public ResponseEntity<Void> publishFeedbackForCamunda(@RequestBody CamundaFeedbackEvent requestEvent) {
        kafkaService.publishFeedbackForCamundaEvent(requestEvent);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

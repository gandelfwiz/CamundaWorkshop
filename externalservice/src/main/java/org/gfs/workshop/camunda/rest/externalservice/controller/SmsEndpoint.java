package org.gfs.workshop.camunda.rest.externalservice.controller;

import org.gfs.workshop.camunda.rest.externalservice.model.Receiver;
import org.gfs.workshop.camunda.rest.externalservice.model.Result;
import org.gfs.workshop.camunda.rest.externalservice.service.SmsService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/external-service")
@AllArgsConstructor
public class SmsEndpoint {
    private final SmsService smsService;
    @PostMapping(value = "/sms", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result> sendSms(@RequestBody Receiver receiver) {
        Result result = smsService.sendSms(receiver);
        if (result.sendingResult()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}

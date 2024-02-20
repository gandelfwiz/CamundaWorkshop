package org.gfs.workshop.camunda.rest.externalservice.service;

import org.gfs.workshop.camunda.rest.externalservice.model.Receiver;
import org.gfs.workshop.camunda.rest.externalservice.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class SmsService {

    public Result sendSms(Receiver receiver) {
        log.info("Send sms. Message is: {}", receiver.message());
        if (Objects.isNull(receiver.phoneNumber())) {
            return new Result(false, "Phone number is required");
        }
        return new Result(true, null);
    }
}

package org.gfs.workshop.camunda.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import lombok.extern.slf4j.Slf4j;
import org.gfs.workshop.camunda.logging.schema.LogRecord;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.concurrent.Executors;

@Slf4j
public class KafkaLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final KafkaTemplate<String, LogRecord> kafkaTemplate;

    public KafkaLogAppender(KafkaTemplate<String, LogRecord> template, Logger logger) {
        kafkaTemplate = template;
        setName(logger.getName());
        setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        LogRecord logRecord = LogRecord.newBuilder()
                .setLevel(iLoggingEvent.getLevel().toString())
                .setLoggerName(iLoggingEvent.getLoggerName())
                .setThread(iLoggingEvent.getThreadName())
                .setTimestamp(iLoggingEvent.getTimeStamp())
                .setMessage(iLoggingEvent.getMessage())
                .setStackTrace(Arrays.toString(iLoggingEvent.getCallerData()))
                .build();
        log.trace("Appending message kafka on topic {}:\n {}", kafkaTemplate.getDefaultTopic(), logRecord);
        Executors.newSingleThreadExecutor().submit(() -> {
            kafkaTemplate.send(kafkaTemplate.getDefaultTopic(), 0, null, logRecord)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.trace("Sent message=[" + logRecord +
                                    "] with offset=[" + result.getRecordMetadata().offset() + "]");
                        } else {
                            log.trace("Unable to send message=[" +
                                    logRecord + "] due to : " + ex.getMessage());
                        }
                    });
        });
    }
}


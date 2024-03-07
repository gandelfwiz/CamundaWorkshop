package org.gfs.workshop.camunda.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gfs.workshop.camunda.logging.schema.LogRecord;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@AutoConfiguration
@AllArgsConstructor
@Slf4j
@ComponentScan
public class KafkaLoggingAutoConfiguration {
    private final KafkaTemplate<String, LogRecord> kafkaTemplate;

    @PostConstruct
    public void startKafkaAppender() {
        log.info("**** KAFKA LOGGER APPENDER INITIALIZATION ****");
        Logger rootLogger = (Logger) LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> kafkaAppender = new KafkaLogAppender(kafkaTemplate, rootLogger);
        kafkaAppender.start();

        rootLogger.addAppender(kafkaAppender);

        Map<String, Appender<ILoggingEvent>> appendersMap = new HashMap<>();
        rootLogger.getLoggerContext().getLoggerList()
                .forEach(logger ->
                        logger.iteratorForAppenders()
                                .forEachRemaining(appender -> appendersMap.put(appender.getName(), appender)));
        log.info("Appenders: {}", appendersMap.entrySet().stream()
                .map(appender -> "\n" + appender.getKey() + " --> " + appender.getValue().isStarted())
                .collect(Collectors.joining(",")));
        log.info("KafkaAppender configuration \n{}\n{}",
                kafkaTemplate.getDefaultTopic(),
                kafkaTemplate.getProducerFactory().getConfigurationProperties());
    }

}

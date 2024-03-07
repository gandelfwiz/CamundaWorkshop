package org.gfs.workshop.camunda.logging;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;


@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
@PropertySource(value = "classpath:kafka-appender.yaml", factory = YamlPropertySourceFactory.class)
@Primary
@Data
public class AppenderKafkaProperties  {

}

package org.gfs.workshop.camunda.sidecar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    /**
     * Used to avoid to serialize schema and specificData fields of Avro generated objects
     * that make serialization fail.
     */
    public interface IgnoreAvroSchemaProperty {
        @JsonIgnore
        void getSchema();

        @JsonIgnore
        void getSpecificData();
    }

    /**
     * Custom objectMapper patched with a mixin with the Interface IgnoreAvroSchemaProperty
     *
     * @return patched object mapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .addMixIn(SpecificRecord.class, IgnoreAvroSchemaProperty.class);
    }

    /**
     * Create rest template with custom object mapper (by default it would create a new
     * object mapper instance that would fail treating SpecificRecord objects)
     *
     * @return rest template with custom object mapper
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
        template.getMessageConverters().add(0, mappingJacksonHttpMessageConverter());
        return template;
    }

    /**
     * Set custom object mapper to the Jackson converter
     *
     * @return Jackson converter with custom object mapper
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }
}

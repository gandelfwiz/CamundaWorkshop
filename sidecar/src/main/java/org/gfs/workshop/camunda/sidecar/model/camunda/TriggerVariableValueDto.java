package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.Map;


/**
 * TriggerVariableValueDto
 */
@Validated
@Data
public class TriggerVariableValueDto {
    @JsonProperty("local")
    private Boolean local;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("type")
    private String type;

    @JsonProperty("valueInfo")
    @Valid
    private Map<String, Object> valueInfo;
    
}

package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * ProcessInstanceWithVariablesDto
 */
@EqualsAndHashCode(callSuper = true)
@Validated
@Data
public class ProcessInstanceWithVariablesDto extends ProcessInstanceDto {
    @JsonProperty("variables")
    @Valid
    private Map<String, VariableValueDto> variables = null;


}

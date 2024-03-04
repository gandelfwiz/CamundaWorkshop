package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * CompleteTaskDto
 */
@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTaskDto {
    @JsonProperty("variables")
    @Valid
    private Map<String, VariableValueDto> variables;

    @JsonProperty("withVariablesInReturn")
    private Boolean withVariablesInReturn = false;

}

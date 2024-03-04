package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * StartProcessInstanceDto
 */
@Validated
@Data
@Builder
public class StartProcessInstanceDto {
    @JsonProperty("businessKey")
    private String businessKey;

    @JsonProperty("variables")
    @Valid
    private Map<String, VariableValueDto> variables;

    @JsonProperty("caseInstanceId")
    private String caseInstanceId;

    @JsonProperty("startInstructions")
    @Valid
    private List<ProcessInstanceModificationInstructionDto> startInstructions;

    @JsonProperty("skipCustomListeners")
    private Boolean skipCustomListeners;

    @JsonProperty("skipIoMappings")
    private Boolean skipIoMappings;

    @JsonProperty("withVariablesInReturn")
    private Boolean withVariablesInReturn;

}

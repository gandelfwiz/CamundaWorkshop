package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * CorrelationMessageDto
 */
@Data
@Builder
public class CorrelationMessageDto {
    private String messageName;
    private String businessKey;
    private String tenantId;
    private Boolean withoutTenantId = false;
    private String processInstanceId;
    private Map<String, VariableValueDto> correlationKeys;
    private Map<String, VariableValueDto> localCorrelationKeys;
    private Map<String, VariableValueDto> processVariables;
    private Map<String, VariableValueDto> processVariablesLocal;
    private Boolean all = false;
    private Boolean resultEnabled = false;
    private Boolean variablesInResultEnabled = false;
}

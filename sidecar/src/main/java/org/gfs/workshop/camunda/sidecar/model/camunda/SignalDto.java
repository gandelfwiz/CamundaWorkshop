package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * SignalDto
 */
@Data
@Builder
public class SignalDto {
    private String name;
    private String executionId;
    private Map<String, VariableValueDto> variables;
    private String tenantId;
    private Boolean withoutTenantId;
}

package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.Data;

/**
 * ExecutionDto
 */
@Data
public class ExecutionDto {
    private String id;
    private String processInstanceId;
    private Boolean ended;
    private String tenantId;
}

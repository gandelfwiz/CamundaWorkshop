package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.Data;

import java.util.List;

/**
 * ProcessInstanceDto
 */
@Data
public class ProcessInstanceDto {
    private String id;
    private String definitionId;
    private String businessKey;
    private String caseInstanceId;
    private Boolean ended;
    private Boolean suspended;
    private String tenantId;
    private List<AtomLink> links;
}

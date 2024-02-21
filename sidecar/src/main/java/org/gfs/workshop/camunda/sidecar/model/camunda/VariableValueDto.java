package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.Data;

import java.util.Map;

/**
 * VariableValueDto
 */
@Data
public class VariableValueDto {
    private Object value;
    private String type;
    private Map<String, Object> valueInfo;
}

package org.gfs.workshop.camunda.sidecar.model.camunda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * VariableValueDto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableValueDto {
    private Object value;
    private String type;
    private Map<String, Object> valueInfo;


}

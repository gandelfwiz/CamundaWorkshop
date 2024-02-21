package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * The processInstance property only has a value if the resultType is set to ProcessDefinition. The processInstance with the properties as described in the [get single instance](https://docs.camunda.org/manual/7.20/reference/rest/process-instance/get/) method.  The &#x60;execution&#x60; property only has a value if the resultType is set to &#x60;Execution&#x60;. The execution with the properties as described in the [get single execution](https://docs.camunda.org/manual/7.20/reference/rest/execution/get/) method.
 */
@Data
public class MessageCorrelationResultWithVariableDto {
    /**
     * Indicates if the message was correlated to a message start event or an  intermediate message catching event. In the first case, the resultType is  `ProcessDefinition` and otherwise `Execution`.
     */
    @AllArgsConstructor
    public enum ResultTypeEnum {
        EXECUTION("Execution"),
        PROCESSDEFINITION("ProcessDefinition");

        private String value;

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ResultTypeEnum fromValue(String text) {
            for (ResultTypeEnum b : ResultTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    private ResultTypeEnum resultType = null;
    private ProcessInstanceDto processInstance = null;
    private ExecutionDto execution = null;
    private Map<String, VariableValueDto> variables = null;

}

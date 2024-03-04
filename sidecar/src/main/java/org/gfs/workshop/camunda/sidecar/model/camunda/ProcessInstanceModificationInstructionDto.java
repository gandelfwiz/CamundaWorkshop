package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import org.springframework.validation.annotation.Validated;


/**
 * ProcessInstanceModificationInstructionDto
 */
@Validated
@Data
public class ProcessInstanceModificationInstructionDto {
    /**
     * **Mandatory**. One of the following values: `cancel`, `startBeforeActivity`, `startAfterActivity`, `startTransition`.  * A cancel instruction requests cancellation of a single activity instance or all instances of one activity. * A startBeforeActivity instruction requests to enter a given activity. * A startAfterActivity instruction requests to execute the single outgoing sequence flow of a given activity. * A startTransition instruction requests to execute a specific sequence flow.
     */
    public enum TypeEnum {
        CANCEL("cancel"),

        STARTBEFOREACTIVITY("startBeforeActivity"),

        STARTAFTERACTIVITY("startAfterActivity"),

        STARTTRANSITION("startTransition");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("type")
    private TypeEnum type = null;

    @JsonProperty("variables")
    private TriggerVariableValueDto variables = null;

    @JsonProperty("activityId")
    private String activityId = null;

    @JsonProperty("transitionId")
    private String transitionId = null;

    @JsonProperty("activityInstanceId")
    private String activityInstanceId = null;

    @JsonProperty("transitionInstanceId")
    private String transitionInstanceId = null;

    @JsonProperty("ancestorActivityInstanceId")
    private String ancestorActivityInstanceId = null;

    @JsonProperty("cancelCurrentActiveActivityInstances")
    private Boolean cancelCurrentActiveActivityInstances = null;
}

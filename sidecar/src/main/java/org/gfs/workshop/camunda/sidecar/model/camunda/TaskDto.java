package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;

/**
 * TaskDto
 */
@Validated
@Data
public class TaskDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("assignee")
    private String assignee;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("created")
    private OffsetDateTime created;

    @JsonProperty("lastUpdated")
    private OffsetDateTime lastUpdated;

    @JsonProperty("due")
    private OffsetDateTime due;

    @JsonProperty("followUp")
    private OffsetDateTime followUp;

    /**
     * The task's delegation state. Possible values are `PENDING` and `RESOLVED`.
     */
    @AllArgsConstructor
    public enum DelegationStateEnum {
        PENDING("PENDING"),

        RESOLVED("RESOLVED");

        private String value;

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static DelegationStateEnum fromValue(String text) {
            for (DelegationStateEnum b : DelegationStateEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("delegationState")
    private DelegationStateEnum delegationState;

    @JsonProperty("description")
    private String description;

    @JsonProperty("executionId")
    private String executionId;

    @JsonProperty("parentTaskId")
    private String parentTaskId;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("processDefinitionId")
    private String processDefinitionId;

    @JsonProperty("processInstanceId")
    private String processInstanceId;

    @JsonProperty("caseExecutionId")
    private String caseExecutionId;

    @JsonProperty("caseDefinitionId")
    private String caseDefinitionId;

    @JsonProperty("caseInstanceId")
    private String caseInstanceId;

    @JsonProperty("taskDefinitionKey")
    private String taskDefinitionKey;

    @JsonProperty("suspended")
    private Boolean suspended;

    @JsonProperty("formKey")
    private String formKey;

    @JsonProperty("camundaFormRef")
    private CamundaFormRef camundaFormRef;

    @JsonProperty("tenantId")
    private String tenantId;

}

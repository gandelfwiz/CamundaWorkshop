package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * A JSON object corresponding to the Activity Instance tree of the given process instance.
 */
@Schema(description = "A JSON object corresponding to the Activity Instance tree of the given process instance.")
@Validated
@Data
public class TransitionInstanceDto implements InstanceDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("parentActivityInstanceId")
    private String parentActivityInstanceId;

    @JsonProperty("activityId")
    private String activityId;

    @JsonProperty("activityName")
    private String activityName;

    @JsonProperty("activityType")
    private String activityType;

    @JsonProperty("processInstanceId")
    private String processInstanceId;

    @JsonProperty("processDefinitionId")
    private String processDefinitionId;

    @JsonProperty("executionId")
    private String executionId;

    @JsonProperty("incidentIds")
    @Valid
    private List<String> incidentIds;

    @JsonProperty("incidents")
    @Valid
    private List<ActivityInstanceIncidentDto> incidents;

    @Override
    public List<String> getExecutionIds() {
        return List.of(executionId);
    }
}

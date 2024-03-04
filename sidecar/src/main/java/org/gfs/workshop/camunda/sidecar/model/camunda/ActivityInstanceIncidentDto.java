package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;


/**
 * An activity instance, incident pair.
 */
@Schema(description = "An activity instance, incident pair.")
@Validated
@Data
public class ActivityInstanceIncidentDto {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("activityId")
    private String activityId = null;

}

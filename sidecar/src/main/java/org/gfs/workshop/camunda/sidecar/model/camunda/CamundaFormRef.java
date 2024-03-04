package org.gfs.workshop.camunda.sidecar.model.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * CamundaFormRef
 */
@Validated
@Data
public class CamundaFormRef {
    @JsonProperty("key")
    private String key = null;

    @JsonProperty("binding")
    private String binding = null;

    @JsonProperty("version")
    private Integer version = null;

}

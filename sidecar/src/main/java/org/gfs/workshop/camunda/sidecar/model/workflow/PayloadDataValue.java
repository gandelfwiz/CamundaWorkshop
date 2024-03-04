package org.gfs.workshop.camunda.sidecar.model.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PayloadDataValue implements Serializable {
    private Object value;

    @JsonValue
    public Object getValue() {
        return value;
    }

    @JsonCreator
    public PayloadDataValue(Object value) {
        this.value = value;
    }

}

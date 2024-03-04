package org.gfs.workshop.camunda.sidecar.model.camunda;

import java.util.List;

public interface InstanceDto {
    String getActivityType();

    List<String> getExecutionIds();
}

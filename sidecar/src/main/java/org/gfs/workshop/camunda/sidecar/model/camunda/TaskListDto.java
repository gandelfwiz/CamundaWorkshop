package org.gfs.workshop.camunda.sidecar.model.camunda;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;

/**
 * TaskDto
 */
@Validated
public class TaskListDto extends ArrayList<TaskDto> {
}

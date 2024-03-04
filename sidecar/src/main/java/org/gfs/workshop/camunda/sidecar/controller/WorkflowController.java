package org.gfs.workshop.camunda.sidecar.controller;

import lombok.RequiredArgsConstructor;
import org.gfs.workshop.camunda.sidecar.model.workflow.WorkflowInstanceDto;
import org.gfs.workshop.camunda.sidecar.model.workflow.WorkflowRequestInstanceDto;
import org.gfs.workshop.camunda.sidecar.service.WorkflowService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@Profile("INCOMING")
//@CrossOrigin(origins = "*")
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    @PostMapping(value = "/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowInstanceDto> createWorkflow(@RequestBody WorkflowRequestInstanceDto workflowRequestInstanceDto) {
        WorkflowInstanceDto response = workflowService.create(workflowRequestInstanceDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/instances/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkflowInstanceDto> updateWorkflow(@PathVariable("uuid") UUID uuid,
                                                              @RequestBody WorkflowRequestInstanceDto workflowRequestInstanceDto) {
        WorkflowInstanceDto response = workflowService.update(uuid, workflowRequestInstanceDto);
        return ResponseEntity.ok(response);
    }
}

package org.gfs.workshop.camunda.sidecar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gfs.workshop.camunda.sidecar.model.camunda.*;
import org.gfs.workshop.camunda.sidecar.model.workflow.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowService {
    private final CamundaService camundaService;

    /**
     * Service to create a new Workflow Instance.
     * @param workflowRequestInstanceDto input data and workflow type
     * @return result of first execution
     */
    public WorkflowInstanceDto create(WorkflowRequestInstanceDto workflowRequestInstanceDto) {
        return camundaService.startNewProcess(workflowRequestInstanceDto);
    }

    /**
     * Once the workflow is created you can post updates providing the instance of the process
     * and additional data or event to correlate
     * @param instanceId Instance of the process
     * @param workflowRequestInstanceDto Data
     * @return Result of execution with process variables
     */
    public WorkflowInstanceDto update(UUID instanceId, WorkflowRequestInstanceDto workflowRequestInstanceDto) {
        WorkflowInstanceDto response = new WorkflowInstanceDto().uuid(instanceId);
        return act(response, workflowRequestInstanceDto);
    }

    /**
     * Route the update process automatically to the task completion or message correlation
     * @param instance Instance of the process
     * @param workflowRequestInstanceDto Data
     * @return Result of execution with process variables
     */
    private WorkflowInstanceDto act(WorkflowInstanceDto instance, WorkflowRequestInstanceDto workflowRequestInstanceDto) {
        // 1. Retrieve the activity instances of the process instance. In this way you can find
        //    the type of task that are active and react only for the ones that can trigger a task completion,
        //    a message or a signal
        ActivityInstanceDto activity = camundaService.getActivityInstances(instance.getUuid());

        List<PayloadData> data = null;

        // 2. Look for the active element that can be triggered
        InstanceDto activeInstance = lookForTriggerInElement(activity);

        // 3. Routing of calls to Correlation of signal and message or Task completion
        if (Objects.nonNull(activeInstance)) {
            if (Objects.nonNull(workflowRequestInstanceDto.getEventName())) {
                switch (TriggerOwnedMember.getFromType(activeInstance.getActivityType()).getTriggerType()) {
                    case SIGNAL -> camundaService.correlate(
                            SignalDto.builder()
                                    .name(workflowRequestInstanceDto.getEventName())
                                    .build(),
                            Void.class,
                            TriggerType.SIGNAL);
                    case MESSAGE -> data = (camundaService.correlate(
                            CorrelationMessageDto.builder()
                                    .messageName(workflowRequestInstanceDto.getEventName())
                                    .processInstanceId(instance.getUuid().toString())
                                    .build(),
                            MessageCorrelationResultWithVariableDto.class,
                            TriggerType.MESSAGE)).getVariables()
                            .entrySet()
                            .stream()
                            .map(entry -> new PayloadData().key(entry.getKey()).value(new PayloadDataValue(entry.getValue().getValue())))
                            .collect(Collectors.toList());
                }
            } else if (TriggerType.COMPLETE.equals(TriggerOwnedMember.getFromType(activeInstance.getActivityType()).getTriggerType())) {
                data = camundaService.complete(workflowRequestInstanceDto, activeInstance.getExecutionIds(), instance.getUuid())
                        .entrySet()
                        .stream()
                        .map(entry -> new PayloadData().key(entry.getKey()).value(new PayloadDataValue(entry.getValue().getValue())))
                        .collect(Collectors.toList());
            }
        }
        return instance.data(data);
    }

    /**
     * Look for a possible activity that can trigger a message or a task completion
     * @param activity The current activity of the process instance
     * @return An instance object
     */
    private InstanceDto lookForTriggerInElement(ActivityInstanceDto activity) {
        if (Objects.isNull(activity)) return null;

        // Use an enum to define which elements can trigger a call
        if (TriggerOwnedMember.isTrigger(activity.getActivityType())) {
            return activity;
        }

        // Look into children list
        if (Objects.isNull(activity.getChildActivityInstances()) &&
                Objects.isNull(activity.getChildTransitionInstances())) {
            return null;
        }

        if (Objects.nonNull(activity.getChildTransitionInstances()) &&
                !activity.getChildTransitionInstances().isEmpty()) {
            InstanceDto instanceDto = activity.getChildTransitionInstances()
                    .stream()
                    .filter(instance -> TriggerOwnedMember.isTrigger(instance.getActivityType()))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(instanceDto)) return instanceDto;
        }

        // Search in sublist is called
        return lookForTriggerInList(activity.getChildActivityInstances());
    }

    /**
     * Look for a triggerable task or event in process instance
     * @param childTransitionInstances list of subactivities
     * @return the instance to trigger
     */
    private InstanceDto lookForTriggerInList(List<ActivityInstanceDto> childTransitionInstances) {
        return childTransitionInstances.stream()
                // call recursively the look for trigger for each element
                .map(this::lookForTriggerInElement)
                .filter(Objects::nonNull)
                .filter(instance -> TriggerOwnedMember.isTrigger(instance.getActivityType()))
                .findFirst()
                .orElse(null);
    }

}

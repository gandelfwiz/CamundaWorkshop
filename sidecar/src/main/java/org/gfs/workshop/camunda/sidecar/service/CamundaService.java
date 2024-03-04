package org.gfs.workshop.camunda.sidecar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gfs.workshop.camunda.sidecar.model.camunda.*;
import org.gfs.workshop.camunda.sidecar.model.workflow.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CamundaService {

    private final RestTemplate restTemplate;

    @Value("${camunda.server.url:}")
    private String camundaServerUrl;

    /**
     * Send message or signal to workflow
     * Rest Endpoints:
     * * POST /message
     * * POST /signal
     *
     * @param messageDto   SignalDto or MessageCorrelationDto
     * @param responseType Response object of Camunda
     * @param triggerType  Type of feedback
     */
    public <T, S> S correlate(T messageDto, Class<S> responseType, TriggerType triggerType) {
        log.info("Correlate message to Camunda {}", messageDto);
        try {
            return restTemplate.postForObject(
                    camundaServerUrl + "/" + triggerType.toString().toLowerCase(),
                    messageDto,
                    responseType
            );
        } catch (Exception e) {
            log.error("Exception calling Camunda engine: ", e);
        }
        return null;
    }

    /**
     * Create new workflow instance
     * Rest endpoint:
     * * POST /process-definition/key/{key}/start
     *
     * @param workflowRequestInstanceDto input data to start the process
     * @return instance data
     */
    public WorkflowInstanceDto startNewProcess(WorkflowRequestInstanceDto workflowRequestInstanceDto) {
        log.info("Start new process to Camunda {}", workflowRequestInstanceDto);
        StartProcessInstanceDto request = StartProcessInstanceDto.builder()
                .variables(Objects.requireNonNullElse(workflowRequestInstanceDto.getPayload(),
                                new ArrayList<PayloadData>())
                        .stream()
                        .collect(Collectors.toMap(
                                PayloadData::getKey,
                                variable -> new VariableValueDto(variable.getValue().getValue(), null, null))
                        ))
                .withVariablesInReturn(true)
                .build();
        try {
            ProcessInstanceWithVariablesDto instance = restTemplate.postForObject(
                    camundaServerUrl + "/process-definition/key/" + workflowRequestInstanceDto.getWorkflowType() + "/start",
                    new HttpEntity<>(
                            request,
                            new HttpHeaders(
                                    new LinkedMultiValueMap<>(
                                            Map.of(
                                                    HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE
                                                    )
                                            )
                                    )
                            )
                    ),
                    ProcessInstanceWithVariablesDto.class
            );
            assert instance != null;
            return new WorkflowInstanceDto()
                    .uuid(UUID.fromString(instance.getId()))
                    .data(instance.getVariables()
                            .entrySet()
                            .stream()
                            .map(entry -> new PayloadData()
                                    .key(entry.getKey())
                                    .value(new PayloadDataValue(entry.getValue().getValue()))
                            ).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Exception calling Camunda engine: ", e);
        }
        return null;
    }

    /**
     * Get the list of activity instances where the type of task is specified
     * Rest endpoint:
     * * GET /process-instance/{id}/activity-instances
     * @param uuid
     * @return
     */
    public ActivityInstanceDto getActivityInstances(UUID uuid) {
        log.info("Get activity instances {}", uuid.toString());
        try {
            return restTemplate.getForObject(
                    camundaServerUrl + "/process-instance/" + uuid + "/activity-instances",
                    ActivityInstanceDto.class
            );
        } catch (Exception e) {
            log.error("Exception calling Camunda engine: ", e);
        }
        return null;
    }

    /**
     * Complete a task
     * Rest endpoint:
     * * /task/{id}/complete
     * @param workflowRequestInstanceDto input data
     * @param executionIds list of executions to filter
     * @param uuid process instance identification code
     * @return Map of process variables after execution
     */
    public VariablesDto complete(WorkflowRequestInstanceDto workflowRequestInstanceDto, List<String> executionIds, UUID uuid) {
        VariablesDto response = null;
        log.info("Complete tasks executions {} for process Id {}", executionIds, uuid.toString());
        try {
            TaskListDto task = getTaskDtos(uuid);

            CompleteTaskDto request = CompleteTaskDto.builder()
                    .variables(workflowRequestInstanceDto.getPayload()
                            .stream()
                            .collect(Collectors.toMap(
                                    PayloadData::getKey,
                                    variable -> new VariableValueDto(variable.getValue().getValue(), null, null))
                            ))
                    .withVariablesInReturn(true)
                    .build();
            if (Objects.nonNull(task) &&
                    Objects.nonNull(executionIds) &&
                    executionIds.contains(task.get(0).getExecutionId()))
                response = restTemplate.postForObject(
                        camundaServerUrl + "/task/" + task.get(0).getId() + "/complete",
                        request,
                        VariablesDto.class
                );
        } catch (Exception e) {
            log.error("Exception calling Camunda engine: ", e);
            throw e;
        }
        return response;
    }

    /**
     * Retrieve the task to complete automatically
     * Rest endpoint:
     * * GET /task?processInstanceId={id}&active=true
     *
     * @param uuid process instance id
     * @return the active instance
     */
    private TaskListDto getTaskDtos(UUID uuid) {
        return restTemplate.getForObject(
                camundaServerUrl + "/task?processInstanceId=" + uuid + "&active=true",
                TaskListDto.class
        );
    }
}

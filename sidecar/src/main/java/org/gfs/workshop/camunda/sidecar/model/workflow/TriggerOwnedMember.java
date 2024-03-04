package org.gfs.workshop.camunda.sidecar.model.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.gfs.workshop.camunda.sidecar.model.workflow.TriggerType.*;

@AllArgsConstructor
@Getter
public enum TriggerOwnedMember {
    MULTI_INSTANCE_BODY("multiInstanceBody", null),

    //gateways //////////////////////////////////////////////

    GATEWAY_EXCLUSIVE("exclusiveGateway", null),
    GATEWAY_INCLUSIVE("inclusiveGateway", null),
    GATEWAY_PARALLEL("parallelGateway", null),
    GATEWAY_COMPLEX("complexGateway", null),
    GATEWAY_EVENT_BASED("eventBasedGateway", null),

    //tasks //////////////////////////////////////////////
    TASK("task", COMPLETE),
    TASK_SCRIPT("scriptTask", null),
    TASK_SERVICE("serviceTask", null),
    TASK_BUSINESS_RULE("businessRuleTask", null),
    TASK_MANUAL_TASK("manualTask", null),
    TASK_USER_TASK("userTask", COMPLETE),
    TASK_SEND_TASK("sendTask", null),
    TASK_RECEIVE_TASK("receiveTask", MESSAGE),

    //other ////////////////////////////////////////////////
    SUB_PROCESS("subProcess", null),
    SUB_PROCESS_AD_HOC("adHocSubProcess", null),
    CALL_ACTIVITY("callActivity", null),
    TRANSACTION("transaction", null),

    //boundary events ////////////////////////////////////////
    BOUNDARY_TIMER("boundaryTimer", null),
    BOUNDARY_MESSAGE("boundaryMessage", MESSAGE),
    BOUNDARY_SIGNAL("boundarySignal", SIGNAL),
    BOUNDARY_COMPENSATION("compensationBoundaryCatch", null),
    BOUNDARY_ERROR("boundaryError", null),
    BOUNDARY_ESCALATION("boundaryEscalation", null),
    BOUNDARY_CANCEL("cancelBoundaryCatch", null),
    BOUNDARY_CONDITIONAL("boundaryConditional", null),

    //start events ////////////////////////////////////////
    START_EVENT("startEvent", null),
    START_EVENT_TIMER("startTimerEvent", null),
    START_EVENT_MESSAGE("messageStartEvent", MESSAGE),
    START_EVENT_SIGNAL("signalStartEvent", SIGNAL),
    START_EVENT_ESCALATION("escalationStartEvent", null),
    START_EVENT_COMPENSATION("compensationStartEvent", null),
    START_EVENT_ERROR("errorStartEvent", null),
    START_EVENT_CONDITIONAL("conditionalStartEvent", null),

    //intermediate catch events ////////////////////////////////////////
    INTERMEDIATE_EVENT_CATCH("intermediateCatchEvent", null),
    INTERMEDIATE_EVENT_MESSAGE("intermediateMessageCatch", MESSAGE),
    INTERMEDIATE_EVENT_TIMER("intermediateTimer", null),
    INTERMEDIATE_EVENT_LINK("intermediateLinkCatch", null),
    INTERMEDIATE_EVENT_SIGNAL("intermediateSignalCatch", SIGNAL),
    INTERMEDIATE_EVENT_CONDITIONAL("intermediateConditional", null),

    //intermediate throw events ////////////////////////////////
    INTERMEDIATE_EVENT_THROW("intermediateThrowEvent", null),
    INTERMEDIATE_EVENT_SIGNAL_THROW("intermediateSignalThrow", null),
    INTERMEDIATE_EVENT_COMPENSATION_THROW("intermediateCompensationThrowEvent", null),
    INTERMEDIATE_EVENT_MESSAGE_THROW("intermediateMessageThrowEvent", null),
    INTERMEDIATE_EVENT_NONE_THROW("intermediateNoneThrowEvent", null),
    INTERMEDIATE_EVENT_ESCALATION_THROW("intermediateEscalationThrowEvent", null),


    //end events ////////////////////////////////////////
    END_EVENT_ERROR("errorEndEvent", null),
    END_EVENT_CANCEL("cancelEndEvent", null),
    END_EVENT_TERMINATE("terminateEndEvent", null),
    END_EVENT_MESSAGE("messageEndEvent", null),
    END_EVENT_SIGNAL("signalEndEvent",null),
    END_EVENT_COMPENSATION("compensationEndEvent", null),
    END_EVENT_ESCALATION("escalationEndEvent", null),
    END_EVENT_NONE("noneEndEvent", null);


    private final String activityType;
    private final TriggerType triggerType;

    private static final Map<String, TriggerOwnedMember> ACTIVITIES =
            Arrays.stream(TriggerOwnedMember.values())
                    .collect(Collectors.toMap(TriggerOwnedMember::getActivityType, e -> e));

    public static TriggerOwnedMember getFromType(String type) {
        return Objects.requireNonNull(ACTIVITIES.get(type));
    }

    public static boolean isTrigger(String type) {
        if (Objects.isNull(ACTIVITIES.get(type))) return false;
        return Objects.nonNull(ACTIVITIES.get(type).getTriggerType());
    }
}

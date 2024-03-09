package org.gfs.workshop.camunda.embedded.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.gfs.workshop.camunda.embedded.model.BusinessDataEntityValue;
import org.gfs.workshop.camunda.embedded.model.EnrollmentDataEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetBusinessData implements JavaDelegate {
    private final RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        log.info("Executing java delegate task getBusinessData");
        try {
            BusinessDataEntityValue businessDataEntityValue =
                    restTemplate.getForObject("http://localhost:8900/business-data/payload/" +
                            delegateExecution.getVariable("businessUuid"), BusinessDataEntityValue.class
                    );

            assert businessDataEntityValue != null;
            businessDataEntityValue.getDataList()
                    .forEach(entry -> delegateExecution.setVariableLocal(entry.getKey(), entry.getValue().getValue()));
            delegateExecution.setVariableLocal("AuthorizationTypeRequested", delegateExecution.getVariableLocal("authorizationType"));

            EnrollmentDataEntity enrollmentDataEntity =
                    restTemplate.getForObject("http://localhost:8900/business-data/enrollment/" +
                                    delegateExecution.getVariableLocal("customerId"),
                            EnrollmentDataEntity.class);

            if (Objects.nonNull(enrollmentDataEntity)) {
                delegateExecution.setVariableLocal("BiometricEnrolled", enrollmentDataEntity.getEnrolledFlg());
            } else {
                delegateExecution.setVariableLocal("BiometricEnrolled", false);
            }
        } catch (Exception e) {
            throw new org.camunda.bpm.engine.ProcessEngineException("Errore durante la chiamata REST: ${e.message}");
        }
    }
}

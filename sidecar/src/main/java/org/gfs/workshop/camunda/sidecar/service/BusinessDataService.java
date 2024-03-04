package org.gfs.workshop.camunda.sidecar.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.gfs.workshop.camunda.sidecar.model.entity.BusinessDataEntity;
import org.gfs.workshop.camunda.sidecar.model.entity.BusinessDataEntityValue;
import org.gfs.workshop.camunda.sidecar.model.entity.EnrollmentDataEntity;
import org.gfs.workshop.camunda.sidecar.model.workflow.PayloadData;
import org.gfs.workshop.camunda.sidecar.model.workflow.PayloadDataValue;
import org.gfs.workshop.camunda.sidecar.model.workflow.WorkflowRequestInstanceDto;
import org.gfs.workshop.camunda.sidecar.repository.BusinessDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessDataService {
    private final BusinessDataRepository<EnrollmentDataEntity> repositoryEnrollment;
    private final BusinessDataRepository<BusinessDataEntity> repositoryBusiness;
    private final ObjectMapper objectMapper;

    public WorkflowRequestInstanceDto wrap(WorkflowRequestInstanceDto workflowRequestInstanceDto) {
        BusinessDataEntity entity;
        try {
            entity = new BusinessDataEntity(UUID.randomUUID().toString(),
                    objectMapper.writeValueAsString(workflowRequestInstanceDto.getPayload()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        repositoryBusiness.insert(entity);
        workflowRequestInstanceDto.setPayload(
                List.of(new PayloadData()
                        .key("businessUuid")
                        .value(new PayloadDataValue(entity.getId()))
                )
        );
        return workflowRequestInstanceDto;
    }

    public EnrollmentDataEntity getEnrollment(String customerId) {
        return repositoryEnrollment.select(customerId).orElse(null);
    }

    public BusinessDataEntityValue getPayload(String id) {
        return BusinessDataEntityValue.from(repositoryBusiness.select(id).orElse(null));
    }
}

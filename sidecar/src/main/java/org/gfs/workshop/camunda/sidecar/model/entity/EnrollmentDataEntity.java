package org.gfs.workshop.camunda.sidecar.model.entity;

import lombok.Data;

@Data
public class EnrollmentDataEntity {
    private String id;
    private String customerId;
    private Boolean enrolledFlg;
}

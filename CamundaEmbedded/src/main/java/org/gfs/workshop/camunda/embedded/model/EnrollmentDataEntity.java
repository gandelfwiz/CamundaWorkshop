package org.gfs.workshop.camunda.embedded.model;

import lombok.Data;

@Data
public class EnrollmentDataEntity {
    private String id;
    private String customerId;
    private Boolean enrolledFlg;
}

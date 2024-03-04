package org.gfs.workshop.camunda.sidecar.controller;

import lombok.RequiredArgsConstructor;
import org.gfs.workshop.camunda.sidecar.model.entity.BusinessDataEntity;
import org.gfs.workshop.camunda.sidecar.model.entity.BusinessDataEntityValue;
import org.gfs.workshop.camunda.sidecar.model.entity.EnrollmentDataEntity;
import org.gfs.workshop.camunda.sidecar.service.BusinessDataService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Profile("OUTGOING")
@RequestMapping("/business-data")
@RequiredArgsConstructor
public class BusinessData {
    private final BusinessDataService service;

    @GetMapping("/enrollment/{id}")
    public ResponseEntity<EnrollmentDataEntity> getEnrollment(@PathVariable("id") String customerId) {
        return ResponseEntity.ok(service.getEnrollment(customerId));
    }

    @GetMapping("/payload/{id}")
    public ResponseEntity<BusinessDataEntityValue> getBusinessData(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.getPayload(id));
    }
}

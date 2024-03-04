package org.gfs.workshop.camunda.sidecar.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class HealthCheck {
    @GetMapping("/health/check")
    public ResponseEntity<Void> healthCheck() {
        return new ResponseEntity(HttpStatus.OK);
    }
}

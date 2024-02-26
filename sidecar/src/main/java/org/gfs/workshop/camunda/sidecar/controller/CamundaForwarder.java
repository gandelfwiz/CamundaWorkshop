package org.gfs.workshop.camunda.sidecar.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/camunda")
@RequiredArgsConstructor
public class CamundaForwarder {
    private final RestTemplate restTemplate;

    @Value("${camunda.server.url}")
    private String camundaServerUrl;

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH
    })
    public ResponseEntity<?> proxyCamundaRestApi(@RequestBody(required = false) String requestBody,
                                                      HttpServletRequest request) {
        String targetUrl = camundaServerUrl +
                "/" + Arrays.stream(request.getRequestURI().split("/"))
                .skip(2)
                .collect(Collectors.joining("/"));
        ResponseEntity<Object> response = restTemplate.exchange(targetUrl,
                HttpMethod.valueOf(request.getMethod()),
                new HttpEntity<>(requestBody),
                Object.class);
        return new ResponseEntity<>(response.getBody(), new HttpHeaders(), response.getStatusCode());

    }
}

package org.gfs.workshop.camunda.sidecar.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Profile("INCOMING")
@RequestMapping("/camunda")
@RequiredArgsConstructor
public class CamundaForwarder {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Value("${camunda.server.url}")
    private String camundaServerUrl;

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH
    })
    public ResponseEntity<?> proxyCamundaRestApi(@RequestBody(required = false) String requestBody,
                                                 HttpServletRequest request) throws JsonProcessingException {
        String targetUrl = camundaServerUrl +
                "/" + Arrays.stream(request.getRequestURI().split("/"))
                .skip(2)
                .collect(Collectors.joining("/"));

        if (Objects.nonNull(request.getQueryString())) {
            targetUrl = targetUrl + "?" + request.getQueryString();
        }

        RequestEntity.BodyBuilder requestBuilder =
                RequestEntity.method(HttpMethod.valueOf(request.getMethod()), targetUrl)
                        .header(HttpHeaders.CONTENT_TYPE, request.getHeader(HttpHeaders.CONTENT_TYPE));

        RequestEntity<?> requestEntity;

        if (Objects.nonNull(requestBody)) {
            requestEntity = requestBuilder.body(objectMapper.readValue(requestBody, Object.class));
        } else {
            requestEntity = requestBuilder.build();
        }

        ResponseEntity<Object> response = restTemplate.exchange(requestEntity, Object.class);

        return new ResponseEntity<>(response.getBody(), new HttpHeaders(), response.getStatusCode());

    }
}

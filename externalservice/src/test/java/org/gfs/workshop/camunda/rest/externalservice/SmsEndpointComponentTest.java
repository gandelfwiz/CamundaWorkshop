package org.gfs.workshop.camunda.rest.externalservice;

import org.gfs.workshop.camunda.rest.externalservice.model.Receiver;
import org.gfs.workshop.camunda.rest.externalservice.model.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmsEndpointComponentTest {

    @LocalServerPort
    int port;

    @Test
    public void runTestOk() {
        RestTemplate restTemplate = new RestTemplate();
        Result result = restTemplate.exchange(
                "http://localhost:" + port + "/external-service/sms",
                HttpMethod.POST,
                new HttpEntity<>(new Receiver("+39111223344", "Test message")),
                Result.class
        ).getBody();
        assert result != null;
        Assertions.assertTrue(result.sendingResult());
        Assertions.assertNull(result.errorMessage());
    }

    @Test
    public void runTestNotOk() {
        RestTemplate restTemplate = new RestTemplate();
        HttpClientErrorException exception = Assertions.assertThrows(HttpClientErrorException.class, () -> restTemplate.exchange(
                "http://localhost:" + port + "/external-service/sms",
                HttpMethod.POST,
                new HttpEntity<>(new Receiver(null, null)),
                Result.class
        ));
        assert exception != null;
        Assertions.assertEquals(HttpStatusCode.valueOf(400), exception.getStatusCode());
        Assertions.assertNotNull(exception.getResponseBodyAsString());
    }
}

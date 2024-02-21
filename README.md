# <span style="color:darkorange">Camunda Workshop</span> 

_<span style="color:blue">**Author**</span>: Francesco Gandelli \
<span style="color:blue">**Date**</span>: 23 February 2024_

---

## <span style="color:darkorange">Goal of the project</span>

This project aims to create a progressive implementation of a Camunda workflow
starting from a standalone/concept design solution to the implementation.

---
## <span style="color:darkorange">The proposed approach</span>

The proposed approach of this project is as follows:
- Initially design a BPM process and test it using Camunda Standalone
- Then remove business data from the Workflow
- And then connect the Workflow to external resources
- Finally create a Camunda embedded solution

---
## <span style="color:darkorange">Proposed Architecture adoption</span>

The following technologies are used inside the project:
- SpringBoot 3.1
- Java 17
- Camunda 7.20

---
## <span style="color:darkorange">Prerequisites</span>

The project is developed under Windows 11.

- Download camunda standalone: https://downloads.camunda.cloud/release/camunda-bpm/run/7.20/camunda-bpm-run-7.20.0.zip
- Download camunda modeler: https://downloads.camunda.cloud/release/camunda-modeler/5.20.0/camunda-modeler-5.20.0-win-x64.zip
- Download jdk 17+: https://adoptium.net/temurin/releases/?version=17 file .zip for windws x64
- Set up path variable. From windows search box run: 

```dos
	rundll32.exe sysdm.cpl,EditEnvironmentVariables
```

Set up **JAVA_HOME** to the jdk 17 you just downloaded.

---
## <span style="color:darkorange">Let's start</span>

&nbsp;

### **Step 1: create a simple workflow**

&nbsp;

1. Run camunda-bpm-run-7 standalone from unzipped folder:

	```dos
	start.bat
	```

2. Run camunda-modeler-5 from unzipped folder with a double click

3. Create the following easy diagram

	![Step 1 process: A simple user task not implemented](images/authorization.png)

4. Deploy it and start the process

5. Complete manually from task list the task.

&nbsp;

### **Step 2: create a variable and pass from one task to another**

&nbsp;

1. Add to the user task the following configuration:

	![Step 2 variable: Add a variable to the process](images/authorization_add_variable.png)

2. Add a script task to receive and print the variable chosen by user:

	![Step 2 script: A simple script task that prints the variable of the previous one](images/authorization_step2_process.png)

3. Configure the script task as follows:

	*Format*: groovy \
	*Type*: Inline script \
	*Script*: 
	```groovy
	println "The authorization type chosen is " + autorizationType
	```

	Pay attention that *autorizationType* is the wrong name of the variable set in the previous user task.

4. Deploy it and start the process

5. Open the task and select a value.

6. Complete the task: you will see an error. In console you can see a java stack exception.

7. You can delete the task uncompletable by rest api.

8. Correct the *groovy* script and update the name of variable as follows:

	```groovy
	println "The authorization type chosen is " + authorizationType
	```
9. Deploy again the process and start it

10. Complete the task choosing Biometric value: this time in console you can read: `The authorization type chosen is BIOMETRIC`

&nbsp;

### **Step 3: add a gateway. Create a collapsed subprocess and an extended one**

&nbsp;

1. Change the process as follow:

	![Step 3 gateway: Add gateway and 2 subprocesses](images/authorization_step3_process.png)

2. Selecting the collapsed subprocess you can enter in the subprocess. Create the following subprocess:

	![Step 3 collapsed subprocess: definition of the subprocess](images/authorization_step3_biometric_subprocess.png)

3. Let's add configuration for the gateway. The arrow `biometric` will have a condition configured as follows:

	*Type*: expression
	*Condition Expression*: `${authorizationType == "BIOMETRIC"}`
	
	The arrow password will be configured with this expression: `${authorizationType == "PASSWORD"}`

	Note that the value is the ID of the enum and not the name

4. To check that the gateway works correctly configure in subprocess the script task as follows:

	```groovy
	println "Biometric authorization push another device"
	```

5. Now deploy the process and start it from tasklist.

6. You should see the task `Select the authorization type`

7. Select `Password` and a new task `Ask for password` appear in task list

8. Select `Biometric` and in console you might read `Biometric authorization push another device`

&nbsp;


### **Step 4: loop on password validation. Boundary events**

&nbsp;

1. Change the process as follows:

	![Step 4 gateway: Add loop and boundary events](images/authorization_step4_boundaryevents.png)

2. Let's configure the task `Ask for password` with a generated task forms with just one field that we name password. Type is string

3. Then configure the task service `Validate password`. This task will be of type Expression and the expression will be as follow:

	```java
	${execution.getVariable("password").equals("111111")}
	```
 Set the name of result variable to `isValid`. This configuration set the variable isValid equals to the value of the check performed in expression.

4. Now select the *boundary conditional event* that is linked to the message. Set the condition variable name equal to `isValid`, variable events that trigger the event are `create,update` and the type of condition is the followin `Expression`:

	```java
	${isValid == true} 
	```
5. The message password is valid needs 2 configurations: the first one is the message reference. In section `Message` set PasswordIsValid as *message name*. Then in order to work the event has to be implemented to correlate the message with the BPM engine. To do this select the implementation type `Expression` and fill in the following way the expression:

	```java
	${execution.getProcessEngineServices().getRuntimeService().createMessageCorrelation("PasswordIsValid").correlateWithResult()}
	```

6. The previous configuration send and correlate the message with all camunda processes that are listening to this message. The boundary catch message linked to the arrow password is valid has to be setup in `Message` section to listent to the message PasswordIsValid

7. Finally let's look to the error case. Before to setup the error configure the subprocess as a sequencial multi-instance process. 

	![Step 4 loop: Set up the sequential multi instance](images/sequencial_multi_instance.png)

	The `Loop` marker also exists, but it is just an annotation that indicates that the task has embedded a loop, while the loop between task is performed using the `Sequential multi-instance` that is the marker with 3 horizontal parallel lines. 

	The implementation of the loop is automatically performed. You should indicate the `Loop cardinality` that in our case is equal to 3 tentatives that customer can try to input the right password; then indicate the `Completion condition` as follows:

	```java
	${execution.getVariable("isValid")==true}
	```

	In this way the loop will be ended when the password will be valid.

8. Let's configure the conditional event linked to the error password locked. The condition variable name is `isValid`, the trigger events like in the other conditional event are `create,update` and the type Expression is: 

	```java
	${isValid == false && loopCounter == 2}
	```

9. In order to throw the exception in the end event error linked to the previous conditional event we have to define the global error reference as `PasswordLocked`. 

10. To catch the error we have to configure the boundary exception event to the global error reference `PasswordLocked`. In this case is possible to indicate the variables name for the error code and message so that the next task could log the error. For this exercise we have just written a groovy script task that print in console the following message:

	```groovy
	println "Password should be blocked"
	```

11. Once all configurations are done you can deploy and test the process. If you put a password different than 111111 after 3 times you will exit with the message `Password should be blocked`, instead if you put the 111111 password the process will be completed succesfully.

&nbsp;

### **Step 5: Escalate and call external rest service**

&nbsp;

1. As first step you have to download a new spring boot project. Use the following command or paste the url in your browser:

	```
	curl "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=3.2.2&baseDir=externalservice&groupId=org.gfs.workshop.camunda.rest&artifactId=externalservice&name=externalservice&description=External%20Service%20called%20by%20Camunda%20Workflow&packageName=org.gfs.workshop.camunda.rest.externalservice&packaging=jar&javaVersion=17&dependencies=lombok,web" --output externalservice.zip
	```
	
	Then unzip the archive and open it in IntelliJ

2. Add a package controller with the following class:

	```java
	@Controller
	@RequestMapping("/external-service")
	@AllArgsConstructor
	public class SmsEndpoint {
		private final SmsService smsService;
		@PostMapping(value = "/sms", produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<Result> sendSms(@RequestBody Receiver receiver) {
			Result result = smsService.sendSms(receiver);
			if (result.sendingResult()) {
				return ResponseEntity.ok(result);
			} else {
				return ResponseEntity.badRequest().body(result);
			}
		}
	}
	```

3. Add the package model with the following records:

	```java
	public record Receiver(String phoneNumber, String message) {
	}

	public record Result(Boolean sendingResult, String errorMessage) {
	}
	```

4. Add the service under the service package:

	```java
	@Service
	@Slf4j
	public class SmsService {

		public Result sendSms(Receiver receiver) {
			log.info("Send sms. Message is: {}", receiver.message());
			if (Objects.isNull(receiver.phoneNumber())) {
				return new Result(false, "Phone number is required");
			}
			return new Result(true, null);
		}
	}
	```

5. Add the test class as follows:

	```java
	@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
	public class SmsEndpointComponentTest {

		@LocalServerPort
		int port;
		@Test
		public void runTest() {
			RestTemplate restTemplate = new RestTemplate();
			Result result = restTemplate.exchange(
					"http://localhost:"+port+"/external-service/sms",
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
	```

	and test it running `mvn test`

6. Run the application using `mvn spring-boot:run '-Dspring-boot.run.arguments="--server.port=8900"'`

7. Go to the Camunda Modeler, remove the script task. Add an event subprocess expanded that catch an escalation and call a task service to send sms. The final result should be as follows:

	![Step 5 rest call: Add event subprocess and task](images/authorization_step05_call_rest.png)

8. Configure the Escalate end event `Warn the customer` declaring the global escalation reference as `WarnCustomer` with code `warn` and set up the receive Escalate event with the same event.

9. To call the rest service that was started at step #6 configure the task Send SMS as follows:
	- Set Implementation type as `Connector` and Connector ID is `http-connector` 
	- Set the *connector inputs* variables:
		- headers: set the map entries `Content-Type: application/json` and `Accept: application/json`
		- method: set the string `POST`
		- url: set the string `http://localhost:8900/external-service/sms`
		- payload: set it as a javascript with the following source code:
			```javascript
				var payload = {
					"phoneNumber": "+39111223344",
					"message": "Send sms from Camunda Process for reason code " + execution.getVariable("escalateCode")
				}

				JSON.stringify(payload);
			```
	- Set the *connector outputs* variable with name `response` defined as expression `${S(response)}`. This will convert the response object in json using a [Camunda Spin Framework](https://docs.camunda.org/manual/7.20/reference/spin/).
	- Pass the connector response to the next task setting a variable *resultSms* as expression `${response}`
	- Add an Execution Listener to handle the status code different than 2xx as and *end* Event Type. Implement it with the following javascript:

		```javascript
		if (statusCode>299) {
			throw new org.camunda.bpm.engine.ProcessEngineException("Error sending SMS: " + response, statusCode); 
		}
		``` 

10. After this complex configuration you can configure a script to print the sms sending result in console with a script task based on the following javascript code:
	```javascript
	java.lang.System.out.println("Result of sending: " + execution.getVariable("resultSms"));
	```

11. At this point you can deploy and run the process. When you input the wrong password (different than 111111) for three times, the call will be performed and the result will be printed in console. In external service log you will see the call.

	```
	2024-02-20T11:13:55.873+01:00  INFO 26152 --- [nio-8900-exec-2] i.f.w.c.r.e.service.SmsService           : Send sms. Message is: Send sms from Camunda Process for reason code warn
	```

&nbsp;

### **Step 6: Resilience and Retry**
&nbsp;
#### **I. BPMN based**

1. It is possible to implement in BPMN the retry. Change the diagram as follows:

	![Step 6 resilience and retry](images/authorization_step06_retry_bpmn.png)

2. Set up for `Send SMS` task an execution listener for the event type `end` as javascript using the following source code:

	```javascript
	if (statusCode>299 && execution.getVariable("retryCounter")<3) {
		execution.setVariable("retryCounter", execution.getVariable("retryCounter")+1);
		throw new org.camunda.bpm.engine.delegate.BpmnError("restError");
	}
	```

3. Then add an execution listener also to the Escalate catch event to initialize the counter. Again add the listener with type `end` as javascript using the following source code:

	```javascript
	execution.setVariable("retryCounter",0);
	```

4. Set the timer duration to `PT5S` for 5 seconds

5. Set up the event error boundary catch for an error with the following configuration:
	- Name: `RestError`
	- Code: `restError` - Pay attention that this is the error raised by script

6. Now deploy the process and try it. Run the external service to send sms you created in previous step. Then Start the process and put the wrong password (different than 111111) three times. The process will be completed and the message you can see in console is: 
	```
	Tentative #0
	Result of sending: {"sendingResult":true,"errorMessage":null}
	```
	The call was executed successfully so the system completed the process.

7. Now change in task `Send SMS` the script of payload. Set the `"phoneNumber"` Json property equal to null:

	```json
	{
		"phoneNumber": null,
		"message": "Send sms from Camunda Process for reason code " + execution.getVariable("escalateCode")
	}
	```

	and deploy the process again. Start the process and put 3 times the wrong password. The external service will return a statusCode `400 - Bad request` and you can see in *Cockpit* application the process that is running the timer. If you look the console of external service you can see that every 5 seconds it receives a call. After 3 tentatives the Camunda console will print:

	```
	Tentative #0
	Result of sending: {"sendingResult":false,"errorMessage":"Phone number is required"}
	```

&nbsp;
#### **II. Camunda engine based**


>**NOTE:** *It is possible to implement the retry using the automatic retry of asynchronous task of Camunda. Make explicit in BPMN the retry behavior can bring advantages for clarity, but at the same time could bring some disadvantages. Consider that desribe a technical retry is not business oriented and that the diagram increase its complexity in reading. In addition to this there is a limit in Camunda 7 for which if a service is unavailable the task exit before to evaluate executionListener and a manual incident is opened automatically, so in those cases the BPMN retry wouldn't be performed. 
For this reason the Camunda Engine approach is preferrable. If you Start back from the diagram of Step 5 you have just few steps to implement the retry.*

1.  Set up the asynchronous continuation of `Send SMS` task. Activate the checkbox under *Asynchronous continuations* `Before` and `Exclusive` and put the following *Job execution - Retry time cycle* expression: `R3/PT5S`. This configuration makes starting a new thread to execute the call and if it fails it retry automatically. In this way even if the service is unavailable the retry will be executed.

2. Add the *Connector output* variable `statusCode` set with the expression `${statusCode}`. 

3. Add the *Execution listeners* script for *end event* as a javascript with the following source code to force the error when the response is a logical failure:

	```javascript
	if (statusCode>299) {
    	throw new org.apache.http.HttpException("Error sending sms : " + response); 
	}
	```

4. Finally to test it set the `"phoneNumber"` Json property in task `Send SMS` to null in the script of payload:

	```json
	{
		"phoneNumber": null,
		"message": "Send sms from Camunda Process for reason code " + execution.getVariable("escalateCode")
	}
	```

5. Deploy the process and test it. When you will put for 3 times the wrong password the system will run an automatic retry.

&nbsp;

### **Step 7: Kafka integration**

>*Camunda 8 provides a native kafka-connector. Camunda 7 instead doesn't provide such connector, but in any case a connector in Camunda 7 is useful just to send message to external assets.\
The choice in this project is instead to create a Camunda sidecar. This approach enables:*\
*- the technology independency*\
*- the correlation of messages with Camunda*\
*- the support of a service mesh*\
*- the delegation of any security policy*

1. Create a new repository named `sidecar` using this command or copying in your browser just the url:

	```
	curl https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=3.2.2&baseDir=sidecar&groupId=org.gfs.workshop.camunda&artifactId=sidecar&name=sidecar&description=Sidecar%20for%20Kafka%20integration&packageName=org.gfs.workshop.camunda.sidecar&packaging=jar&javaVersion=17&dependencies=lombok,web,kafka
	```

2. Adjust pom.xml in this way to support avro schema:
	- Add the properties:
		```xml
		<kafka-avro-serializer.version>7.5.1</kafka-avro-serializer.version>
		<avro-maven-plugin.version>1.11.3</avro-maven-plugin.version>
		```
	- Add the dependencies for confluent:
		```xml
		<dependency>
			<groupId>io.confluent</groupId>
			<artifactId>common-config</artifactId>
			<version>7.4.0</version>
		</dependency>

		<dependency>
			<groupId>io.confluent</groupId>
			<artifactId>kafka-avro-serializer</artifactId>
			<version>${kafka-avro-serializer.version}</version>
			<exclusions>
				<exclusion>
					<groupId>org.apache.kafka</groupId>
					<artifactId>kafka-clients</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
		```
	- Add the plugin for avro schema (if you use avro schema in your kafka instance):
		```xml
		<plugin>
			<groupId>org.apache.avro</groupId>
			<artifactId>avro-maven-plugin</artifactId>
			<version>${avro-maven-plugin.version}</version>
			<executions>
				<execution>
					<phase>generate-sources</phase>
					<goals>
						<goal>schema</goal>
					</goals>
					<configuration>
						<stringType>String</stringType>
						<sourceDirectory>${project.basedir}/src/main/resources/avro/</sourceDirectory>
					</configuration>
				</execution>
			</executions>
		</plugin>
		```

3. Add avro schemas in src/main/resources/avro and remember to register them into Kafka Schema Registry. The avro object will be created automatically in `target/generated-sources` by the `maven-avro-plugin`.
	Avro schema is the contract between Camunda and Other External service.
	The json that Camunda send for a **request** to an external is defined in the project as follows:

	`CamundaRequestEvent`
	```json
	{
		"workflowId": "da2fd17b-0d0f-4d00-b88e-13d0361073c1",
		"taskId": "7055fb17-b008-4142-98f2-dcf9f4d13dc2",
		"feedbackRequired": true,
		"feedback": {
			"feedbackEvent": "AuthorizedByOtherDevice",
			"feedbackType": "MESSAGE"
		},
		"data": {
			"author": "gandelfwiz"
		}
	}
	```
	The idea is that Camunda provides keys of its process and ask for a feedback if the process subsequently will wait it. Additional properties can be put in `data` object that represent the business/audit data. The property `feedbackRequired` will communicate to the external service whether a feedback is expected or not.

	The feedback event will be similar, but will add the result of external service.

	`CamundaFeedbackEvent`
	```json
	{
		"workflowId": "7b17db81-d0d0-11ee-a84d-581cf8936878",
		"taskId": "7055fb17-b008-4142-98f2-dcf9f4d13dc2",
		"result": "OK",
		"timestamp": "2023-01-23T01:03:10",
		"componentName": "curl",
		"feedback": {
			"feedbackEvent": "AuthorizedByOtherDevice",
			"feedbackType": "MESSAGE"
		},
		"data": {
			"author": "gandelfwiz"
		}
	}
	```

	Obviously the contract is defined arbitrarily for the scope of this project. It is possible to create any event based on the needs and rules that in a project are defined.

	The following files and schemas should be added:

	File: `camunda_request.avsc`
	```json
	{
		"name": "CamundaRequestEvent",
		"type": "record",
		"doc": "Event used by Camunda to request an action to other applications",
		"namespace": "org.camunda.kafka",
		"fields": [
			{
				"name": "workflowId",
				"type": "string"
			},
			{
				"name": "taskId",
				"type": "string"
			},
			{
				"name": "feedbackRequired",
				"type": "boolean"
			},
			{
				"name": "feedback",
				"type": [
					"null",
					{
						"name": "FeedbackRecord",
						"type": "record",
						"fields": [
							{
							"name": "feedbackEvent",
							"type": "string"
							},
							{
								"name": "feedbackType",
								"type": {
									"type": "enum",
									"symbols": [
									"SIGNAL",
									"MESSAGE"
									],
									"name": "FeedbackTypeEnum"
								}
							}
						]
					}
				],
			"default": null
			},
			{
				"name": "data",
				"type": {
					"type": "map",
					"values": "string"
				}
			}
		]
	}
	```

	File: `camunda_feedback.avsc`
	```json
	{
		"name": "CamundaFeedbackEvent",
		"type": "record",
		"doc": "Event used to trigger a feedback to Camunda",
		"namespace": "org.camunda.kafka",
		"fields": [
			{
				"name": "workflowId",
				"type": "string"
			},
			{
				"name": "taskId",
				"type": "string"
			},
			{
				"name": "result",
				"type": {
					"name": "ResultEnum",
					"type": "enum",
					"symbols": [
					"OK",
					"NOK"
					]
			}
			},
			{
				"name": "timestamp",
				"type": "string"
			},
			{
				"name": "componentName",
				"type": "string"
			},
			{
			"name": "Feedback",
			"type": [
				"null",
				{
					"name": "FeedbackRecord",
					"type": "record",
					"doc": "Schema used to define feedback",
					"fields": [
						{
						"name": "feedbackEvent",
						"type": "string"
						},
						{
							"name": "feedbackType",
							"type": {
								"type": "enum",
								"symbols": [
								"SIGNAL",
								"MESSAGE"
								],
								"name": "FeedbackTypeEnum"
							}
						}
					]
				}
			],
			"default": null
			},
			{
				"name": "data",
				"type": {
					"type": "map",
					"values": "string"
				}
			}
		]
	}
	```
4. Rename `application.properties` to `application.yaml` and paste the following kafka configuration:

	```yaml
	kafka:
	  request-topic: workshop_camunda_request_topic
	  feedback-topic: workshop_camunda_feedback_topic

	spring:
	  kafka:
		bootstrap-servers: localhost:9092
		properties:
		  schema:
			registry:
			  url: http://localhost:8081
			  #ssl:
				#keystore:
				# location: src/main/resources/cert.jks
				# password: changeit
				#truststore:
				#  location: src/main/resources/cert.jks
				#  password: changeit
				#key:
				#  password: changeit
		  spring.deserializer.key.delegate.class: org.apache.kafka.common.serialization.StringDeserializer
		  spring.deserializer.value.delegate.class: io.confluent.kafka.serializers.KafkaAvroDeserializer
		  specific.avro.reader: true
		producer:
		  key-serializer: org.apache.kafka.common.serialization.StringSerializer
		  value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
		consumer:
		  key-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
		  value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
		  group-id: camunda-sidecar-local
		  auto-offset-reset: earliest
		auto.register.schema: false
	camunda:
	  server:
		url: http://localhost:8080/engine-rest
	```

5. Setup the project as follows:
	* `controller` package with java class `KafkaController`

		```java
		@Controller
		@RequestMapping("/kafka")
		@AllArgsConstructor
		public class KafkaController {
			private final KafkaService kafkaService;

			@PostMapping("/events/camunda/publishing")
			public ResponseEntity<Void> publishCamundaEvent(@RequestBody CamundaRequestEvent requestEvent) {
				kafkaService.publishCamundaEvent(requestEvent);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			@PostMapping("/events/feedback/publishing")
			public ResponseEntity<Void> publishFeedbackForCamunda(@RequestBody CamundaFeedbackEvent requestEvent) {
				kafkaService.publishFeedbackForCamundaEvent(requestEvent);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

		}			
		```
	* `service` package with java class `KafkaService` that:
		* implement kafka publishing
		* implement the REST call to Camunda for the feedback. The REST API used are: 
			- POST /message
			- POST /signal
		* implement 2 KafkaListeners: one for feedback and one to implement a mock of 
			camunda request. The mock will wait 5 seconds and then will publish the feedback
			that the kafka listener of feedback will forward to Camunda using Rest API

		```java
		@Service
		@RequiredArgsConstructor
		@Slf4j
		public class KafkaService {

			private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
			private final RestTemplate restTemplate;
			private final Function<CamundaFeedbackEvent, CorrelationMessageDto> mapFeedbackEventToCorrelationMessage =
					feedbackEvent ->
							CorrelationMessageDto.builder()
									.messageName(feedbackEvent.getFeedback().getFeedbackEvent())
									.processInstanceId(feedbackEvent.getWorkflowId())
									.build();

			private final Function<CamundaFeedbackEvent, SignalDto> mapFeedbackEventToCorrelationSignal =
					feedbackEvent ->
							SignalDto.builder()
									.name(feedbackEvent.getFeedback().getFeedbackEvent())
									.build();
			@Value("${camunda.server.url}")
			private String camundaServerUrl;

			@Value("${kafka.request-topic}")
			private String requestTopic;

			/**
			* Implement /kafka/events/camunda/publishing
			*
			* @param requestEvent a CamundaRequestEvent
			*/
			public void publishCamundaEvent(CamundaRequestEvent requestEvent) {
				sendEvent(requestTopic, requestEvent);
			}

			/**
			* Implement /kafka/events/feedback/publishing.
			* This endpoint is useful to simulate the other device publishing
			*
			* @param requestEvent a feedback event for Camunda
			*/
			public void publishFeedbackForCamundaEvent(CamundaFeedbackEvent requestEvent) {
				sendEvent("workshop_camunda_feedback_topic", requestEvent);
			}

			/**
			* Generic method to send kafka message
			*
			* @param topic  topic name
			* @param record avro object
			*/
			private void sendEvent(String topic, SpecificRecord record) {
				log.info("Sending message to kafka {}", record);
				CompletableFuture<SendResult<String, SpecificRecord>> sendingResult =
						kafkaTemplate.send(topic, record);

				sendingResult.whenComplete((result, exception) -> {
					if (Objects.isNull(exception)) {
						final RecordMetadata metadata = result.getRecordMetadata();
						log.info("Message successfully sent at {} _ {} bytes to topic {}.", metadata.timestamp(), metadata.serializedValueSize() + metadata.serializedKeySize(), metadata.topic());
					} else {
						log.error("Exception producing message {}", exception.toString());
					}
				});
			}

			/**
			* Message listener of Feedbacks. Call Camunda only when a feedback is provided
			*
			* @param feedbackEvent feedback event for Camunda received from topic
			*/
			@KafkaListener(topics = "${kafka.feedback-topic}")
			public void feedbackHandler(CamundaFeedbackEvent feedbackEvent) {
				log.info("Received message {}", feedbackEvent);
				if (Objects.nonNull(feedbackEvent.getFeedback())) {
					switch (feedbackEvent.getFeedback().getFeedbackType()) {
						case SIGNAL -> callCamunda(mapFeedbackEventToCorrelationSignal.apply(feedbackEvent),
								Void.class);
						case MESSAGE -> callCamunda(mapFeedbackEventToCorrelationMessage.apply(feedbackEvent),
								MessageCorrelationResultWithVariableDto.class);
					}
				}
			}


			/**
			* Message listener of Request coming from camunda. This is a mock of third party service.
			* Wait 5 seconds and send a response.
			*
			* @param requestEvent intercept request coming from camunda
			*/
			@KafkaListener(topics = "${kafka.request-topic}")
			public void mockRequestHandler(CamundaRequestEvent requestEvent) {
				log.info("** MOCK SERVICE ** - Received message {}", requestEvent);
				CompletableFuture.runAsync(() -> {
					try {
						Thread.sleep(5000);
						CamundaFeedbackEvent.Builder feedbackEvent = CamundaFeedbackEvent
								.newBuilder()
								.setComponentName("sidecar")
								.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
								.setResult(ResultEnum.OK)
								.setTaskId(requestEvent.getTaskId())
								.setWorkflowId(requestEvent.getWorkflowId())
								.setData(requestEvent.getData());
						if (requestEvent.getFeedbackRequired()) {
							feedbackEvent.setFeedback(requestEvent.getFeedback());
						}
						publishFeedbackForCamundaEvent(feedbackEvent.build());
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				});
			}
			
			// private method to call Camunda through RestTemplate
			private <T, S> void callCamunda(T messageDto, Class<S> responseType) {
				log.info("Correlate message to Camunda {}", messageDto);
				try {
					restTemplate.postForObject(
							camundaServerUrl + "/message",
							messageDto,
							responseType
					);
				} catch (Exception e) {
					log.error("Exception calling Camunda engine: ", e);
				}
			}
		}
		```

	* in sidecar package add the following configuration class that:
		* make avro object jackson-compliant
		* define rest template with custom object mapper

		```java
		@Configuration
		public class AppConfig {

			/**
			* Used to avoid to serialize schema and specificData fields of Avro generated objects
			* that make serialization fail.
			*/
			public interface IgnoreAvroSchemaProperty {
				@JsonIgnore
				void getSchema();

				@JsonIgnore
				void getSpecificData();
			}

			/**
			* Custom objectMapper patched with a mixin with the Interface IgnoreAvroSchemaProperty
			* @return patched object mapper
			*/
			@Bean
			@Primary
			public ObjectMapper objectMapper() {
				return new ObjectMapper()
						.addMixIn(SpecificRecord.class, IgnoreAvroSchemaProperty.class);
			}

			/**
			* Create rest template with custom object mapper (by default it would create a new
			* object mapper instance that would fail treating SpecificRecord objects)
			* @return rest template with custom object mapper
			*/
			@Bean
			public RestTemplate restTemplate() {
				RestTemplate template =  new RestTemplateBuilder()
						.setConnectTimeout(Duration.ofSeconds(30))
						.setReadTimeout(Duration.ofSeconds(30))
						.build();
				template.getMessageConverters().add(0, mappingJacksonHttpMessageConverter());
				return template;
			}

			/**
			* Set custom object mapper to the Jackson converter
			* @return Jackson converter with custom object mapper
			*/
			@Bean
			public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
				MappingJackson2HttpMessageConverter converter =
						new MappingJackson2HttpMessageConverter();
				converter.setObjectMapper(objectMapper());
				return converter;
			}
		}
		```
	* Create the model to call Camunda under model.camunda. The models were created using the swagger editor starting from [OpenApi](https://docs.camunda.org/rest/camunda-bpm-platform/7.20/) definition of Camunda.

		```java
		@Data
		public class AtomLink {
			private String rel = null;
			private String href = null;
			private String method = null;
		}
		@Data
		@Builder
		public class CorrelationMessageDto {
			private String messageName;
			private String businessKey;
			private String tenantId;
			private Boolean withoutTenantId = false;
			private String processInstanceId;
			private Map<String, VariableValueDto> correlationKeys;
			private Map<String, VariableValueDto> localCorrelationKeys;
			private Map<String, VariableValueDto> processVariables;
			private Map<String, VariableValueDto> processVariablesLocal;
			private Boolean all = false;
			private Boolean resultEnabled = false;
			private Boolean variablesInResultEnabled = false;
		}
		@Data
		public class ExecutionDto {
			private String id;
			private String processInstanceId;
			private Boolean ended;
			private String tenantId;
		}
		@Data
		public class MessageCorrelationResultWithVariableDto {
			/**
			* Indicates if the message was correlated to a message start event or an  intermediate message catching event. In the first case, the resultType is  `ProcessDefinition` and otherwise `Execution`.
			*/
			@AllArgsConstructor
			public enum ResultTypeEnum {
				EXECUTION("Execution"),
				PROCESSDEFINITION("ProcessDefinition");

				private String value;

				@Override
				@JsonValue
				public String toString() {
					return String.valueOf(value);
				}

				@JsonCreator
				public static ResultTypeEnum fromValue(String text) {
					for (ResultTypeEnum b : ResultTypeEnum.values()) {
						if (String.valueOf(b.value).equals(text)) {
							return b;
						}
					}
					return null;
				}
			}

			private ResultTypeEnum resultType = null;
			private ProcessInstanceDto processInstance = null;
			private ExecutionDto execution = null;
			private Map<String, VariableValueDto> variables = null;

		}
		@Data
		public class ProcessInstanceDto {
			private String id;
			private String definitionId;
			private String businessKey;
			private String caseInstanceId;
			private Boolean ended;
			private Boolean suspended;
			private String tenantId;
			private List<AtomLink> links;
		}
		@Data
		@Builder
		public class SignalDto {
			private String name;
			private String executionId;
			private Map<String, VariableValueDto> variables;
			private String tenantId;
			private Boolean withoutTenantId;
		}
		@Data
		public class VariableValueDto {
			private Object value;
			private String type;
			private Map<String, Object> valueInfo;
		}
		```

6. You can check if everything works well running the application

	```dos
	mvn spring-boot:run '-Dspring-boot.run.arguments="--server.port=8500"'
	```	

	and calling it using these curls:

	```dos
	curl -X POST http://localhost:8500/kafka/events/camunda/publishing --data-raw "{\"workflowId\": \"da2fd17b-0d0f-4d00-b88e-13d0361073c1\",\"taskId\": \"7055fb17-b008-4142-98f2-dcf9f4d13dc2\", \"feedbackRequired\": true, \"feedback\":{\"feedbackEvent\":\"AuthorizedByOtherDevice\",\"feedbackType\":\"SIGNAL\"},\"data\": {\"author\":\"gandelfwiz\"}}" -H "Content-Type: application/json"

	curl -X POST http://localhost:8500/kafka/events/feedback/publishing --data-raw "{\"workflowId\": \"7b17db81-d0d0-11ee-a84d-581cf8936878\",\"taskId\": \"7055fb17-b008-4142-98f2-dcf9f4d13dc2\",\"result\": \"OK\",\"timestamp\": \"2023-01-23T01:03:10\",\"componentName\":\"curl\",\"feedback\":{\"feedbackEvent\":\"AuthorizedByOtherDevice\",\"feedbackType\":\"MESSAGE\"},\"data\": {\"author\": \"gandelfwiz\"}}" -H "Content-Type: application/json"
	```

7. Once you have created a working kafka sidecar you can interact with it from camunda process. Let's design the process of an authorization from another device. Go inside `Authorize with other device` subprocess and change it as follows:

	![Step 7 kafka integration subprocess](images/authorization_step07_kafka_integration.png)

	The process send a push notification to the other device using a sidecar connected to Kafka. Then wait for an event: if in 1 minute it doesn't receive a response close the process, else it close the process 

8. To create step `Push other device` you can copy from escalate subprocess the task `Send SMS` and adjust it as follow:
	* Set the *payload* variable javascript as follows:
		```javascript
		var payload = {
			"workflowId": execution.getProcessInstanceId(),
			"taskId": execution.getId(),
				"feedbackRequired": true,
				"feedback": {
						"feedbackEvent": "AuthorizedByOtherDevice",
						"feedbackType": "MESSAGE"
				},
			"data": {
				"author": "gandelfwiz"
			}
		}

		JSON.stringify(payload);
		```
	
	* Set the *url* variable as follows: `http://localhost:8500/kafka/events/camunda/publishing`

9. Configure the catch event to the message `AuthorizedByOtherDevice`. The set up the timer duration to `PT1M` and in Execution Listener set a javascript for *end* event type. 

	```javascript
	throw new org.camunda.bpm.engine.ProcessEngineException("No response received from device"); 
	```

10. Deploy and start the process and the sidecar. This time choose the biometric authorization. You will see in sidecar log something similar:

	```s
	2024-02-21T20:21:30.301+01:00  INFO 24508 --- [nio-8500-exec-6] o.g.w.c.sidecar.service.KafkaService     : Sending message to kafka {"workflowId": "66b9f24f-d0ee-11ee-a84d-581cf8936878", "taskId": "6a7f85df-d0ee-11ee-a84d-581cf8936878", "feedbackRequired": true, "feedback": {"feedbackEvent": "AuthorizedByOtherDevice", "feedbackType": "MESSAGE"}, "data": {"author": "gandelfwiz"}}
	2024-02-21T20:21:30.424+01:00  INFO 24508 --- [ad | producer-1] o.g.w.c.sidecar.service.KafkaService     : Message successfully sent at 1708543290385 _ 121 bytes to topic workshop_camunda_request_topic.
	2024-02-21T20:21:30.426+01:00  INFO 24508 --- [ntainer#1-0-C-1] o.g.w.c.sidecar.service.KafkaService     : ** MOCK SERVICE ** - Received message {"workflowId": "66b9f24f-d0ee-11ee-a84d-581cf8936878", "taskId": "6a7f85df-d0ee-11ee-a84d-581cf8936878", "feedbackRequired": true, "feedback": {"feedbackEvent": "AuthorizedByOtherDevice", "feedbackType": "MESSAGE"}, "data": {"author": "gandelfwiz"}}
	2024-02-21T20:21:35.433+01:00  INFO 24508 --- [onPool-worker-8] o.g.w.c.sidecar.service.KafkaService     : Sending message to kafka {"workflowId": "66b9f24f-d0ee-11ee-a84d-581cf8936878", "taskId": "6a7f85df-d0ee-11ee-a84d-581cf8936878", "result": "OK", "timestamp": "2024-02-21T20:21:35.4337519", "componentName": "sidecar", "Feedback": {"feedbackEvent": "AuthorizedByOtherDevice", "feedbackType": "MESSAGE"}, "data": {"author": "gandelfwiz"}}
	2024-02-21T20:21:35.503+01:00  INFO 24508 --- [ad | producer-1] o.g.w.c.sidecar.service.KafkaService     : Message successfully sent at 1708543295463 _ 157 bytes to topic workshop_camunda_feedback_topic.
	2024-02-21T20:21:35.505+01:00  INFO 24508 --- [ntainer#0-0-C-1] o.g.w.c.sidecar.service.KafkaService     : Received message {"workflowId": "66b9f24f-d0ee-11ee-a84d-581cf8936878", "taskId": "6a7f85df-d0ee-11ee-a84d-581cf8936878", "result": "OK", "timestamp": "2024-02-21T20:21:35.4337519", "componentName": "sidecar", "Feedback": {"feedbackEvent": "AuthorizedByOtherDevice", "feedbackType": "MESSAGE"}, "data": {"author": "gandelfwiz"}}
	2024-02-21T20:21:35.538+01:00  INFO 24508 --- [ntainer#0-0-C-1] o.g.w.c.sidecar.service.KafkaService     : Correlate message to Camunda CorrelationMessageDto(messageName=AuthorizedByOtherDevice, businessKey=null, tenantId=null, withoutTenantId=null, processInstanceId=66b9f24f-d0ee-11ee-a84d-581cf8936878, correlationKeys=null, localCorrelationKeys=null, processVariables=null, processVariablesLocal=null, all=null, resultEnabled=null, variablesInResultEnabled=null)
	```

11. To test the timer you should set up the payload javascript property `feedbackRequired` to false. Deploy, run the process and wait. An incident will be created.

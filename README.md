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


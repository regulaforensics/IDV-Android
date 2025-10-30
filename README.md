# **IDV SDK Integration Guide**

## **Introduction**

This guide provides step-by-step instructions on integrating the **IDV SDK** into an Android application. It covers initialization, API configuration, workflow setup, and starting the ID verification process.

---

## **1. Prerequisites**

Before integrating the SDK, ensure the following:

- Android Target SDK **34** recommended
- **Camera permission** enabled
- **NFC permission** enabled
- **Internet permission** enabled
- Regula IDV SDK dependency added to `build.gradle.kts`

---

## **2. Add Regula SDK to Gradle**

1. Add url to Regula's maven repository in you `build.gradle.kts` file
   maven {
   url = uri("https://maven.regulaforensics.com/RegulaDocumentReader")
   }

   // only for Beta versions
   maven {
   url = uri("https://maven.regulaforensics.com/RegulaDocumentReader/Beta")
   }

2. Copy **regula.license** and **db.dat** files to `app/src/main/assets/Regula` folder.

3. Enable view and data bingings features in your **app-level** `build.gradle.kts` by adding to `android` section:

```kotlin
    buildFeatures {
        viewBinding=true
        dataBinding=true
    }
```

and add `kotlin-kapt` plugin:

```kotlin
    plugins {
        id("kotlin-kapt")
    }
```

4. In your **app-level** `build.gradle.kts`, add the following dependency:

```kotlin
// Required only if you're going to use DocReader SDK in your workflows
implementation("com.regula.documentreader.core:fullrfid:8.2+@aar") {}
implementation("com.regula.idv:docreader:3.1.+@aar") {
        isTransitive = true
    }

// Required only if you're going to use Face SDK in your workflows
implementation("com.regula.face.core:basic:7.1+@aar") {}
implementation("com.regula.idv:face:3.1.+@aar") {
        isTransitive = true
    }

// main dependency
implementation("com.regula.idv:api:3.1.+@aar") {
        isTransitive = true
    }
```

Sync Gradle after adding the dependencies.

---

## **3. Update AndroidManifest.xml**

Ensure the following permissions and features are included:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-feature android:name="android.hardware.camera"
            android:required="true" />

    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Required only if you're going to scan documents using Camera  -->
    <uses-permission android:name="android.permission.CAMERA"/>

    <!-- Required only if you're going to read Rfid chip  -->
    <uses-permission android:name="android.permission.NFC" />

</manifest>
```

---

## **4. Initialize Regula IDV SDK**

In your `MainActivity.kt`, initialize the SDK with the **Document Reader Module**:

```kotlin
import com.regula.idv.api.IdvSdk
import com.regula.idv.docreader.DocReaderModule
import com.regula.idv.face.FaceModule
import com.regula.idv.api.config.InitConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = InitConfig(listOf(DocReaderModule(), FaceModule()))   // you can use both DocReader and Face module or only one when it's not required by workflow
        IdvSdk.instance().initialize(context, config) {
            // Handle initialization result
        }
    }
}
```

---

## **5. Configure API Settings**

Before using the SDK, configure the API connection. There are 3 ways to configure the API connection:

<details><summary>Using credentials</summary>

```kotlin
import com.regula.idv.api.config.CredentialsConnectionConfig

val URL = "https://..."
val USER_NAME = "your_username"
val PASSWORD = "your_password"

IdvSdk.instance().configure(context, CredentialsConnectionConfig(URL, USER_NAME, PASSWORD, IS_SECURE)) {
    it.onSuccess {
        // handle success
    }
    it.onFailure { exception ->
        // handle errors
    }
}
```

</details>

<details><summary>Using token</summary>

Use this way when you have url with already defined auth key. In the result you will get a list of workflows id that you can perform.

```kotlin
import com.regula.idv.api.config.TokenConnectionConfig

val URL = "https://.../mobile?authorization=Token%20tr-pn7u6f5wizj9l7fnx4nzu8pqrnlwr&sessionId=68a72388660070d96275a8c2"

IdvSdk.instance().configure(context, TokenConnectionConfig(URL)) {
    it.onSuccess { workflows ->
        // handle success
    }
    it.onFailure { exception ->
        // handle errors
    }
}
```

</details>

<details><summary>Using Api Key</summary>

```kotlin
import com.regula.idv.api.config.ApiKeyConnectionConfig

val URL = "https://..."
val API_KEY = "yprpwttipracqerersuzzrs_fbptvhtoczfjbx9azn8w3lzioddacyw3slfvoq3"

IdvSdk.instance().configure(context, ApiKeyConnectionConfig(URL, API_KEY)) {
    it.onSuccess {
        // handle success
    }
    it.onFailure { exception ->
        // handle errors
    }
}
```

</details>

---

## **6. Prepare and Start an ID Verification Workflow**

#### Prepare a workflow before starting the verification:

```kotlin
import com.regula.idv.api.config.PrepareWorkflowConfig

val workflowId = "your_workflow_id"
IdvSdk.instance().prepareWorkflow(context, PrepareWorkflowConfig(workflowId)) {
    it.onSuccess {
        // handle success
    }
    it.onFailure { exception ->
        // handle errors
    }
}
```

#### Start the workflow with default parameters when ready:

```kotlin
IdvSdk.instance().startWorkflow(context) {
    it.onSuccess { workflowResult ->
        Log.d("IDV", "sessionId: ${workflowResult.sessionId}")
    }
    it.onFailure { exception -> 
        // handle errors
    }
}
```

#### Set up metadata before start workflow:


```kotlin
import com.regula.idv.api.config.StartWorkflowConfig

val metadata = JSONObject()
metadata.put("key1", "value1")
val config = StartWorkflowConfig.Builder()
    .setMetadata(metadata)
    .build()
IdvSdk.instance().startWorkflow(context, config) {
    it.onSuccess { workflowResult ->
        Log.d("IDV", "sessionId: ${workflowResult.sessionId}")
    }
    it.onFailure { exception -> 
        // handle errors
    }
}
```

#### With specific language:


```kotlin
import com.regula.idv.api.config.StartWorkflowConfig

val config = StartWorkflowConfig.Builder()
    .setLocale("de")
    .build()
IdvSdk.instance().startWorkflow(context, config) {
    it.onSuccess { workflowResult ->
        Log.d("IDV", "sessionId: ${workflowResult.sessionId}")
    }
    it.onFailure { exception -> 
        // handle errors
    }
}
```

---

## **7 Migration**

### **7.1 Migration from 2.3 to 2.4/2.5**

If you used metadata when you start workflow, from 2.4 version you should use `IdvStartWorkflowConfig` config.
See example:

```kotlin
val metadata = JSONObject()
metadata.put("key1", "value1")
val scenarioConfig = IdvStartWorkflowConfig.Builder()
    .setMetadata(metadata)
    .build()
IdvSdk.instance().startWorkflow(this, scenarioConfig) { sessionResult, error ->
    if (error == null) {
        // Handle successful verification
    } else {
        // Handle error
    }
}
```


### **7.2 Migration from 2.4/2.5 to 3.1**

- `IdvInitConfig` renamed to `InitConfig`.
- `IdvConnectionConfig` renamed to `CredentialsConnectionConfig`. Also, now the first parameter is full url that includes http schema (http or https). You don't need to put IS_SECURE parameter anymore.
- `IdvPrepareWorkflowConfig` renamed to `PrepareWorkflowConfig`.
- `IdvStartWorkflowConfig` renamed to `StartWorkflowConfig`.
- changed completion in `startWorkflow` method. Now it returns `Result<WorkflowResult>` object.

---

## **8. Best Practices & Troubleshooting**

- **Ensure all necessary dependencies** are included in `build.gradle.kts`.
- **Handle API failures** by checking the `workflowResult` and `exception` callbacks.
- **Grant camera permissions** before starting the workflow.
- **Use proper configuration** when establish connection to the platform.

---


## **Conclusion**

This guide provides all necessary steps to integrate the **Regula IDV SDK** into an Android application. By following these instructions, developers can build a document verification feature using Regula’s technology.

For further details, refer to the **official Regula IDV SDK documentation** or contact support team.

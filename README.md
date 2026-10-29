# **IDV SDK Integration Guide**

## **Introduction**

This guide provides step-by-step instructions on integrating the **IDV SDK** into an Android application. It covers initialization, API configuration, workflow setup, and starting the ID verification process.

---

## **1. Prerequisites**

Before integrating the SDK, ensure the following:

- Android target SDK **36** is recommended
- **Camera** permission is enabled
- **NFC** permission is enabled
- **Internet** permission is enabled
- The Regula IDV SDK dependency is added to `build.gradle.kts`

---

## **2. Add Regula SDK to Gradle**

1. Add the URL of Regula's Maven repository to your `build.gradle.kts` file:

```kotlin
maven {
    url = uri("https://maven.regulaforensics.com/RegulaDocumentReader")
}

// Only for beta versions
maven {
    url = uri("https://maven.regulaforensics.com/RegulaDocumentReader/Beta")
}
```

2. Copy the **regula.license** and **db.dat** files to the `app/src/main/assets/Regula` folder.

3. Enable view binding and data binding in your **app-level** `build.gradle.kts` by adding the following to the `android` section:

```kotlin
    buildFeatures {
        viewBinding=true
        dataBinding=true
    }
```

Also add the `kotlin-kapt` plugin:

```kotlin
    plugins {
        id("kotlin-kapt")
    }
```

4. In your **app-level** `build.gradle.kts`, add the following dependencies:

```kotlin
// Required only if you're going to use DocReader SDK in your workflows
implementation("com.regula.documentreader.core:fullrfid:9.9+@aar") {}
implementation("com.regula.idv:docreader:3.10.+@aar") {
        isTransitive = true
    }

// Required only if you're going to use Face SDK in your workflows
implementation("com.regula.face.core:basic:8.3+@aar") {}
implementation("com.regula.idv:face:3.10.+@aar") {
        isTransitive = true
    }

// Main dependency
implementation("com.regula.idv:api:3.10.+@aar") {
        isTransitive = true
    }
```

Sync Gradle after adding the dependencies.

5. Use a different version of the Document Reader API SDK or Face API SDK

The minimum versions of the Document Reader SDK and Face SDK are already included in the project. If you want to update them to the latest versions, check the release pages and use the current versions:

- [Document Reader Android releases](https://github.com/regulaforensics/DocumentReader-Android/releases)
- [Face SDK Android releases](https://github.com/regulaforensics/FaceSDK-Android/releases)

```kotlin
// By default, the Document Reader API is included in the docreader module of IDV.
// You can override it with a different version if you want to use the latest one.
implementation("com.regula.documentreader:api:9.9.+@aar") {
        isTransitive = true
    }

// By default, the Face API is included in the face module of IDV.
// You can override it with a different version if you want to use the latest one.
implementation("com.regula.face:api:8.4.+@aar") {
        isTransitive = true
    }
```

6. If you plan to read an mDL, add the BT Device library:

```kotlin
implementation("com.regula.btdevice:api:9.9.+@aar")
```

Make sure the following permissions are included:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission
        android:name="android.permission.ACCESS_COARSE_LOCATION"
        tools:remove="android:maxSdkVersion" />
    <uses-permission
        android:name="android.permission.ACCESS_FINE_LOCATION"
        tools:remove="android:maxSdkVersion" />

</manifest>
```

Use `tools:remove="android:maxSdkVersion"` when the workflow requests the user's location.

---

## **3. Update AndroidManifest.xml**

Ensure the following permissions and features are included:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-feature android:name="android.hardware.camera"
            android:required="true" />

    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Required only if you are going to scan documents using the camera -->
    <uses-permission android:name="android.permission.CAMERA"/>

    <!-- Required only if you are going to read an RFID chip -->
    <uses-permission android:name="android.permission.NFC" />

    <!-- Required only if you are going to request the user's location in a workflow or read an mDL -->
    <uses-permission
        android:name="android.permission.ACCESS_COARSE_LOCATION"
        tools:remove="android:maxSdkVersion" />
    <uses-permission
        android:name="android.permission.ACCESS_FINE_LOCATION"
        tools:remove="android:maxSdkVersion" />

</manifest>
```

---

## **4. Initialize Regula IDV SDK**

In your `MainActivity.kt`, initialize the SDK with the modules required by your workflow:

```kotlin
import com.regula.idv.api.IdvSdk
import com.regula.idv.docreader.DocReaderModule
import com.regula.idv.face.FaceModule
import com.regula.idv.api.config.InitConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = InitConfig(listOf(DocReaderModule(), FaceModule()))   // You can use both the DocReader and Face modules, or only the one required by the workflow
        IdvSdk.instance().initialize(context, config) {
            // Handle initialization result
        }
    }
}
```

---

## **5. Configure API Settings**

Before using the SDK, configure the API connection. There are three ways to do this:

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

Use this option when you have a URL with an auth key already defined. The result is a list of workflow IDs that you can run.

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

<details><summary>Using an API key</summary>

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

#### Start the workflow with the default parameters:

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

#### Set metadata before starting the workflow:


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

#### Start the workflow in a specific language:


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

## **7. Migration**

### **7.1 Migration from 2.3 to 2.4/2.5**

If you passed metadata when starting a workflow, use the `IdvStartWorkflowConfig` configuration starting from version 2.4.
See the example:

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

- `IdvInitConfig` was renamed to `InitConfig`.
- `IdvConnectionConfig` was renamed to `CredentialsConnectionConfig`. The first parameter is now the full URL, including the scheme (`http` or `https`). You no longer need to pass the `IS_SECURE` parameter.
- `IdvPrepareWorkflowConfig` was renamed to `PrepareWorkflowConfig`.
- `IdvStartWorkflowConfig` was renamed to `StartWorkflowConfig`.
- The completion handler of `startWorkflow` has changed. The method now returns a `Result<WorkflowResult>`.

### **7.3 Migration from 3.1 to 3.2**
- No updates are required.

### **7.4 Migration from 3.2 to 3.3**
- No updates are required.

### **7.5 Migration from 3.3 to 3.4**
- No updates are required.

### **7.6 Migration from 3.4 to 3.6**
- No updates are required.

### **7.7 Migration from 3.6 to 3.8**
- No updates are required.

### **7.8 Migration from 3.8 to 3.9**
- No updates are required.

### **7.9 Migration from 3.9 to 3.10**
- No updates are required.

---

## **8. Best Practices & Troubleshooting**

- **Ensure all necessary dependencies** are included in `build.gradle.kts`.
- **Handle API failures** by checking `workflowResult` in `onSuccess` and `exception` in `onFailure`.
- **Grant camera permission** before starting the workflow.
- **Use the correct configuration** when establishing a connection to the platform.

---


## **Conclusion**

This guide covers the steps required to integrate the **Regula IDV SDK** into an Android application. By following these instructions, you can build a document verification feature with Regula technology.

For further details, refer to the **official Regula IDV SDK documentation** or contact the support team.

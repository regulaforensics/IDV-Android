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
implementation("com.regula.documentreader.core:fullrfid:7.6+@aar") {}
implementation("com.regula.idv:docreader:1.0.29@aar") {
        isTransitive = true
    }
implementation("com.regula.face.core:basic:6.4+@aar") {}
implementation("com.regula.idv:face:1.0.27@aar") {
        isTransitive = true
    }
implementation("com.regula.idv:api:1.0.17@aar") {
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
    <uses-permission android:name="android.permission.CAMERA"/>
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
import com.regula.idv.api.config.IdvInitConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = IdvInitConfig(listOf(DocReaderModule(), FaceModule()))
        IdvSdk.instance().initialize(this, config) {
            // Handle initialization result
        }
    }
}
```

---

## **5. Configure API Settings**

Before using the SDK, configure the API connection:

```kotlin
import com.regula.idv.api.config.IdvUrlConfig
import com.regula.idv.api.config.IdvConnectionConfig

val HOST = "your_host"
val USER_NAME = "your_username"
val PASSWORD = "your_password"
val IS_SECURE = true

IdvSdk.instance().configure(this, IdvConnectionConfig(HOST, USER_NAME, PASSWORD, IS_SECURE)) {}
```

---

## **6. Prepare and Start an ID Verification Workflow**

Prepare a workflow before starting the verification:

`your_workflow_id` is dependent on wheter the disability check is on or off.

If disablity check in ON, the value of `your_workflow_id` will be "WorkflowXXX", otherwise, it will be "WorkflowYYY".

**We will confirm the workflow ids before we start integration.**

```kotlin
import com.regula.idv.api.config.IdvPrepareWorkflowConfig

val workflowId = "your_workflow_id"
IdvSdk.instance().prepareWorkflow(this, IdvPrepareWorkflowConfig(workflowId)) {}
```

Start the workflow when ready:

```kotlin
IdvSdk.instance().startWorkflow(this) { sessionResult, error ->
    if (error == null) {
        // Handle successful verification
    } else {
        // Handle error
    }
}
```

With metadata:


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

With specific language:


```kotlin
val metadata = JSONObject()
metadata.put("key1", "value1")
val scenarioConfig = IdvStartWorkflowConfig.Builder()
    .setLocale("de")
    .build()
IdvSdk.instance().startWorkflow(this, scenarioConfig) { sessionResult, error ->
    if (error == null) {
        // Handle successful verification
    } else {
        // Handle error
    }
}
```

---

## **7. Migration from 2.3 to 2.4**

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

---

## **8. Best Practices & Troubleshooting**

- **Ensure all necessary dependencies** are included in `build.gradle.kts`.
- **Handle API failures** by checking the `sessionResult` and `error` callbacks.
- **Grant camera permissions** before starting the workflow.
- **Use proper credentials** when configuring `IdvConnectionConfig`.

---


## **Conclusion**

This guide provides all necessary steps to integrate the **Regula IDV SDK** into an Android application. By following these instructions, developers can build a document verification feature using Regula’s technology.

For further details, refer to the **official Regula IDV SDK documentation** or contact support team.

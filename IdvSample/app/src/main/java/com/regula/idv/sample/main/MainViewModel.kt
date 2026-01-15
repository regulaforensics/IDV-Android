package com.regula.idv.sample.main

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.regula.idv.api.IdvSdk
import com.regula.idv.api.config.ApiKeyConnectionConfig
import com.regula.idv.api.config.CredentialsConnectionConfig
import com.regula.idv.api.config.InitConfig
import com.regula.idv.api.config.PrepareWorkflowConfig
import com.regula.idv.api.config.StartWorkflowConfig
import com.regula.idv.api.config.TokenConnectionConfig
import com.regula.idv.api.listeners.IdvSdkListener
import com.regula.idv.api.models.Workflow
import com.regula.idv.docreader.DocReaderModule
import com.regula.idv.face.FaceModule
import com.regula.idv.sample.App
import com.regula.idv.sample.R
import com.regula.idv.sample.main.adapter.UiWorkflow
import com.regula.idv.sample.utils.LocalizedString
import com.regula.idv.sample.utils.SingleLiveEvent

class MainViewModel(
    private val appContext: Context = App.instance(),
    private val idvSdk: IdvSdk = IdvSdk.instance(),
) : ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.simpleName

        private const val BASE_URL = "https://..."
        private const val USER_NAME = "..."
        private const val PASSWORD = "..."
        private const val API_KEY = "..."
    }

    private val _btnCredConfigEnabled = MutableLiveData(true)
    val btnCredConfigEnabled: LiveData<Boolean>
        get() = _btnCredConfigEnabled

    private val _btnApiKeyConfigEnabled = MutableLiveData(true)
    val btnApiKeyConfigEnabled: LiveData<Boolean>
        get() = _btnApiKeyConfigEnabled

    private val _btnUrlConfigEnabled = MutableLiveData(true)
    val btnUrlConfigEnabled: LiveData<Boolean>
        get() = _btnUrlConfigEnabled

    private val _btnQrConfigEnabled = MutableLiveData(true)
    val btnQrConfigEnabled: LiveData<Boolean>
        get() = _btnQrConfigEnabled

    private val _progressMessage = MutableLiveData<LocalizedString?>(null)
    val progressMessage: LiveData<LocalizedString?>
        get() = _progressMessage

    private val _errorEvent = SingleLiveEvent<Throwable>()
    val errorEvent: LiveData<Throwable>
        get() = _errorEvent

    private val _workflows = MutableLiveData<List<UiWorkflow>>(emptyList())
    val workflows: LiveData<List<UiWorkflow>>
        get() = _workflows

    private val selectedWorkflow: LiveData<UiWorkflow?> =
        workflows.map { workflows ->
            workflows.firstOrNull(UiWorkflow::isSelected)
        }

    val btnStartWorkflowEnabled: LiveData<Boolean> =
        selectedWorkflow.map { selectedWorkflow ->
            (selectedWorkflow != null)
        }

    private val idvSdkListener: IdvSdkListener =
        object : IdvSdkListener {
            override fun didStartSession() {
                Log.d(TAG, "didStartSession: ${idvSdk.currentSessionId()}")
            }
            override fun didEndSession() {
                Log.d(TAG, "didEndSession: ${idvSdk.currentSessionId()}")
            }
            override fun didStartRestoreSession() {
                Log.d(TAG, "didStartRestoreSession: ${idvSdk.currentSessionId()}")
            }
            override fun didContinueRemoteSession() {
                Log.d(TAG, "didContinueRemoteSession: ${idvSdk.currentSessionId()}")
            }
        }

    init {
        initSdk()
        setupSdkListener()
    }

    private fun initSdk() {
        val config = InitConfig(listOf(DocReaderModule(), FaceModule()))
        idvSdk.initialize(appContext, config) { result ->
            result
                .onSuccess {
                    Log.d(TAG, "initSdk success")
                }
                .onFailure { error ->
                    Log.d(TAG, "initSdk error: $error")
                    showError(error)
                }
        }
    }

    private fun setupSdkListener() {
        idvSdk.listener = idvSdkListener
    }

    fun configureByCredentials() {
        disableAllConfigureButtons()
        showProgress(R.string.configuring_sdk)

        val config = CredentialsConnectionConfig(BASE_URL, USER_NAME, PASSWORD)
        idvSdk.configure(appContext, config) { result ->
            result
                .onSuccess {
                    Log.d(TAG, "configureByCredentials success")
                    hideProgress()
                    getWorkflows()
                }
                .onFailure { error ->
                    Log.e(TAG, "configureByCredentials error: $error")
                    hideProgress()
                    showError(error)
                    enableAllConfigureButtons()
                }
        }
    }

    fun configureByApiKey() {
        disableAllConfigureButtons()
        showProgress(R.string.configuring_sdk)

        val config = ApiKeyConnectionConfig(BASE_URL, API_KEY)
        idvSdk.configure(appContext, config) { result ->
            result
                .onSuccess {
                    Log.d(TAG, "configureByApiKey success")
                    hideProgress()
                    getWorkflows()
                }
                .onFailure { error ->
                    Log.e(TAG, "configureByApiKey error: $error")
                    hideProgress()
                    showError(error)
                    enableAllConfigureButtons()
                }
        }
    }

    fun configureByUrl(url: String) {
        clearWorkflows()
        disableAllConfigureButtons()
        showProgress(R.string.configuring_sdk)

        val config = TokenConnectionConfig(url)
        idvSdk.configure(appContext, config) { result ->
            result
                .onSuccess { workflowIds ->
                    Log.d(TAG, "configureByUrl success")
                    hideProgress()
                    enableConfigureByUrlButton()
                    enableConfigureByQrButton()
                    getWorkflows(
                        filter = { workflow -> workflow.id in workflowIds }
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "configureByUrl error: $error")
                    hideProgress()
                    showError(error)
                    enableAllConfigureButtons()
                }
        }
    }

    private fun getWorkflows(
        filter: (Workflow) -> Boolean = { true }
    ) {
        showProgress(R.string.loading_workflows)

        idvSdk.getWorkflows { result ->
            result
                .map { workflows ->
                    workflows.filter(filter)
                }
                .onSuccess { workflows ->
                    Log.d(TAG, "getWorkflows success")
                    hideProgress()
                    showWorkflows(workflows)
                    if (workflows.size == 1) {
                        prepareWorkflow(workflows[0].id)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "getWorkflows error: $error")
                    hideProgress()
                    showError(error)
                }
        }
    }

    fun prepareWorkflow(workflowId: String) {
        if (workflowId == selectedWorkflow.value?.id) {
            return
        }
        showProgress(R.string.preparing_workflow)
        selectWorkflow("")

        val config = PrepareWorkflowConfig(workflowId)
        idvSdk.prepareWorkflow(appContext, config) { result ->
            result
                .onSuccess {
                    Log.d(TAG, "prepareWorkflow success")
                    hideProgress()
                    selectWorkflow(workflowId)
                }
                .onFailure { error ->
                    Log.e(TAG, "prepareWorkflow error: $error")
                    hideProgress()
                    showError(error)
                }
        }
    }

    fun startWorkflow() {
        val config = StartWorkflowConfig.Builder()
            //.setLocale("en") // set language here
            .build()
        idvSdk.startWorkflow(appContext, config) { result ->
            result
                .onSuccess { workflowResult ->
                    Log.d(TAG, "Workflow completed successfully, transactionId: ${workflowResult.sessionId}")
                }
                .onFailure { error ->
                    Log.e(TAG, "Workflow completed with error: $error")
                }
        }
    }

    private fun enableConfigureByUrlButton() {
        _btnUrlConfigEnabled.value = true
    }

    private fun enableConfigureByQrButton() {
        _btnQrConfigEnabled.value = true
    }

    private fun enableAllConfigureButtons() {
        _btnCredConfigEnabled.value = true
        _btnApiKeyConfigEnabled.value = true
        _btnUrlConfigEnabled.value = true
        _btnQrConfigEnabled.value = true
    }

    private fun disableAllConfigureButtons() {
        _btnCredConfigEnabled.value = false
        _btnApiKeyConfigEnabled.value = false
        _btnUrlConfigEnabled.value = false
        _btnQrConfigEnabled.value = false
    }

    private fun showProgress(@StringRes messageId: Int) {
        _progressMessage.value = LocalizedString(messageId)
    }

    private fun hideProgress() {
        _progressMessage.value = null
    }

    private fun showError(error: Throwable) {
        _errorEvent.value = error
    }

    private fun showWorkflows(workflows: List<Workflow>) {
        _workflows.value = workflows
            .toUiWorkflows()
    }

    private fun selectWorkflow(workflowId: String) {
        _workflows.value = _workflows.value
            ?.selectWorkflow(workflowId)
    }

    private fun clearWorkflows() {
        _workflows.value = emptyList()
    }
}

private fun List<Workflow>.toUiWorkflows(): List<UiWorkflow> =
    map { workflow ->
        UiWorkflow(
            id = workflow.id,
            name = workflow.name,
            isSelected = false,
        )
    }

private fun List<UiWorkflow>.selectWorkflow(workflowId: String): List<UiWorkflow> =
    map { workflow ->
        workflow.copy(
            isSelected = (workflow.id == workflowId)
        )
    }

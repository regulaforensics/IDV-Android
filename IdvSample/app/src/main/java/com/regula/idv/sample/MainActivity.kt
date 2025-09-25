package com.regula.idv.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.regula.idv.api.IdvSdk
import com.regula.idv.api.config.ApiKeyConnectionConfig
import com.regula.idv.api.config.CredentialsConnectionConfig
import com.regula.idv.api.config.InitConfig
import com.regula.idv.api.config.PrepareWorkflowConfig
import com.regula.idv.api.config.StartWorkflowConfig
import com.regula.idv.api.config.TokenConnectionConfig
import com.regula.idv.docreader.DocReaderModule
import com.regula.idv.face.FaceModule
import com.regula.idv.sample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var detector: BarcodeScanner

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private val TAG = MainActivity::class.simpleName
        private const val PERMISSION_REQUEST_CODE = 1

        private const val BASE_URL = "https://..."
        private const val USER_NAME = "..."
        private const val PASSWORD = "..."
        private const val WORKFLOW_ID = "..."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buildQrDetector()

        initialize()
    }

    private fun initialize() {
        val config = InitConfig(listOf(DocReaderModule(), FaceModule()))
        IdvSdk.instance().initialize(this, config) {
            it.onSuccess {
                Log.d(TAG, "Initialized successfully")
            }
            it.onFailure { error ->
                Log.d(TAG, "Initialization error: $error")
            }
        }
    }

    private fun buildQrDetector() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC)
            .build()
        detector = BarcodeScanning.getClient(options)
    }

    fun openCamera(view: View) {
        if (checkPermission()) {
            dispatchTakePictureIntent()
        } else {
            requestPermission()
        }
    }

    fun loginWithCredentials(view: View) {
        connectWithCredentials()
    }

    fun loginWithApiKey(view: View) {
        configureByApiKey()
    }

    private fun dispatchTakePictureIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // error with permission
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap?
            if (bitmap != null) {
                detectQrCode(bitmap)
            }
        }
    }

    private fun detectQrCode(bitmap: Bitmap) {
        detector.process(InputImage.fromBitmap(bitmap, 0))
            .addOnCompleteListener { result ->
                result.result.firstOrNull()?.let {
                    it.rawValue?.let { url ->
                        connectByQr(url)
                    }
                }
                for (barcode in result.result) {
                    val rawValue = barcode.rawValue
                    Log.d(TAG, "Result: $rawValue")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Error: $it")
            }
    }

    private fun connectByQr(url: String) {
        binding.btnStart.isEnabled = false
        changeButtonsState(false)
        val config = TokenConnectionConfig(url)
        IdvSdk.instance().configure(this, config) {
            it.onSuccess { workflows ->
                Log.d(TAG, "Connected successfully")
                binding.btnScanQr.isEnabled = true
                if (workflows.isEmpty().not())
                    prepareWorkflow(workflows[0])
                else
                    changeButtonsState(true)
            }
            it.onFailure { error ->
                changeButtonsState(true)
                Log.e(TAG, "Connect error: $error")
            }
        }
    }

    private fun connectWithCredentials() {
        changeButtonsState(false)
        val config = CredentialsConnectionConfig(BASE_URL, USER_NAME, PASSWORD)
        IdvSdk.instance().configure(this, config) {
            it.onSuccess {
                Log.d(TAG, "Connected successfully")
                prepareWorkflow(WORKFLOW_ID)
            }
            it.onFailure { error ->
                changeButtonsState(true)
                Log.e(TAG, "Connect error: $error")
            }
        }
    }

    private fun configureByApiKey() {
        changeButtonsState(false)
        val apiKey = ""
        val config = ApiKeyConnectionConfig(BASE_URL, apiKey)
        IdvSdk.instance().configure(this, config) {
            it.onSuccess {
                Log.d(TAG, "Connected successfully")
                prepareWorkflow(WORKFLOW_ID)
            }
            it.onFailure { error ->
                changeButtonsState(true)
                Log.e(TAG, "Connect error: $error")
            }
        }
    }

    private fun prepareWorkflow(workflowId: String) {
        IdvSdk.instance().prepareWorkflow(this@MainActivity, PrepareWorkflowConfig(workflowId)) {
            it.onSuccess {
                binding.btnStart.isEnabled = true
                binding.clProgress.visibility = View.GONE
            }
            it.onFailure { error ->
                changeButtonsState(true)
                Log.d(TAG, "Prepare error: $error")
            }
        }
    }

    fun startWorkflow(view: View) {
        binding.btnStart.isEnabled = false

        val config = StartWorkflowConfig.Builder()
//            .setLocale("es") // set language here
            .build()
        IdvSdk.instance().startWorkflow(this@MainActivity, config) {
            binding.btnStart.isEnabled = true
            it.onSuccess { workflowResult ->
                Log.d(TAG, "Workflow completed successfully, transactionId: ${workflowResult.sessionId}")
            }
            it.onFailure { error ->
                Log.e(TAG, "Workflow completed with error: $error")
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun changeButtonsState(enable: Boolean) {
        binding.btnCredLogin.isEnabled = enable
        binding.btnApiKeyLogin.isEnabled = enable
        binding.btnScanQr.isEnabled = enable
        if (enable)
            binding.clProgress.visibility = View.GONE
        else
            binding.clProgress.visibility = View.VISIBLE
    }
}
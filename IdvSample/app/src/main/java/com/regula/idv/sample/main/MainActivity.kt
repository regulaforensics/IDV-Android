package com.regula.idv.sample.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.regula.idv.sample.R
import com.regula.idv.sample.databinding.ActivityMainBinding
import com.regula.idv.sample.error.showErrorDialog
import com.regula.idv.sample.main.adapter.UiWorkflowsAdapter
import com.regula.idv.sample.url.UrlInputActivity
import com.regula.idv.sample.utils.handleWindowInsets

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    private object RequestCode {
        const val PERMISSION_REQUEST = 1
        const val IMAGE_CAPTURE = 2
        const val URL_INPUT = 3
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var detector: BarcodeScanner

    private val viewModel: MainViewModel by viewModels()
    private val workflowsAdapter: UiWorkflowsAdapter by lazy {
        UiWorkflowsAdapter(
            onItemClick = { workflow ->
                viewModel.prepareWorkflow(workflow.id)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleWindowInsets(binding.contentLayout)

        detector = buildQrDetector()

        setupRecycler()
        setupListeners()
        setupObservers()
    }

    private fun buildQrDetector(): BarcodeScanner {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()
        return BarcodeScanning.getClient(options)
    }

    private fun setupRecycler() {
        binding.rvWorkflows.adapter = workflowsAdapter
    }

    private fun setupListeners() {
        binding.btnUrlConfig.setOnClickListener {
            startUrlInput()
        }
        binding.btnQrConfig.setOnClickListener {
            startCamera()
        }
        binding.btnCredConfig.setOnClickListener {
            viewModel.configureByCredentials()
        }
        binding.btnApiKeyConfig.setOnClickListener {
            viewModel.configureByApiKey()
        }
        binding.btnStartWorkflow.setOnClickListener {
            viewModel.startWorkflow()
        }
    }

    private fun setupObservers() {
        viewModel
            .btnCredConfigEnabled
            .observe(this, binding.btnCredConfig::setEnabled)

        viewModel
            .btnUrlConfigEnabled
            .observe(this, binding.btnUrlConfig::setEnabled)

        viewModel
            .btnQrConfigEnabled
            .observe(this, binding.btnQrConfig::setEnabled)

        viewModel
            .btnApiKeyConfigEnabled
            .observe(this, binding.btnApiKeyConfig::setEnabled)

        viewModel
            .btnStartWorkflowEnabled
            .observe(this, binding.btnStartWorkflow::setEnabled)

        viewModel
            .progressMessage
            .observe(this) { progressMessage ->
                binding.progressLayout.isVisible = (progressMessage != null)
                binding.tvProgressMessage.text = progressMessage?.toPlainString(this)
            }

        viewModel
            .errorEvent
            .observe(this) { error ->
                showErrorDialog(error.toString())
            }

        viewModel
            .workflows
            .observe(this, workflowsAdapter::submitList)
    }

    private fun startUrlInput() {
        val intent = Intent(this, UrlInputActivity::class.java)
        startActivityForResult(intent, RequestCode.URL_INPUT)
    }

    private fun startCamera() {
        if (checkCameraPermission()) {
            dispatchTakePictureIntent()
        } else {
            requestCameraPermission()
        }
    }

    private fun dispatchTakePictureIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, RequestCode.IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RequestCode.PERMISSION_REQUEST -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    showErrorDialog(getString(R.string.camera_permission_denied))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            RequestCode.IMAGE_CAPTURE -> {
                val bitmap = data?.extras?.get("data") as Bitmap?
                if (bitmap != null) {
                    detectQrCode(bitmap)
                }
            }
            RequestCode.URL_INPUT -> {
                val url = data?.extras?.getString(UrlInputActivity.URL_RESULT_KEY)
                if (url != null) {
                    viewModel.configureByUrl(url)
                }
            }
        }
    }

    private fun detectQrCode(bitmap: Bitmap) {
        detector.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { barcodes ->
                val url = barcodes.firstOrNull()?.rawValue
                if (url != null) {
                    Log.d(TAG, "detectQrCode success: $url")
                    viewModel.configureByUrl(url)
                } else {
                    Log.e(TAG, "detectQrCode error")
                    showErrorDialog(getString(R.string.no_qr_code_detected))
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "detectQrCode error: $error")
                showErrorDialog(error.toString())
            }
    }

    private fun checkCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            RequestCode.PERMISSION_REQUEST
        )
    }
}

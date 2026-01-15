package com.regula.idv.sample.url

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import com.regula.idv.sample.databinding.ActivityUrlInputBinding
import com.regula.idv.sample.utils.handleWindowInsets

class UrlInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUrlInputBinding

    companion object {
        const val URL_RESULT_KEY = "URL_RESULT_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUrlInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleWindowInsets(binding.root, WindowInsetsCompat.Type.statusBars())

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSubmit.setOnClickListener {
            val url = binding.etUrl.text.toString()
            finishWithResult(url)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun finishWithResult(url: String) {
        val intent = Intent()
        intent.putExtra(URL_RESULT_KEY, url)
        setResult(RESULT_OK, intent)
        finish()
    }
}

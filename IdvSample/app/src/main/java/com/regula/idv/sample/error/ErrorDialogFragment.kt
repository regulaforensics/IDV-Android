package com.regula.idv.sample.error

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.graphics.drawable.toDrawable
import com.regula.idv.sample.R
import com.regula.idv.sample.databinding.FragmentErrorBinding
import com.regula.idv.sample.utils.ClipboardManager
import com.regula.idv.sample.utils.requireString
import com.regula.idv.sample.utils.setArgs
import com.regula.idv.sample.utils.showShortToast

class ErrorDialogFragment : AppCompatDialogFragment() {

    companion object {
        val TAG = ErrorDialogFragment::class.simpleName

        private const val ERROR_MESSAGE_KEY = "ERROR_MESSAGE_KEY"

        fun newInstance(errorMessage: String): ErrorDialogFragment =
            ErrorDialogFragment().setArgs {
                putString(ERROR_MESSAGE_KEY, errorMessage)
            }
    }

    private lateinit var binding: FragmentErrorBinding
    private lateinit var errorMessage: String

    private val clipboardManager: ClipboardManager by lazy {
        ClipboardManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentErrorBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        isCancelable = false

        val args = requireArguments()
        errorMessage = args.requireString(ERROR_MESSAGE_KEY)

        setErrorMessage()
        setupListeners()
    }

    private fun setErrorMessage() {
        binding.tvErrorMessage.text = errorMessage
    }

    private fun setupListeners() {
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }
        binding.btnCopy.setOnClickListener {
            clipboardManager.copyToClipboard(errorMessage)
            showShortToast(R.string.copied_to_clipboard)
        }
    }
}

fun AppCompatActivity.showErrorDialog(errorMessage: String) {
    dismissErrorDialog()

    ErrorDialogFragment
        .newInstance(errorMessage)
        .show(supportFragmentManager, ErrorDialogFragment.TAG)
}

fun AppCompatActivity.dismissErrorDialog() {
    (supportFragmentManager.findFragmentByTag(ErrorDialogFragment.TAG)
            as? AppCompatDialogFragment)
        ?.dismiss()
}

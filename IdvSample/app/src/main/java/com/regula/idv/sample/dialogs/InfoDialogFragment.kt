package com.regula.idv.sample.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.graphics.drawable.toDrawable
import com.regula.idv.sample.databinding.FragmentInfoBinding
import com.regula.idv.sample.utils.requireString
import com.regula.idv.sample.utils.setArgs

class InfoDialogFragment : AppCompatDialogFragment() {

    companion object {
        val TAG = InfoDialogFragment::class.simpleName

        private const val MESSAGE_KEY = "MESSAGE_KEY"

        fun newInstance(errorMessage: String): InfoDialogFragment =
            InfoDialogFragment().setArgs {
                putString(MESSAGE_KEY, errorMessage)
            }
    }

    private lateinit var binding: FragmentInfoBinding
    private lateinit var message: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        isCancelable = false

        val args = requireArguments()
        message = args.requireString(MESSAGE_KEY)

        setErrorMessage()
        setupListeners()
    }

    private fun setErrorMessage() {
        binding.tvMessage.text = message
    }

    private fun setupListeners() {
        binding.btnDismiss.setOnClickListener {
            dismiss()
        }
    }
}

fun AppCompatActivity.showInfoDialog(message: String) {
    dismissInfoDialog()

    InfoDialogFragment
        .newInstance(message)
        .show(supportFragmentManager, InfoDialogFragment.TAG)
}

fun AppCompatActivity.dismissInfoDialog() {
    (supportFragmentManager.findFragmentByTag(InfoDialogFragment.TAG)
            as? AppCompatDialogFragment)
        ?.dismiss()
}

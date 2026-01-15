package com.regula.idv.sample.utils

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun <T : Fragment> T.setArgs(actionWithBundle: Bundle.() -> Unit): T =
    apply {
        arguments = Bundle().apply(actionWithBundle)
    }

fun Fragment.showShortToast(@StringRes textId: Int) {
    showShortToast(getString(textId))
}

fun Fragment.showShortToast(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
}

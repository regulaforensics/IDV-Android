package com.regula.idv.sample.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsCompat.Type.InsetsType

fun handleWindowInsets(
    view: View,
    @InsetsType insetsType: Int = Type.systemBars()
) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
        val insets = windowInsets.getInsets(insetsType)
        view.setPadding(
            insets.left,
            insets.top,
            insets.right,
            insets.bottom
        )
        WindowInsetsCompat.CONSUMED
    }
}

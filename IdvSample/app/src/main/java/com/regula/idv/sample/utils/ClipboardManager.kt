package com.regula.idv.sample.utils

import android.content.ClipData
import android.content.Context
import android.content.ClipboardManager as AndroidClipboardManager

class ClipboardManager(
    context: Context
) {

    private val androidClipboardManager: AndroidClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager

    fun copyToClipboard(text: CharSequence) {
        val clip = ClipData.newPlainText("label", text)
        androidClipboardManager.setPrimaryClip(clip)
    }
}

package com.regula.idv.sample.utils

import android.content.Context
import androidx.annotation.StringRes

data class LocalizedString(
    @StringRes val stringResId: Int
) {

    fun toPlainString(context: Context): String =
        context.getString(stringResId)
}

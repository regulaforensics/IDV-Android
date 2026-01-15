package com.regula.idv.sample.utils

import android.os.Bundle

fun Bundle.requireString(key: String): String =
    getString(key)
        ?: throw IllegalStateException("No value for key: $key")

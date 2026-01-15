package com.regula.idv.sample

import android.app.Application

class App : Application() {

    companion object {

        private lateinit var instance: App

        fun instance(): App =
            instance
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }
}

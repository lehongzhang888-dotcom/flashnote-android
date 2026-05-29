package com.flashnote.app

import android.app.Application

/**
 * Application 类 - 应用级初始化
 */
class FlashNoteApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        lateinit var instance: FlashNoteApp
            private set
    }
}

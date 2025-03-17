package com.wc.fatclip

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        var instance: App? = null
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
    }
}
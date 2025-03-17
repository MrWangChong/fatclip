package com.wc.fatclip

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
//        Handler().postDelayed({
//        }, 1000)
    }
}
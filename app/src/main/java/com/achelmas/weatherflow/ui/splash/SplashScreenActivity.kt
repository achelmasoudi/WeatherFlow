package com.achelmas.weatherflow.ui.splash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.ui.main.MainActivity

class SplashScreenActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_SPEED: Long = 2100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Set navigation bar color
        window.navigationBarColor = Color.parseColor("#973EAC")

        Thread {
            Thread.sleep(SPLASH_SPEED)
            runOnUiThread {
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }.start()
    }
}
package com.example.qrreader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlin.concurrent.thread

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        thread{
            Thread.sleep(1600)
            startActivity(Intent(this, MainActivity::class.java))
            Animatoo.animateSplit(this)
            finish()
        }


    }
}
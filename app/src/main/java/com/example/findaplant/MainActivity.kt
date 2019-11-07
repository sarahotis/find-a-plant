package com.example.findaplant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {


    internal var reportBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()

        reportBtn!!.setOnClickListener {
            if (notLoggedIn()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, ReportPlantActivity::class.java))
            }
        }

    }

    private fun initializeViews() {
        reportBtn = findViewById(R.id.report)
    }

    private fun notLoggedIn(): Boolean {
        return false
        //SharedPreferences
    }
}

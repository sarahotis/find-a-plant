package com.example.findaplant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.EventLogTags
import android.widget.Button

class MainActivity : AppCompatActivity() {


    internal var reportBtn: Button? = null
    internal var descriptionBtn: Button? = null
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

        descriptionBtn!!.setOnClickListener{
            startActivity(Intent(this@MainActivity, DescriptionActivity::class.java));
        }

    }

    private fun initializeViews() {
        reportBtn = findViewById(R.id.report)
        descriptionBtn = findViewById(R.id.description)
    }

    private fun notLoggedIn(): Boolean {
        return false
        //SharedPreferences
    }
}

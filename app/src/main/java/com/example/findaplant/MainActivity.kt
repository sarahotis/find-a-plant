package com.example.findaplant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.EventLogTags
import android.widget.Button
import android.util.Log


class MainActivity : AppCompatActivity() {


    internal var reportBtn: Button? = null
    internal var descriptionBtn: Button? = null
    internal var searchBtn: Button? = null
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
            Log.i("Description", "activity start")
            startActivity(Intent(this@MainActivity, DescriptionActivity::class.java))

        }

        searchBtn!!.setOnClickListener {
            Log.i("Search", "Search Activity Started")
            startActivity(Intent(this@MainActivity, SearchActivity::class.java))
        }

    }

    private fun initializeViews() {
        reportBtn = findViewById(R.id.report)
        descriptionBtn = findViewById(R.id.description)
        searchBtn = findViewById(R.id.search)
    }

    private fun notLoggedIn(): Boolean {
        return true
        //SharedPreferences
        //TODO: move firebase login checking here to save the user a step
    }
}

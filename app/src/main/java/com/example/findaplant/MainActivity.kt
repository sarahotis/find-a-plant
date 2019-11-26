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
            ReportPlantActivity.animate(it)
            if (notLoggedIn()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, ReportPlantActivity::class.java))
            }
        }

        descriptionBtn!!.setOnClickListener{
            Log.i("Description", "activity start")
            ReportPlantActivity.animate(it)
            startActivity(Intent(this@MainActivity, DescriptionActivity::class.java))

        }

        searchBtn!!.setOnClickListener {
            Log.i("Search", "Search Activity Started")
            ReportPlantActivity.animate(it)
            startActivity(Intent(this@MainActivity, SearchActivity::class.java))
        }

    }

    private fun initializeViews() {
        reportBtn = findViewById(R.id.report)
        ReportPlantActivity.setStrokes(reportBtn, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        descriptionBtn = findViewById(R.id.description)
        ReportPlantActivity.setStrokes(descriptionBtn, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        searchBtn = findViewById(R.id.search)
        ReportPlantActivity.setStrokes(searchBtn, ReportPlantActivity.LIGHT_ORANGE_COLOR)
    }

    private fun notLoggedIn(): Boolean {
        return false //switched off for developing ReportPlant
        //SharedPreferences
        //TODO: move firebase login checking here to save the user a step
    }
}

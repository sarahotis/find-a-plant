package com.example.findaplant

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide


class DescriptionActivity : AppCompatActivity(){

    private var plantImage : ImageView? = null
    private var plantName : TextView? = null
    private var plantDescription : TextView? = null
    private var backToMapButton : Button? = null
    private var backToMainButton : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.description_of_plant_layout)
        plantImage = findViewById(R.id.plantImage)
        plantName = findViewById(R.id.plantName)
        plantDescription = findViewById(R.id.plantDescription)
        backToMapButton = findViewById(R.id.backToMapButton)
        ReportPlantActivity.setStrokes(backToMapButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        backToMapButton?.setOnClickListener {
            ReportPlantActivity.animate(it)
            finish()
        }
        backToMainButton = findViewById(R.id.backToMainButton)
        ReportPlantActivity.setStrokes(backToMainButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        backToMainButton?.setOnClickListener {
            ReportPlantActivity.animate(it)
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        val plantIntent = intent
        plantName?.text = plantIntent.getStringExtra(MapsActivity.TITLE_KEY)

        // TODO: fix these null checks + make this work for user entered data
        val descriptionText = plantIntent.getStringExtra(MapsActivity.DESCRIPTION_KEY)
        if (descriptionText != null) {
            plantDescription?.text = descriptionText
            plantDescription?.visibility = View.VISIBLE
        }

        val plantURL = plantIntent.getStringExtra(MapsActivity.IMAGE_KEY)
        if (plantURL != null) {
            Glide.with(this)
                .load(plantURL)
                .into(plantImage!!)
        }


    }

}
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
<<<<<<< HEAD
=======
        val searchActivityIntent = intent
>>>>>>> 13ee32d4ad02d3812741167a6417babeb4ae82a0
        Log.i(TAG, "Entering Description Activity")

        setContentView(R.layout.description_of_plant_layout)
        plantImage = findViewById(R.id.plantImage)
        plantName = findViewById(R.id.plantName)
        plantName!!.setText("NEW TEXTTT")
        Log.i(TAG, "Plant name is " + plantName!!.text.toString())
        Log.i(TAG, "Plant name is " + plantName!!.text)
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

        // Intent can either come from SearchActivity or MapActivity
        if(MapsActivity.TITLE_KEY != null){

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

        }else{
            val plantIntent = intent
            plantName?.text = plantIntent.getStringExtra(SearchActivity.TITLE_KEY)

            // TODO: fix these null checks + make this work for user entered data
            val descriptionText = plantIntent.getStringExtra(SearchActivity.DESCRIPTION_KEY)
            if (descriptionText != null) {
                plantDescription?.text = descriptionText
                plantDescription?.visibility = View.VISIBLE
            }
        }



    }

    companion object{
        val TAG = "Description Activity"

    }

    companion object{
        val TAG = "Description Activity"
    }

}
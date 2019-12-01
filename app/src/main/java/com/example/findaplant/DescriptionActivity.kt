package com.example.findaplant

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import android.util.Log


class DescriptionActivity : AppCompatActivity(){

    private var plantImage : ImageView? = null
    private var plantName : TextView? = null
    private var plantDescription : TextView? = null
    private var backToMapButton : Button? = null
    private var backToMainButton : Button? = null
    private var cameFromSearchActivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Entering description activity")

        setContentView(R.layout.description_of_plant_layout)
        plantImage = findViewById(R.id.plantImage)
        plantName = findViewById(R.id.plantName)
        plantDescription = findViewById(R.id.plantDescription)
        backToMapButton = findViewById(R.id.backToMapButton)
        ReportPlantActivity.setStrokes(backToMapButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        backToMapButton?.setOnClickListener {
            if(!cameFromSearchActivity){
                Log.i(TAG, "Go to map button clicked")
                ReportPlantActivity.animate(it)
                finish()
            }else{
                //Intent came from Search Activity
                //Start map activity
                //get latitude and longitude from Firebase to put into map intent
                //get name, description and image taken to put into map intent
                //then call map activity

            }

        }
        backToMainButton = findViewById(R.id.backToMainButton)
        ReportPlantActivity.setStrokes(backToMainButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        backToMainButton?.setOnClickListener {
            //ReportPlantActivity.animate(it)
            Log.i(TAG, "Main button clicked")
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
        val plantIntent = intent
        // Intent can either come from SearchActivity or MapActivity
        if (plantIntent.getStringExtra(MapsActivity.TITLE_KEY) != null &&
            plantIntent.getStringExtra(MapsActivity.TITLE_KEY).isNotEmpty()) {

            Log.i(TAG, "Map Activity Intent Started")

            plantName?.text = plantIntent.getStringExtra(MapsActivity.TITLE_KEY)

            // TODO: fix these null checks + make this work for user entered data
            val descriptionText = plantIntent.getStringExtra(MapsActivity.DESCRIPTION_KEY)
            if (descriptionText != null) {
                plantDescription?.text = descriptionText
                plantDescription?.visibility = View.VISIBLE
            }

            val plantPicSource = plantIntent.getStringExtra(MapsActivity.IMAGE_KEY)
            // kinda hacky but whatever atm
            if (plantPicSource.contains("https")) { // URL
                Glide.with(this)
                    .load(plantPicSource)
                    .into(plantImage!!)
            } else { // Base64 encoded
                val byteArray = Base64.decode(plantPicSource, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                plantImage?.setImageBitmap(bitmap)
                plantImage?.rotation = 90f
            }

        } else {
            cameFromSearchActivity = true
            Log.i(TAG, "Intent came from searchActivity")

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
        const val TAG = "Description Activity"

    }
}
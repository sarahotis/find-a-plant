package com.example.findaplant

import android.content.Intent
import android.graphics.Bitmap
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
import java.lang.Exception
import java.net.URL
import java.util.concurrent.Executors


class DescriptionActivity : AppCompatActivity(){

    private var plantImage : ImageView? = null
    private var plantName : TextView? = null
    private var plantDescription : TextView? = null
    private var backToMapButton : Button? = null
    private var backToMainButton : Button? = null
    private var cameFromSearchActivity: Boolean = false
    private lateinit var plantIntent: Intent
    private lateinit var mapIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Entering description activity")

        //Get intent
        plantIntent = intent

        //TODO: Show image of plant on description

        setContentView(R.layout.description_of_plant_layout)
        plantImage = findViewById(R.id.plantImage)
        plantName = findViewById(R.id.plantName)
        plantDescription = findViewById(R.id.plantDescription)
        backToMapButton = findViewById(R.id.backToMapButton)
        ReportPlantActivity.setStrokes(backToMapButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        backToMapButton?.setOnClickListener {
            goToMap(it)
        }
        backToMainButton = findViewById(R.id.backToMainButton)
        ReportPlantActivity.setStrokes(backToMainButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        backToMainButton?.setOnClickListener {
            //ReportPlantActivity.animate(it)
            Log.i(TAG, "Main button clicked")
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        // Intent can either come from SearchActivity or MapActivity
        Log.i(TAG, "Title is " + plantIntent.getStringExtra(MapsActivity.TITLE_KEY))

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
            Log.i(TAG, "Title is " + plantName?.text + " description " + descriptionText)
            if (descriptionText != null) {
                plantDescription?.text = descriptionText
                plantDescription?.visibility = View.VISIBLE
            }
            Log.i(TAG, "Loads plant url into plant image")
            val imageURL = plantIntent.getStringExtra(SearchActivity.IMAGE_KEY)
            if (imageURL.contains("https")) { // URL
                Glide.with(this)
                    .load(imageURL)
                    .into(plantImage!!)
            } else { // Base64 encoded
                val byteArray = Base64.decode(imageURL, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                plantImage?.setImageBitmap(bitmap)
                plantImage?.rotation = 90f
            }
        }

    }

    private fun goToMap(it:View){
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
            mapIntent = Intent(this, MapsActivity::class.java)
            mapIntent.putExtra(PLANT_NAME_KEY, plantIntent.getStringExtra(SearchActivity.TITLE_KEY))
            Log.i("Plant name is ", plantName!!.text.toString())
            Log.i("Plant name is from Description Activity", plantIntent.getStringExtra(SearchActivity.TITLE_KEY))
            Log.i("Description name is from Description Activity", plantIntent.getStringExtra(SearchActivity.DESCRIPTION_KEY))
            mapIntent.putExtra(PLANT_DESC_KEY, plantIntent.getStringExtra(SearchActivity.DESCRIPTION_KEY))
            val latitude = plantIntent.getDoubleExtra(SearchActivity.LATITUDE, DEFAULT_LAT)
            val longitude = plantIntent.getDoubleExtra(SearchActivity.LONGITUDE, DEFAULT_LONG)
            val imageURL = plantIntent.getStringExtra(SearchActivity.IMAGE_KEY)
            Log.i(TAG, "Latitude " + latitude)
            Log.i(TAG, "Longitude " + longitude)
            mapIntent.putExtra(LATITUDE_KEY, latitude)
            mapIntent.putExtra(LONGITUDE_KEY, longitude)
            mapIntent.putExtra(IMAGE_KEY, imageURL)
            mapIntent.putExtra(NOT_A_REPORT, true)
            startActivity(mapIntent)

            /***Convert image URL into bitmap ***/


        }
    }

    /*** Android does not allow accessing internet from main thread so use background thread***/
//    private fun convertImageToBitmap(imageURL : String) : Boolean{
//        //Check if URL is null
//        if(imageURL == null){
//            return false
//        }
//        //check if image URL is an empty string
//        if(imageURL.length == 0){
//            return false
//        }
//
//        Executors.newSingleThreadExecutor().execute{
//            Log.i(TAG, "Entering thread to convert to bitmap")
//
//            val imageToURL = URL(imageURL)
//            //open http connection
//            val connection = imageToURL.openConnection()
//            //connect the connection
//            connection.connect()
//            //get Input Stream
//            val input = connection.getInputStream()
//            //turn to BitMap
//            val bitMap = BitmapFactory.decodeStream(input)
//            Log.i(TAG, "Bitmap is " + bitMap)
//            mapIntent.putExtra(IMAGE_KEY, bitMap)
//
//
//        }
//        return true
//    }


    companion object{
        const val TAG = "Description Activity"
        const val LATITUDE_KEY = "LATITUDE_KEY"
        const val LONGITUDE_KEY = "LONGITUDE_KEY"
        const val PLANT_NAME_KEY = "PLANT_NAME_KEY"
        const val PLANT_DESC_KEY = "PLANT_DESC_KEY"
        const val IMAGE_KEY = "IMAGE_KEY"
        const val DEFAULT_LAT = 38.9858
        const val DEFAULT_LONG = -76.9373
        const val NOT_A_REPORT = "Not a report"

    }
}
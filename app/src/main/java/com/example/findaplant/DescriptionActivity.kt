package com.example.findaplant

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class DescriptionActivity : AppCompatActivity(){

    private var plantImage : ImageView? = null
    private var plantName : TextView? = null
    private var plantDescription : TextView? = null
    private var backToMapButton : Button? = null
    private var backToMainButton : Button? = null
    private var cameFromSearchActivity: Boolean = false
    private lateinit var plantIntent: Intent
    private lateinit var mapIntent: Intent
    private var toolbar: Toolbar? = null
    private var drawable: Drawable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get intent
        plantIntent = intent

        setContentView(R.layout.description_of_plant_layout)

        //toolbar for account icon
        toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        drawable = ContextCompat.getDrawable(this, R.drawable.leaf_icon)
        toolbar?.setNavigationIcon(drawable)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);

        toolbar?.setNavigationOnClickListener {
            startActivity(Intent(this@DescriptionActivity, MainActivity::class.java))
        }

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
            ReportPlantActivity.animate(it)
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        // Intent can either come from SearchActivity or MapActivity

        if (plantIntent.getStringExtra(MapsActivity.TITLE_KEY) != null &&
            plantIntent.getStringExtra(MapsActivity.TITLE_KEY).isNotEmpty()) {

            plantName?.text = plantIntent.getStringExtra(MapsActivity.TITLE_KEY)

            val descriptionText = plantIntent.getStringExtra(MapsActivity.DESCRIPTION_KEY)
            if (descriptionText != null) {
                plantDescription?.text = descriptionText
                plantDescription?.visibility = View.VISIBLE
            }

            val plantPicSource = plantIntent.getStringExtra(MapsActivity.IMAGE_KEY)
            //Uploads plant picture if its a url
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

            plantName?.text = plantIntent.getStringExtra(SearchActivity.TITLE_KEY)
            val descriptionText = plantIntent.getStringExtra(SearchActivity.DESCRIPTION_KEY)
            if (descriptionText != null) {
                plantDescription?.text = descriptionText
                plantDescription?.visibility = View.VISIBLE
            }
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
        ReportPlantActivity.animate(it)
        if (!cameFromSearchActivity) {
            finish()
        } else {
            //Intent came from Search Activity
            //Start map activity
            //get latitude and longitude from Firebase to put into map intent
            //get name, description and image taken to put into map intent
            //then call map activity
            mapIntent = Intent(this, MapsActivity::class.java)
            mapIntent.putExtra(PLANT_NAME_KEY, plantIntent.getStringExtra(SearchActivity.TITLE_KEY))
            mapIntent.putExtra(PLANT_DESC_KEY, plantIntent.getStringExtra(SearchActivity.DESCRIPTION_KEY))
            val latitude = plantIntent.getDoubleExtra(SearchActivity.LATITUDE, DEFAULT_LAT)
            val longitude = plantIntent.getDoubleExtra(SearchActivity.LONGITUDE, DEFAULT_LONG)
            val imageURL = plantIntent.getStringExtra(SearchActivity.IMAGE_KEY)
            val latitudeGeocode = plantIntent.getDoubleExtra(SearchActivity.LATITUDE_FROM_GEOCODER, -0.0)
            val longitudeGeocode = plantIntent.getDoubleExtra(SearchActivity.LONGITUDE_FROM_GEOCODER, -0.0)
            mapIntent.putExtra(LATITUDE_KEY, latitude)
            mapIntent.putExtra(LONGITUDE_KEY, longitude)
            mapIntent.putExtra(IMAGE_KEY, imageURL)
            mapIntent.putExtra(NOT_A_REPORT, true)
            mapIntent.putExtra(SearchActivity.LATITUDE_FROM_GEOCODER, latitudeGeocode)
            mapIntent.putExtra(SearchActivity.LONGITUDE_FROM_GEOCODER, longitudeGeocode)
            startActivity(mapIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.example.findaplant.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_account -> {
            startActivity(Intent(this@DescriptionActivity, ProfileActivity::class.java))
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

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
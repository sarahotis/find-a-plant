package com.example.findaplant

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var databasePlants: DatabaseReference // For FireBase stuff
    private var backToMainButton : Button? = null //TODO: eliminate code duplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        backToMainButton = findViewById(R.id.backToMainButton)
        ReportPlantActivity.setStrokes(backToMainButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        backToMainButton?.setOnClickListener {
            ReportPlantActivity.animate(it)
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val mapIntent = intent
        val plantName = mapIntent.getStringExtra(ReportPlantActivity.PLANT_NAME_KEY).capitalizeWords()
        val plantDesc = mapIntent.getStringExtra(ReportPlantActivity.PLANT_DESC_KEY)
        val latitude = mapIntent.getDoubleExtra(ReportPlantActivity.LATITUDE_KEY, DEFAULT_LAT)
        val longitude = mapIntent.getDoubleExtra(ReportPlantActivity.LONGITUDE_KEY, DEFAULT_LONG)


        // Add a marker at plant location and move the camera
        val marker = LatLng(latitude, longitude)
        if (plantDesc.isEmpty()) {
            mMap.addMarker(MarkerOptions()
                .position(marker)
                .title(plantName)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.plant))) // can also use R.drawable.flower
        } else {
            mMap.addMarker(MarkerOptions()
                .position(marker)
                .flat(true)
                .title(plantName)
                .snippet(plantDesc + "\nClick for more info!") //TODO: expand this InfoWindow based on text length - TextViews in a layout file
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.plant))) // can also use R.drawable.flower
        }
        // Max zoom out level (20 = buildings, 1 = world)
        mMap.setMinZoomPreference(MINIMUM_ZOOM_LEVEL)
        //Camera initially appears at zoom level 15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, INITIAL_ZOOM_LEVEL));

        /* Add all the plants in the database previously to the map */
        val database = FirebaseDatabase.getInstance()
        databasePlants = database.getReference("Greenbelt Plants")
        databasePlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children) {
                    val name = postSnapshot.child("common_name").value as String
                    if (name.isNotEmpty()) { // Make sure plant has a name
                        // Get latitude and longitude from database
                        val datLat = postSnapshot.child("latitude").value as Double
                        val datLong = postSnapshot.child("longitude").value as Double
                        val imageURL = postSnapshot.child("image_url").value as String

                        // Add plant marker to map
                        val datLocation = LatLng(datLat, datLong)
                        val datMarker = mMap.addMarker(MarkerOptions()
                            .position(datLocation)
                            .title(name.capitalizeWords())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.flower))) // can also use R.drawable.plant
                        datMarker.tag = imageURL // Tag used to store image of plant on marker

                        /*Glide.with(applicationContext)
                            .asBitmap()
                            .load(imageURL)
                            .fitCenter()
                            .into(object : SimpleTarget<Bitmap>(150, 150) {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    mMap.addMarker(MarkerOptions()
                                        .position(datLocation)
                                        .title(name.capitalizeWords())
                                        .icon(BitmapDescriptorFactory.fromBitmap(resource)))
                                }
                            })*/
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })

        mMap.setOnInfoWindowClickListener {
            val descriptionIntent = Intent(this, DescriptionActivity::class.java)
            descriptionIntent.putExtra(TITLE_KEY, it.title)
            descriptionIntent.putExtra(DESCRIPTION_KEY, it.snippet)
            if (it.tag != null) {
                descriptionIntent.putExtra(IMAGE_KEY, it.tag as String)
            }
            startActivity(descriptionIntent)
        }
    }

    fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

    companion object {
        const val TITLE_KEY = "TITLE_KEY"
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY"
        const val IMAGE_KEY = "IMAGE_KEY"
        const val INITIAL_ZOOM_LEVEL = 16f
        const val MINIMUM_ZOOM_LEVEL = 10f
        // Note: Default lat/long is UMD
        const val DEFAULT_LAT = 38.9858
        const val DEFAULT_LONG = -76.9373
    }
}

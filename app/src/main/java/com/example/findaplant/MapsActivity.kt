package com.example.findaplant

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var databasePlants: DatabaseReference // For FireBase stuff
    private var backToMainButton : Button? = null

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
        val database = FirebaseDatabase.getInstance()


        val databaseUserPlants = database.getReference("User Plants")

        // Get name, description, location, and image that user reported
        val plantName = mapIntent.getStringExtra(ReportPlantActivity.PLANT_NAME_KEY).capitalizeWords()
        val plantDesc = mapIntent.getStringExtra(ReportPlantActivity.PLANT_DESC_KEY)
        val latitude = mapIntent.getDoubleExtra(ReportPlantActivity.LATITUDE_KEY, DEFAULT_LAT)
        val longitude = mapIntent.getDoubleExtra(ReportPlantActivity.LONGITUDE_KEY, DEFAULT_LONG)
        // Base64 encoding for uploading Bitmap image to firebase
        val image = mapIntent.getParcelableExtra<Bitmap>(ReportPlantActivity.IMAGE_KEY)
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
        val timeStamp = System.currentTimeMillis()
        // Add plant report to database
        databaseUserPlants.child(timeStamp.toString()).child("common_name").setValue(plantName)
        databaseUserPlants.child(timeStamp.toString()).child("latitude").setValue(latitude)
        databaseUserPlants.child(timeStamp.toString()).child("longitude").setValue(longitude)
        databaseUserPlants.child(timeStamp.toString()).child("image").setValue(imageEncoded)
        databaseUserPlants.child(timeStamp.toString()).child("description").setValue(plantDesc)

        /* Add all the plants in the User's database previously to the map */
        databaseUserPlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children) {

                    val name = postSnapshot.child("common_name").value as String
                    val datLat = postSnapshot.child("latitude").value as Double
                    val datLong = postSnapshot.child("longitude").value as Double
                    val datDesc = postSnapshot.child("description").value as String
                    val datImage = postSnapshot.child("image").value as String

                    // Add plant marker to map
                    val datLocation = LatLng(datLat, datLong)
                    lateinit var datMarker : Marker
                    if (datDesc.isEmpty()) {
                        datMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(datLocation)
                                .title(name.capitalizeWords())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flower))
                        ) // can also use R.drawable.plant
                    } else {
                        datMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(datLocation)
                                .title(name.capitalizeWords())
                                .snippet(datDesc)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flower))
                        ) // can also use R.drawable.pl
                    }
                    datMarker.tag = datImage // Tag used to store image of plant on marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(datLocation, INITIAL_ZOOM_LEVEL))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })

        /* Add all the plants in the Plants Added To FB database previously to the map */
        databasePlants = database.getReference("Plants Added To FB")
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
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })

        // Max zoom out level (20 = buildings, 1 = world)
        mMap.setMinZoomPreference(MINIMUM_ZOOM_LEVEL)
        //Camera initially appears at zoom level 15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, INITIAL_ZOOM_LEVEL));

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

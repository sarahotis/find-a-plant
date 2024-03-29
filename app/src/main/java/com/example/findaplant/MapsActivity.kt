package com.example.findaplant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.MenuItem
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
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var databasePlants: DatabaseReference // For FireBase stuff
    private var backToMainButton : Button? = null
    private lateinit var plantName: String
    private lateinit var plantDesc : String
    private lateinit var latLng: LatLng
    private var imageURL: String? = null
    private var toolbar: Toolbar? = null
    private var drawable: Drawable? = null
    private var latitudeGeocode: Double = 0.0
    private var longitudeGeocode: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        Toast.makeText(this, "Loading map...",
            Toast.LENGTH_LONG).show();
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        //toolbar for account icon
        toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        drawable = ContextCompat.getDrawable(this, R.drawable.leaf_icon)
        toolbar?.setNavigationIcon(drawable)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);

        toolbar?.setNavigationOnClickListener {
            startActivity(Intent(this@MapsActivity, MainActivity::class.java))
        }


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

    /*** Check if intent came from ReportPlantActivity or is a plant that is searched in the database***/
    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(this, "Click plant names for more info!",
            Toast.LENGTH_LONG).show();
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true // Blue dot representing user
        }

        val mapIntent = intent
        val database = FirebaseDatabase.getInstance()
        val databaseUserPlants = database.getReference("User Plants")

        val notAPlantReport = mapIntent.getBooleanExtra(DescriptionActivity.NOT_A_REPORT, false)
        val searchPlantsBasedOnLocation = mapIntent.getBooleanExtra(SearchActivity.SEARCH_FOR_PLANTS_BY_LOCATION, false)
        /*** Check if its intent is from ReportPlantActivity or location is being searched. ***/
        //It is a plant report, putting new plant in Firebase
        if(!notAPlantReport && !searchPlantsBasedOnLocation){
            // Get name, description, location, and image that user reported
            plantName = mapIntent.getStringExtra(ReportPlantActivity.PLANT_NAME_KEY).capitalizeWords()
            plantDesc = mapIntent.getStringExtra(ReportPlantActivity.PLANT_DESC_KEY)
            val latitude = mapIntent.getDoubleExtra(ReportPlantActivity.LATITUDE_KEY, DEFAULT_LAT)
            val longitude = mapIntent.getDoubleExtra(ReportPlantActivity.LONGITUDE_KEY, DEFAULT_LONG)
            latLng = LatLng(latitude, longitude)
            // Base64 encoding for uploading Bitmap image to firebase
            val image = mapIntent.getParcelableExtra<Bitmap>(ReportPlantActivity.IMAGE_KEY)
            val baos = ByteArrayOutputStream()
            image!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            val timeStamp = System.currentTimeMillis()
            // Add plant report to database
            databaseUserPlants.child(timeStamp.toString()).child("common_name").setValue(plantName)
            databaseUserPlants.child(timeStamp.toString()).child("latitude").setValue(latitude)
            databaseUserPlants.child(timeStamp.toString()).child("longitude").setValue(longitude)
            databaseUserPlants.child(timeStamp.toString()).child("image").setValue(imageEncoded)
            databaseUserPlants.child(timeStamp.toString()).child("description").setValue(plantDesc)
        } else {
            //Searching for the plants in the specified location
            if(searchPlantsBasedOnLocation){
                //If searching for a plant by locations
                latitudeGeocode= mapIntent.getDoubleExtra(SearchActivity.LATITUDE_FROM_GEOCODER, 0.0)
                longitudeGeocode = mapIntent.getDoubleExtra(SearchActivity.LONGITUDE_FROM_GEOCODER, 0.0)
            }else{
                //Searching for a specific plant
                plantName = mapIntent.getStringExtra(DescriptionActivity.PLANT_NAME_KEY).capitalizeWords()
                plantDesc = mapIntent.getStringExtra(DescriptionActivity.PLANT_DESC_KEY)
                val latitude = mapIntent.getDoubleExtra(DescriptionActivity.LATITUDE_KEY, DEFAULT_LAT)
                val longitude = mapIntent.getDoubleExtra(DescriptionActivity.LONGITUDE_KEY, DEFAULT_LONG)
                latLng = LatLng(latitude, longitude)
                imageURL = mapIntent.getStringExtra(DescriptionActivity.IMAGE_KEY)
            }
        }

        /* Add all the plants in the Plants Added To FB database previously to the map */
        databasePlants = database.getReference("Plants Added To FB")
        databasePlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children) {
                    val name = postSnapshot.child("common_name").value as? String
                    if (name!!.isNotEmpty()) { // Make sure plant has a name
                        // Get latitude and longitude from database
                        val datLat = postSnapshot.child("latitude").value as? Double
                        val datLong = postSnapshot.child("longitude").value as? Double
                        val imageURL = postSnapshot.child("image_url").value as? String

                        if (searchPlantsBasedOnLocation) { // Only add marker if within provided location range
                            val latitudeGeocode = mapIntent.getDoubleExtra(SearchActivity.LATITUDE_FROM_GEOCODER, -0.0)
                            val longitudeGeocode = mapIntent.getDoubleExtra(SearchActivity.LONGITUDE_FROM_GEOCODER, -0.0)
                            if (latitudeGeocode != 0.0 && longitudeGeocode != 0.0
                                && withinRange(latitudeGeocode, longitudeGeocode, datLat!!, datLong!!)) {
                                // Add plant marker to map
                                val datLocation = LatLng(datLat!!, datLong!!)
                                val datMarker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(datLocation)
                                        .title(name.capitalizeWords())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                ) // can also use R.drawable.plant
                                datMarker.tag = imageURL // Tag used to store image of plant on marker
                            }
                        } else {
                            // Add plant marker to map
                            val datLocation = LatLng(datLat!!, datLong!!)
                            val datMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(datLocation)
                                    .title(name.capitalizeWords())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                            ) // can also use R.drawable.plant
                            datMarker.tag = imageURL // Tag used to store image of plant on marker
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })

        /* Add all the plants in the User's database previously to the map */
        databaseUserPlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children) {

                    //Added as? to avoid error "null cannot be cast to non-null type"
                    val name = postSnapshot.child("common_name").value as? String ?: continue
                    var datLat = postSnapshot.child("latitude").value as? Double
                    var datLong = postSnapshot.child("longitude").value as? Double
                    var datDesc = postSnapshot.child("description").value as? String
                    var datImage = postSnapshot.child("image").value as? String

                    // Add plant marker to map
                    if (datLat != null && datLong != null && datDesc != null && datImage != null) {
                        //Finding plants based on the searched location
                        if (searchPlantsBasedOnLocation) { // Only add marker within range
                            val latitudeGeocode = mapIntent.getDoubleExtra(SearchActivity.LATITUDE_FROM_GEOCODER, -0.0)
                            val longitudeGeocode = mapIntent.getDoubleExtra(SearchActivity.LONGITUDE_FROM_GEOCODER, -0.0)
                            if (latitudeGeocode != 0.0 && longitudeGeocode != 0.0
                                && withinRange(latitudeGeocode, longitudeGeocode, datLat!!, datLong!!)) {
                                val datLocation = LatLng(datLat!!, datLong!!)
                                lateinit var datMarker: Marker
                                if (datDesc.isEmpty()) {
                                    datMarker = mMap.addMarker(
                                        MarkerOptions()
                                            .position(datLocation)
                                            .title(name.capitalizeWords())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                    ) // can also use R.drawable.plant
                                } else {
                                    datMarker = mMap.addMarker(
                                        MarkerOptions()
                                            .position(datLocation)
                                            .title(name.capitalizeWords())
                                            .snippet(datDesc)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                    ) // can also use R.drawable.plant
                                }
                                //If its a plant report put marker on last entry
                                datMarker.tag =
                                    datImage // Tag used to store image of plant on marker
                                if (!notAPlantReport && !searchPlantsBasedOnLocation) {
                                    mMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            datLocation,
                                            INITIAL_ZOOM_LEVEL
                                        )
                                    )
                                }
                            }
                        } else {
                            val datLocation = LatLng(datLat!!, datLong!!)
                            lateinit var datMarker: Marker
                            if (datDesc.isEmpty()) {
                                datMarker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(datLocation)
                                        .title(name.capitalizeWords())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                ) // can also use R.drawable.plant
                            } else {
                                datMarker = mMap.addMarker(
                                    MarkerOptions()
                                        .position(datLocation)
                                        .title(name.capitalizeWords())
                                        .snippet(datDesc)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                ) // can also use R.drawable.plant
                            }
                            //If its a plant report put marker on last entry
                            datMarker.tag = datImage // Tag used to store image of plant on marker
                            if (!notAPlantReport && !searchPlantsBasedOnLocation) {
                                mMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        datLocation,
                                        INITIAL_ZOOM_LEVEL
                                    )
                                )
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })

        /** Check if plant is searched in the database or reported. Also check if
         * we're searching for a plant or plants in a location. If searching for plant then move
        marker to searched plant location **/
        if(notAPlantReport && !searchPlantsBasedOnLocation){
            //Plant information came from DescriptionActivity
            if(plantDesc.isNotEmpty()) {
                val datMarker = mMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(plantName.capitalizeWords())
                    .snippet(plantDesc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))) // can also use R.drawable.plant
                datMarker.tag = imageURL // Tag used to store image of plant on marker
            } else {
                val datMarker = mMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(plantName.capitalizeWords())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))) // can also use R.drawable.plant
                datMarker.tag = imageURL // Tag used to store image of plant on marker
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM_LEVEL))
        }else{
            //Location is being searched so move camera to location searched
            val searchedLocation = LatLng(latitudeGeocode!!, longitudeGeocode!!)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, INITIAL_ZOOM_LEVEL))
        }


        // Max zoom out level (20 = buildings, 1 = world)
        mMap.setMinZoomPreference(MINIMUM_ZOOM_LEVEL)

        /** Bring up DescriptionActivity when marker content clicked */
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.example.findaplant.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_account -> {
            startActivity(Intent(this@MapsActivity, ProfileActivity::class.java))
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

    /** Return true if start location is within MAX_DISTANCE_METERS of end location */
    fun withinRange(startLatitude : Double, startLongitude : Double,
                    endLatitude : Double, endLongitude : Double) : Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results)
        Log.d(TAG, "results[0]: " + results[0])
        return results[0] < MAX_DISTANCE_METERS
    }

    companion object {
        const val TITLE_KEY = "TITLE_KEY_FROM_MAP"
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY_FROM_MAP"
        const val IMAGE_KEY = "IMAGE_KEY_FROM_MAP"
        const val INITIAL_ZOOM_LEVEL = 16f
        const val MINIMUM_ZOOM_LEVEL = 10f
        // Note: Default lat/long is UMD
        const val DEFAULT_LAT = 38.9858
        const val DEFAULT_LONG = -76.9373
        const val MAX_DISTANCE_METERS = 100
        const val TAG = "Map Activity"
    }
}
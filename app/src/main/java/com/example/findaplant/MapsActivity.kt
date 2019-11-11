package com.example.findaplant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        val plantName = mapIntent.getStringExtra(ReportPlantActivity.PLANT_NAME_KEY)
        val plantDesc = mapIntent.getStringExtra(ReportPlantActivity.PLANT_DESC_KEY)
        // Note: Default lat/long is UMD
        val latitude = mapIntent.getDoubleExtra(ReportPlantActivity.LATITUDE_KEY, 38.9858)
        val longitude = mapIntent.getDoubleExtra(ReportPlantActivity.LONGITUDE_KEY, -76.9373)


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
                .title(plantName)
                .snippet(plantDesc)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.plant))) // can also use R.drawable.flower
        }
        mMap.setMinZoomPreference(15.toFloat()) // Set zoom level (15 = streets, 1 = world)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
    }
}

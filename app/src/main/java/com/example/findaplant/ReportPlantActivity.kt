package com.example.findaplant

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class ReportPlantActivity : AppCompatActivity() {

    var reportImageView : ImageView? = null
    var helpIdentifyButton : Button? = null
    var reportPlantButton : Button? = null
    var reportPlantEditText : EditText? = null
    var reportDescEditText : EditText? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
        } else {
            setupViews()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setupViews()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    setContentView(R.layout.report_plant_no_location)
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /** Replace current image view picture with thumbnail of image taken by user */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            reportImageView?.setImageBitmap(imageBitmap)
        }
    }

    /**
     * Launch the camera when the image view in clicked
     */
    fun imageViewOnClick(v: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    /**
     * Adds plant to database and map
     */
    fun reportPlantOnClick(v: View) {
        animate(v) // Animate button color change
        val plantName = reportPlantEditText?.text.toString().trim() // Get plant's name
        if (plantName.isEmpty()) { // If no name entered do not add to database, prompt user for entry
            val errorToast = Toast.makeText(this, R.string.blank_plant_name, Toast.LENGTH_LONG)
            errorToast.setGravity(Gravity.CENTER, 0, 0)
            errorToast.show()
        } else {
            val mapsIntent = Intent(this, MapsActivity::class.java) // Intent to launch map with plant marker

            mapsIntent.putExtra(PLANT_NAME_KEY, plantName) // Store name for plant marker on map
            val plantDesc = reportDescEditText?.text.toString().trim() // Store extra info about plant
            mapsIntent.putExtra(PLANT_DESC_KEY, plantDesc)

            // Get last location of phone for logging the plant location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Note: Default for android emulator is somewhere in Mountain View, must manually set
                        // Store longitude and latitude for plant marker on map
                        mapsIntent.putExtra(LATITUDE_KEY, location.latitude)
                        mapsIntent.putExtra(LONGITUDE_KEY, location.longitude)
                    }
                }
            // TODO: Add plant to database
            // TODO: Error handling if no picture taken
            startActivity(mapsIntent)
        }
    }

    /**
     * Run MLKit to identify plant if user does not know the species name
     */
    fun helpIdentifyOnClick(v: View) {
        animate(v) // Animate button color change
        // TODO: Firebase MlKit stuff here
    }

    private fun animate(v: View?) {
        val oldColor = Color.parseColor("#FFFFFF")
        val newColor = Color.parseColor(LIGHT_ORANGE_COLOR)

        /* Animate background color from white to newColor */
        val colorAnimationForward = ValueAnimator.ofObject(ArgbEvaluator(), oldColor, newColor)
        colorAnimationForward.duration = 250
        val temp = ValueAnimator.AnimatorUpdateListener {
            val drawable = v?.background as GradientDrawable
            drawable.setColor(it.animatedValue as Int)
        }
        colorAnimationForward.addUpdateListener(temp)
        colorAnimationForward.start()

        /* Animate back to white from newColor */
        val colorAnimationBackward = ValueAnimator.ofObject(ArgbEvaluator(), newColor, oldColor)
        colorAnimationBackward.duration = 250
        val temp2 = ValueAnimator.AnimatorUpdateListener {
            val drawable = v?.background as GradientDrawable
            drawable.setColor(it.animatedValue as Int)
        }
        colorAnimationBackward.addUpdateListener(temp2)
        colorAnimationBackward.startDelay = 250
        colorAnimationBackward.start()
    }

    private fun setStrokes(button: Button?, colorString : String) {
        val drawable = button?.background as GradientDrawable
        drawable.setStroke(16, Color.parseColor(colorString))
    }

    private fun setupViews() {
        setContentView(R.layout.report_plant_layout)
        reportImageView = findViewById(R.id.reportImageView)
        helpIdentifyButton = findViewById(R.id.helpIdentifyButton)
        reportPlantButton = findViewById(R.id.reportPlantButton)
        reportPlantEditText = findViewById(R.id.reportPlantEditText)
        reportDescEditText = findViewById(R.id.reportDescEditText)

        // Set stroke (border) and body color of button
        setStrokes(helpIdentifyButton, LIGHT_ORANGE_COLOR)
        setStrokes(reportPlantButton, LIGHT_ORANGE_COLOR)
    }

    companion object {
        val TAG = "ReportPlantActivity"
        val MY_PERMISSIONS_REQUEST_LOCATION = 1
        val REQUEST_IMAGE_CAPTURE = 1
        const val LIGHT_ORANGE_COLOR = "#FCB97D"
        const val LATITUDE_KEY = "LATITUDE_KEY"
        const val LONGITUDE_KEY = "LONGITUDE_KEY"
        const val PLANT_NAME_KEY = "PLANT_NAME_KEY"
        const val PLANT_DESC_KEY = "PLANT_DESC_KEY"
    }

}
package com.example.findaplant

import android.Manifest
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



class ReportPlantActivity : AppCompatActivity() {

    var reportImageView : ImageView? = null
    var helpIdentifyButton : Button? = null
    var reportPlantButton : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
        } else {
            setContentView(R.layout.report_plant_layout)
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
                    setContentView(R.layout.report_plant_layout)
                    reportImageView = findViewById(R.id.reportImageView)
                    helpIdentifyButton = findViewById(R.id.helpIdentifyButton)
                    reportPlantButton = findViewById(R.id.reportPlantButton)

                    // Set stroke (border) and body color of button
                    setStrokes(helpIdentifyButton, "#FCB97D")
                    setStrokes(reportPlantButton, "#FCB97D")
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

    fun reportPlantOnClick(v: View) {

    }

    fun helpIdentifyOnClick(v: View) {

    }

    private fun setStrokes(button: Button?, colorString : String) {
        val drawable = button?.background as GradientDrawable
        drawable.setStroke(16, Color.parseColor(colorString))
    }

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 1
        val REQUEST_IMAGE_CAPTURE = 1
    }

}
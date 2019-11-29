package com.example.findaplant

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions

class ReportPlantActivity : AppCompatActivity() {

    var reportImageView : ImageView? = null // Holds image of plant (camera by default)
    var helpIdentifyButton : Button? = null // Click to launch MLKit (eventually)
    var permissionsButton : Button? = null // Click to launch permissions page
    var reportPlantButton : Button? = null // Click to report plant and go to map
    var reportPlantEditText : EditText? = null // Place to enter plant name
    var reportDescEditText : EditText? = null // Place to enter optional plant description
    var imageTakenBool = false // Determines if plant picture was taken
    lateinit var imageTaken : Bitmap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestPermissions() {
        val permissionsNeeded = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.remove(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.remove(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.remove(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsNeeded.size > 0) { // Permissions array cannot be empty/null
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                MY_PERMISSIONS_REQUEST
            )
        }
        setupViews()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Setup the views
                    setupViews()
                } else {
                    // permission denied, boo! Different layout w/ prompt to enable permissions
                    setContentView(R.layout.report_plant_no_permissions)
                    permissionsButton = findViewById(R.id.enable_permissions_button)
                    // Clickable button that opens permissions page so user can enable them
                    permissionsButton?.setOnClickListener {
                        val permissionIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + packageName))
                        permissionIntent.addCategory(Intent.CATEGORY_DEFAULT)
                        permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(permissionIntent)
                    }
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /** Replace current image view picture with thumbnail of image taken by user */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageTaken = imageBitmap // Save image taken to global variable for later storage
            reportImageView?.setImageBitmap(imageBitmap)
            reportImageView?.rotation = 90f
            imageTakenBool = true
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
        }

        if (plantName.isNotEmpty() && !imageTakenBool) { // No image taken, prompt user for entry
            val errorToast = Toast.makeText(this, R.string.blank_plant_image, Toast.LENGTH_LONG)
            errorToast.setGravity(Gravity.CENTER, 0, 0)
            errorToast.show()
        }

        if (plantName.isNotEmpty() && imageTakenBool) {
            val mapsIntent = Intent(this, MapsActivity::class.java) // Intent to launch map with plant marker

            mapsIntent.putExtra(PLANT_NAME_KEY, plantName) // Store name for plant marker on map
            val plantDesc = reportDescEditText?.text.toString().trim() // Store extra info about plant
            mapsIntent.putExtra(PLANT_DESC_KEY, plantDesc)
            mapsIntent.putExtra(IMAGE_KEY, imageTaken)

            // Get last location of phone for logging the plant location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Note: Default for android emulator is somewhere in Mountain View, must manually set
                        // Store longitude and latitude for plant marker on map
                        mapsIntent.putExtra(LATITUDE_KEY, location.latitude)
                        mapsIntent.putExtra(LONGITUDE_KEY, location.longitude)
                        startActivity(mapsIntent)
                    }
                }
        }
    }

    /**
     * Run MLKit to identify plant if user does not know the species name
     */
    fun helpIdentifyOnClick(v: View) {
        animate(v) // Animate button color change
        if (!imageTakenBool) { // No plant image to identify
            val errorToast = Toast.makeText(this, R.string.blank_plant_image, Toast.LENGTH_LONG)
            errorToast.setGravity(Gravity.CENTER, 0, 0)
            errorToast.show()
        } else {
            // Rotate bitmap because thumbnails need to rotated 90 degrees
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedImage = Bitmap.createBitmap(imageTaken, 0, 0,
                imageTaken.width, imageTaken.height, matrix, true)
            // Prepare input image as FirebaseVisionImage
            val image = FirebaseVisionImage.fromBitmap(rotatedImage)
            // Configure and run the image labeler (on device)
            val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                 .setConfidenceThreshold(0.80f) // Minimum confidence
                 .build()
            val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)

            labeler.processImage(image)
                .addOnSuccessListener { labels ->
                    if (labels.isEmpty()) { // MLKit ran but no labels
                        showMLKitError()
                    } else {
                        // Task completed successfully, get the potential labels
                        val labelStringList = ArrayList<String>()
                        for (label in labels) {
                            labelStringList.add(label.text)
                        }
                        val labelString = labelStringList.joinToString(", ")

                        // Alert user of potential labels with an AlertDialog
                        val builder: AlertDialog.Builder? = this.let {
                            AlertDialog.Builder(it)
                        }
                        // Display the AlertDialog with the potential labels
                        builder?.setMessage(getString(R.string.mlkit_results, labelString))
                        val dialog: AlertDialog? = builder?.create()
                        dialog?.show()

                        // Center text
                        val messageView: TextView? = dialog?.findViewById(android.R.id.message)
                        messageView?.gravity = Gravity.CENTER
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    showMLKitError()
                }
        }
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

    private fun showMLKitError() {
        val errorToast = Toast.makeText(this, R.string.mlkit_failed, Toast.LENGTH_LONG)
        errorToast.setGravity(Gravity.CENTER, 0, 0)
        errorToast.show()
    }

    companion object {
        val TAG = "ReportPlantActivity"
        val MY_PERMISSIONS_REQUEST = 1
        val REQUEST_IMAGE_CAPTURE = 1
        const val LIGHT_ORANGE_COLOR = "#FCB97D"
        const val LATITUDE_KEY = "LATITUDE_KEY"
        const val LONGITUDE_KEY = "LONGITUDE_KEY"
        const val PLANT_NAME_KEY = "PLANT_NAME_KEY"
        const val PLANT_DESC_KEY = "PLANT_DESC_KEY"
        const val IMAGE_KEY = "IMAGE_KEY"

        fun animate(v: View?) {
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

        fun setStrokes(button: Button?, colorString : String) {
            val drawable = button?.background as GradientDrawable
            drawable.setStroke(16, Color.parseColor(colorString))
        }
    }

}
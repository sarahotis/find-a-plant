package com.example.findaplant

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.google.firebase.database.*
import kotlin.collections.ArrayList
import android.location.Geocoder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_maps.*


class SearchActivity : AppCompatActivity() {
    //search will tap into the Firebase
    internal var searchText: EditText? = null
    private lateinit var databasePlants: DatabaseReference
    private lateinit var databasePlantsByUser: DatabaseReference
    private lateinit var searchPlantButton: Button
    private lateinit var backToMainButton: Button
    private lateinit var findLocations: Button
    private lateinit var findPlantsByLocation: Button
    private lateinit var plantToFind: String
    private lateinit var locationText: EditText
    private var found: Int = 0
    private lateinit var geoCode : Geocoder
    private var wasTouched : Boolean = false
    private var latitudeFromGeocoder: Double? = null
    private var longitudeFromGeocoder: Double? = null
    private var toolbar: Toolbar? = null
    private var drawable: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_plant_layout)

        //toolbar for account icon
        toolbar = findViewById<Toolbar>(R.id.my_toolbar)
        drawable = ContextCompat.getDrawable(this, R.drawable.leaf_icon)
        toolbar?.setNavigationIcon(drawable)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);

        toolbar?.setNavigationOnClickListener {
            startActivity(Intent(this@SearchActivity, MainActivity::class.java))
        }

        initializeViews()
        Log.i("SearchActivity", "OnCreate launched")

        //get instance of database
        val database = FirebaseDatabase.getInstance()
        databasePlantsByUser = database.getReference("User Plants")
        databasePlants = database.getReference("Plants Added To FB")

        //Go back to main button is clicked
        backToMainButton.setOnClickListener {
            val mainActivityIntent = Intent(this@SearchActivity, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }

        //Find locations of address input
        findLocations.setOnClickListener {
            var locationText = locationText.text.toString()
            if(locationText.isEmpty()){
                //If location text is empty make Toast
                Toast.makeText(applicationContext, "Please provide a location. ", Toast.LENGTH_SHORT).show()
            }else{
                createAddressList(locationText)
            }
        }


        //Set on click listener for Search Button
        searchPlantButton.setOnClickListener {
            ReportPlantActivity.animate(it)
            var plantToSearch = searchText!!.text.toString()
            plantToFind = plantToSearch

            //Check if plant is null or empty
            if(plantToSearch == null || plantToSearch == ""){
                val pleaseEnterPlantText = Toast.makeText(applicationContext, "Please provide a plant.", Toast.LENGTH_SHORT)
                pleaseEnterPlantText.show()
            }else{
                //call searchPlant method
                searchPlantInputs()
                searchUserInputs()
            }
        }

        //Find Plants By Location button is clicked
        //If latitude and longitude are not null then go to Map Activity
        //Else make a toast
        findPlantsByLocation.setOnClickListener {
            if(latitudeFromGeocoder != null && longitudeFromGeocoder != null){
                val mapIntent = Intent(this, MapsActivity::class.java)
                mapIntent.putExtra(LATITUDE_FROM_GEOCODER, latitudeFromGeocoder!!)
                mapIntent.putExtra(LONGITUDE_FROM_GEOCODER, longitudeFromGeocoder!!)
                mapIntent.putExtra(SEARCH_FOR_PLANTS_BY_LOCATION, true)
                startActivity(mapIntent)
            }else{
                Toast.makeText(applicationContext, "Please provide a location. ", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /***This method occurs when the Get Location button is pressed. It
     * finds addresses relating to the location text and creates a spinner
     * of possible matched address***/
    private fun createAddressList(locationText : String){
        geoCode = Geocoder(this)

        var listAddress = geoCode.getFromLocationName(locationText, 10)
        //Create adapter
        if(listAddress != null){
            // Get a reference to the Spinner
            val spinner = findViewById<Spinner>(R.id.address_spinner)

            var addressList = ArrayList<String>()
            addressList.add("No address")
            for(currLoc in listAddress){
                val address = currLoc.getAddressLine(0)
                addressList.add(address)
            }

            // Create an Adapter that holds a list of addresses
            val adapter = ArrayAdapter(
                this, R.layout.list_layout_test,addressList)

            //set Spinner to adapter
            spinner.adapter = adapter

            spinner.setOnTouchListener { v: View, _ ->
                wasTouched = true
                v.performClick()
                false
            }

            // Set an onItemSelectedListener on the spinner
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View,
                    pos: Int, id: Long
                ) {
                    if (wasTouched) {
                        if(pos > 0){
                            Toast.makeText(applicationContext, "Address selected! ", Toast.LENGTH_SHORT)
                                .show()
                            //Sets values
                            latitudeFromGeocoder = listAddress[pos - 1]!!.latitude
                            longitudeFromGeocoder = listAddress[pos - 1]!!.longitude

                        }else{
                            latitudeFromGeocoder = null
                            longitudeFromGeocoder = null
                            Toast.makeText(applicationContext, "No address selected ", Toast.LENGTH_SHORT)
                                .show()
                        }

                        wasTouched = false
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }else{
            //Address not found. Make toast
            Toast.makeText(applicationContext, "Address not found. ", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /***Search Firebase for plants that Users Inputed if found then start DescriptionActivity.
     * Else make toast that plant isn't found ***/
    private fun searchUserInputs(){
        databasePlantsByUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {

                    val name = postSnapshot.child("common_name").value as? String
                    val description = postSnapshot.child("description").value as? String
                    if (name != null && name.isNotEmpty() && name.toLowerCase().compareTo(plantToFind.toLowerCase()) === 0) {
                        val longitude = postSnapshot.child("longitude").value as? Double
                        val latitude = postSnapshot.child("latitude").value as? Double
                        val imageURL = postSnapshot.child("image").value as? String

                        //Null checks
                        if (longitude != null && latitude != null && imageURL != null && description != null) {
                            found = 1
                            //If match found then call method to start description intent
                            callDescriptionIntent(description, name, latitude, longitude, imageURL)
                        }
                    }

                }
                if (found == 0) {
                    /*** If plant not found show Toast message ***/
                    Toast.makeText(applicationContext, "Plant not found.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })
    }


    /*** Search plants that are from iNaturalist data ***/
    private fun searchPlantInputs(){
        Log.i("Search Activity", "Entered searchPlant function")
        databasePlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children){
                    val name = postSnapshot.child("common_name").value as? String
                    val description = postSnapshot.child("description").value as? String
                    if(name != null && name.isNotEmpty() && name.toLowerCase().compareTo(plantToFind.toLowerCase()) === 0){
                        val longitude = postSnapshot.child("longitude").value as? Double
                        val latitude = postSnapshot.child("latitude").value as? Double
                        val imageURL = postSnapshot.child("image_url").value as? String

                        //Null checks
                        if(longitude != null && latitude != null && imageURL != null && description != null){
                            //If match found then call method to start description intent
                            found = 1
                            callDescriptionIntent(description, name, latitude, longitude, imageURL)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })
    }

    /***Method called to start description intent with given parameters***/
    private fun callDescriptionIntent(description : String, name : String, latitude : Double, longitude : Double,
                                      imageURL : String){

        val descriptionActivityIntent = Intent(this@SearchActivity, DescriptionActivity::class.java)
        //Placing plant name and description into intent
        descriptionActivityIntent.putExtra(TITLE_KEY, name)
        descriptionActivityIntent.putExtra(DESCRIPTION_KEY, description)
        descriptionActivityIntent.putExtra(LATITUDE, latitude)
        descriptionActivityIntent.putExtra(LONGITUDE, longitude)
        descriptionActivityIntent.putExtra(IMAGE_KEY, imageURL)
        startActivity(descriptionActivityIntent)

    }

    /*** Initialize views of layout ***/
    private fun initializeViews() {
        searchText = findViewById(R.id.plant_search_text)
        searchPlantButton = findViewById(R.id.plant_search_button)
        backToMainButton = findViewById(R.id.backToMainButton)
        locationText = findViewById(R.id.location_search_text)
        findLocations = findViewById(R.id.find_locations)
        findPlantsByLocation = findViewById(R.id.search_by_location)
        ReportPlantActivity.setStrokes(searchPlantButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.example.findaplant.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_account -> {
            startActivity(Intent(this@SearchActivity, ProfileActivity::class.java))
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    companion object{
        const val TAG = "Search Activity"
        const val TITLE_KEY = "TITLE_KEY_FROM_SEARCH"
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY_FROM_SEARCH"
        const val LATITUDE = "LATITUDE_FROM_SEARCH"
        const val LONGITUDE = "LONGITUDE_FROM_SEARCH"
        const val IMAGE_KEY = "IMAGE_KEY_FROM_SEARCH"
        const val LATITUDE_FROM_GEOCODER = "LATITUDE_FROM_SEARCH_FROM_GEOCODER"
        const val LONGITUDE_FROM_GEOCODER = "LONGITUDE_FROM_SEARCH_FROM_GEOCODER"
        const val SEARCH_FOR_PLANTS_BY_LOCATION = "SEARCH_FOR_PLANTS_BY_LOCATION"
    }
}
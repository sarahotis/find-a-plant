package com.example.findaplant

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.google.firebase.database.*
import kotlin.collections.ArrayList
import android.location.Geocoder
import android.view.View
import android.widget.*


class SearchActivity : AppCompatActivity() {
    //search will tap into the Firebase
    internal var searchText: EditText? = null
    private lateinit var databasePlants: DatabaseReference
    private lateinit var databasePlantsByUser: DatabaseReference
    private lateinit var searchPlantButton: Button
    private lateinit var backToMainButton: Button
    private lateinit var searchLocationButton: Button
    private lateinit var plantToFind: String
    private lateinit var locationText: EditText
    private var found: Int = 0
    private lateinit var geoCode : Geocoder
    private var wasTouched : Boolean = false
    private var latitudeFromGeocoder: Double? = null
    private var longitudeFromGeocoder: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_plant_layout)
        initializeViews()
        Log.i("SearchActivity", "OnCreate launched")

        //get instance of database
        val database = FirebaseDatabase.getInstance()
        databasePlantsByUser = database.getReference("User Plants")
        databasePlants = database.getReference("Plants Added To FB")
        Log.i("Firebase", "Database Plants Added to FB referenced")




        //Go back to main button is clicked
        backToMainButton.setOnClickListener {
            val mainActivityIntent = Intent(this@SearchActivity, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }

        //TODO: Show a list of addresses and ask user to pick
        //Create an adapter containing a list of addresses
        searchLocationButton.setOnClickListener {
            var locationText = locationText.text.toString()
            Log.i(TAG, "Location is " + locationText)
            if(locationText.isEmpty()){
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
            Log.i("SearchActivity", "Plant search " + plantToSearch)

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
            Log.i(TAG, "Array size is " + listAddress.size)
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
                Log.i(TAG, "Touched is true")
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
                            Log.i(TAG, "Address is " + parent.getItemAtPosition(pos).toString())
                            Toast.makeText(applicationContext, "Address selected! ", Toast.LENGTH_SHORT)
                                .show()
                            Log.i(TAG, "pos is " + pos)
                            latitudeFromGeocoder = listAddress[pos - 1]!!.latitude
                            longitudeFromGeocoder = listAddress[pos - 1]!!.longitude

                            Log.i(TAG, "Address is from list address " + listAddress[pos - 1].getAddressLine(0))
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

    //Search firebase for plant if found then start DescriptionActivity. Else make toast that plant
    //isn't found
    private fun searchUserInputs(){
        Log.i(TAG, "search user inputs")
        databasePlantsByUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i(TAG, "Searching through user inputs")
                for (postSnapshot in dataSnapshot.children) {

                    val name = postSnapshot.child("common_name").value as? String
                    Log.i(TAG, "name is " + name)
                    val description = postSnapshot.child("description").value as? String
                    if (name != null && name.isNotEmpty() && name.compareTo(plantToFind) === 0) {
                        val longitude = postSnapshot.child("longitude").value as? Double
                        val latitude = postSnapshot.child("latitude").value as? Double
                        val imageURL = postSnapshot.child("image").value as? String

                        //Null checks
                        if (longitude != null && latitude != null && imageURL != null && description != null) {
                            Log.i("Search Activity", "We have a match! From User Input")
                            found = 1
                            //If match found then call method to start description intent
                            callDescriptionIntent(description, name, latitude, longitude, imageURL)
                        }
                        val name = postSnapshot.child("common_name").value as String
                        val description = postSnapshot.child("description").value as String
                        if (name.compareTo(plantToFind) == 0) {
                            val longitude = postSnapshot.child("longitude").value as Double
                            val latitude = postSnapshot.child("latitude").value as Double
                            val imageURL = postSnapshot.child("image").value as String
                            found = 1
                            Log.i("Search Activity", "We have a match! From User Input")
                            //If match found then call method to start description intent
                            callDescriptionIntent(description, name, latitude, longitude, imageURL)
                        }
                    }

                    if (found == 0) {
                        /*** If plant not found show Toast message ***/
                        Log.i("Search Activity", "End of firebase loop")
                        Toast.makeText(applicationContext, "Plant not found.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Report Firebase", "Failed to read data", error.toException())
            }
        })
    }


    //TODO: Have search look at user inputs and iNaturalist data
    private fun searchPlantInputs(){
        Log.i("Search Activity", "Entered searchPlant function")
        databasePlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children){
                    val name = postSnapshot.child("common_name").value as? String
                    val description = postSnapshot.child("description").value as? String
                    if(name != null && name.isNotEmpty() && name.compareTo(plantToFind) === 0){
                        val longitude = postSnapshot.child("longitude").value as? Double
                        val latitude = postSnapshot.child("latitude").value as? Double
                        val imageURL = postSnapshot.child("image_url").value as? String

                        //Null checks
                        if(longitude != null && latitude != null && imageURL != null && description != null){
                            //If match found then call method to start description intent
                            Log.i("Search Activity", "We have a match! From iNaturalist" )
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

    //Method called to start description intent with given parameters
    private fun callDescriptionIntent(description : String, name : String, latitude : Double, longitude : Double,
                                      imageURL : String){

        val descriptionActivityIntent = Intent(this@SearchActivity, DescriptionActivity::class.java)
        //Placing plant name and description into intent
        descriptionActivityIntent.putExtra(TITLE_KEY, name)
        descriptionActivityIntent.putExtra(DESCRIPTION_KEY, description)
        descriptionActivityIntent.putExtra(LATITUDE, latitude)
        descriptionActivityIntent.putExtra(LONGITUDE, longitude)
        descriptionActivityIntent.putExtra(IMAGE_KEY, imageURL)
        Log.i(TAG, "Lat " + latitudeFromGeocoder + "Long " + longitudeFromGeocoder)
        descriptionActivityIntent.putExtra(LATITUDE_FROM_GEOCODER, latitudeFromGeocoder)
        descriptionActivityIntent.putExtra(LONGITUDE_FROM_GEOCODER, longitudeFromGeocoder)
        startActivity(descriptionActivityIntent)

    }

    private fun initializeViews() {
        searchText = findViewById(R.id.plant_search_text)
        searchPlantButton = findViewById(R.id.plant_search_button)
        backToMainButton = findViewById(R.id.backToMainButton)
        locationText = findViewById(R.id.location_search_text)
        searchLocationButton = findViewById(R.id.location_search_button)
        ReportPlantActivity.setStrokes(searchPlantButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
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
    }
}
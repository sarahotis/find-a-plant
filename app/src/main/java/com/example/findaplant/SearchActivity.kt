package com.example.findaplant

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


class SearchActivity : AppCompatActivity() {
    //search will tap into the Firebase
    internal var searchText: EditText? = null
    private lateinit var databasePlants: DatabaseReference
    private lateinit var databasePlantsByUser: DatabaseReference
    private lateinit var searchPlantButton: Button
    private lateinit var backToMainButton: Button
    private lateinit var plantToFind: String
    private var found: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_plant_layout)
        initializeViews()
        Log.i("SearchActivity", "OnCreate launched")

        //TODO: Account for upper/lower case letters. white spaces


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

        //Set on click listener for Search Button
        searchPlantButton.setOnClickListener {
            ReportPlantActivity.animate(it)
            var plantToSearch = searchText!!.text.toString()
            plantToFind = plantToSearch
            Log.i("SearchActivity", "Plant search " + plantToSearch)

            if(plantToSearch == ""){
                val pleaseEnterPlantText = Toast.makeText(applicationContext, "Please provide a plant.", Toast.LENGTH_SHORT)
                pleaseEnterPlantText.show()
            }else{
                //call searchPlant method
                searchPlantInputs()
                searchUserInputs()
            }
        }

    }

    //Search firebase for plant if found then start DescriptionActivity. Else make toast that plant
    //isn't found
    private fun searchUserInputs(){
        Log.i(TAG, "search user inputs")
        databasePlantsByUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i(TAG, "Searching through user inputs")
                for(postSnapshot in dataSnapshot.children){
                    val name = postSnapshot.child("common_name").value as String
                    val description = postSnapshot.child("description").value as String
                    if(name.compareTo(plantToFind) === 0){
                        val longitude = postSnapshot.child("longitude").value as Double
                        val latitude = postSnapshot.child("latitude").value as Double
                        val imageURL = postSnapshot.child("image").value as String
                        found = 1
                        Log.i("Search Activity", "We have a match! From User Input" )
                        //If match found then call method to start description intent
                        callDescriptionIntent(description, name, latitude, longitude, imageURL)
                    }
                }

                if(found == 0){
                    /*** If plant not found show Toast message ***/
                    Log.i("Search Activity", "End of firebase loop")
                    Toast.makeText(applicationContext, "Plant not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    //TODO: Have search look at user inputs and iNaturalist data
    private fun searchPlantInputs(){
        Log.i("Search Activity", "Entered searchPlant function")
        databasePlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children){
                    val name = postSnapshot.child("common_name").value as String
                    val description = postSnapshot.child("description").value as String
                    if(name.compareTo(plantToFind) === 0){
                        val longitude = postSnapshot.child("longitude").value as Double
                        val latitude = postSnapshot.child("latitude").value as Double
                        val imageURL = postSnapshot.child("image_url").value as String
                        found = 1
                        Log.i("Search Activity", "We have a match! From iNaturalist" )
                        //If match found then call method to start description intent
                        callDescriptionIntent(description, name, latitude, longitude, imageURL)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        startActivity(descriptionActivityIntent)

    }

    private fun initializeViews() {
        searchText = findViewById(R.id.plant_search_text)
        searchPlantButton = findViewById(R.id.plant_search_button)
        backToMainButton = findViewById(R.id.backToMainButton)
        ReportPlantActivity.setStrokes(searchPlantButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
    }

    companion object{
        const val TAG = "Search Activity"
        const val TITLE_KEY = "TITLE_KEY_FROM_SEARCH"
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY_FROM_SEARCH"
        const val LATITUDE = "LATITUDE_FROM_SEARCH"
        const val LONGITUDE = "LONGITUDE_FROM_SEARCH"
        const val IMAGE_KEY = "IMAGE_KEY_FROM_SEARCH"
    }
}
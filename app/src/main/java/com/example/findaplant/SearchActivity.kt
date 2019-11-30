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
    private lateinit var searchPlantButton: Button
    private lateinit var plantToFind: String
    private var found: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_plant_layout)
        initializeViews()
        Log.i("SearchActivity", "OnCreate launched")


        //get instance of database
        val database = FirebaseDatabase.getInstance()
        databasePlants = database.getReference("Plants Added To FB")
        Log.i("Firebase", "Database Plants Added to FB referenced")

        //get search text



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
                searchPlant()
            }
        }


//        //Making a plant object to add to Firebase
//        var plant = Plant("Daisy", "White petals, yellow inside", "Ngan")
//        databasePlants.setValue(plant)

    }

    //Search firebase for plant if found then start DescriptionActivity. Else make toast that plant
    //isn't found

    //TODO: Have search look at user inputs and iNaturalist data
    private fun searchPlant(){
        Log.i("Search Activity", "Entered searchPlant function")
        databasePlants.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(postSnapshot in dataSnapshot.children){
                    Log.i("Search Activity", "Through loop")
                    val name = postSnapshot.child("common_name").value as String
                    val description = postSnapshot.child("description").value as String
                    if(name.compareTo(plantToFind) === 0){
                        found = 1
                        Log.i("Search Activity", "We have a match!" )
                        //If match found then move to description activity
                        val descriptionActivityIntent = Intent(this@SearchActivity, DescriptionActivity::class.java)
                        //Placing plant name and description into intent
                        descriptionActivityIntent.putExtra(TITLE_KEY, description)
                        descriptionActivityIntent.putExtra(DESCRIPTION_KEY, name)
                        startActivity((descriptionActivityIntent))

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

    private fun initializeViews() {
        searchText = findViewById(R.id.plant_search_text)
        searchPlantButton = findViewById(R.id.plant_search_button)
        ReportPlantActivity.setStrokes(searchPlantButton, ReportPlantActivity.LIGHT_ORANGE_COLOR)
    }

    companion object{
        const val TITLE_KEY = "TITLE_KEY"
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY"
    }
}
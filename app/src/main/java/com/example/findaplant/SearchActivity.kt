package com.example.findaplant

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_plant_layout)
        initializeViews()
        Log.i("SearchActivity", "OnCreate launched")

        //get instance of database
        val database = FirebaseDatabase.getInstance()
        databasePlants = database.getReference("Plants Added To FB")
        Log.i("Firebase", "Database plants referenced")

        //get search text



        //Set on click listener for Search Button
        searchPlantButton.setOnClickListener {
            var plantToSearch = searchText!!.text.toString()
            Log.i("SearchActivity", "Plant search " + plantToSearch)

            if(plantToSearch == ""){
                val pleaseEnterPlantText = Toast.makeText(applicationContext, "Please provide a plant.", Toast.LENGTH_SHORT)
                pleaseEnterPlantText.show()
            }else{
                //call searchPlant method
                searchPlant(plantToSearch)
            }
        }


//        //Making a plant object to add to Firebase
//        var plant = Plant("Daisy", "White petals, yellow inside", "Ngan")
//        databasePlants.setValue(plant)

    }

    //Search firebase for plant if found then start DescriptionActivity. Else make toast that plant
    //isn't found
    private fun searchPlant(plantText : String){
        //Making a plant object to add to Firebase
        var plant = Plant("Cosmos", "Purple petals, yellow inside", "Ngan")
        databasePlants.setValue(plant)
        Log.i("Firebase", "onStart started in Search Activity")
        databasePlants.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
               for(postSnapshot in dataSnapshot.children){
                   var plant = postSnapshot.getValue(String::class.java)
                   Log.i("SearchActivity", "Plant name is " + plant)
               }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read data", error.toException())
            }
        })
        //search Firebase

    }

    private fun initializeViews() {
        searchText = findViewById(R.id.plant_search_text)
        searchPlantButton = findViewById(R.id.plant_search_button)
    }
}
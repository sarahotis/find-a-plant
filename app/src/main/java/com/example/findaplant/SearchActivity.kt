package com.example.findaplant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import com.google.firebase.database.*


class SearchActivity : AppCompatActivity() {
    //search will tap into the Firebase
    internal var searchText: EditText? = null
    private lateinit var databasePlants: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_plant_layout)
        initializeViews()
        Log.i("SearchActivity", "OnCreate launched")

        //get instance of database
        val database = FirebaseDatabase.getInstance()
        databasePlants = database.getReference("Plants Added To FB")
        Log.i("Firebase", "Database plants referenced")

        //Making a plant object to add to Firebase
        var plant = Plant("Daisy", "White petals, yellow inside", "Ngan")
        databasePlants.setValue(plant)



    }

    override fun onStart() {
        super.onStart()
        Log.i("Firebase", "onStart started in Search Activity")
        databasePlants.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Plant::class.java)
                Log.i("Firebase", "Name is ${value!!.plantName}")
                Log.i("Firebase", "Name is ${value!!.plantDescription}")
                Log.i("Firebase", "Name is ${value!!.user}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read data", error.toException())
            }
        })

    }

    private fun initializeViews() {
        searchText = findViewById(R.id.plant_search_text)
    }
}
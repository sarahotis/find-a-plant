package com.example.findaplant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log


class DescriptionActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.description_of_plant_layout)
        Log.i("Description", "Class Started");
    }

}
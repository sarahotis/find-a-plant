package com.example.findaplant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.EditText


class SearchActivity : AppCompatActivity() {
    //search will tap into the Firebase
    internal var searchText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_plant_layout)
        initializeViews()

    }

    private fun initializeViews() {
        searchText = findViewById(R.id.plant_search_text)
    }
}
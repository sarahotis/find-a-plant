package com.example.findaplant

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.EventLogTags
import android.widget.Button
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import android.view.MenuItem
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.widget.Toolbar


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    internal var reportBtn: Button? = null
    internal var searchBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);

        initializeViews()

        reportBtn!!.setOnClickListener {
            ReportPlantActivity.animate(it)
            if (notLoggedIn()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                Toast.makeText(this, "Log in to add to our growing database",
                    Toast.LENGTH_LONG).show();
            } else {
                startActivity(Intent(this@MainActivity, ReportPlantActivity::class.java))
            }
        }


        searchBtn!!.setOnClickListener {
            ReportPlantActivity.animate(it)
            startActivity(Intent(this@MainActivity, SearchActivity::class.java))
        }

    }

    private fun initializeViews() {
        reportBtn = findViewById(R.id.report)
        ReportPlantActivity.setStrokes(reportBtn, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        searchBtn = findViewById(R.id.centered_search)
        ReportPlantActivity.setStrokes(searchBtn, ReportPlantActivity.LIGHT_ORANGE_COLOR)
        mAuth = FirebaseAuth.getInstance()
    }

    private fun notLoggedIn(): Boolean {
        //check if current user is null
        if(mAuth!!.currentUser == null){
            return true
        }

        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(com.example.findaplant.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_account -> {
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    companion object{
        const val TAG = "Main Activity"
    }
}

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


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    internal var reportBtn: Button? = null
//    internal var descriptionBtn: Button? = null
    internal var searchBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        Log.i(TAG, "In Main Activity")
        //TODO: Have user enter a special userid
        var user = mAuth!!.currentUser
        if(user != null){
            Log.i(TAG, "Current user is " + user.email)
        }


        reportBtn!!.setOnClickListener {
            ReportPlantActivity.animate(it)
            if (notLoggedIn()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, ReportPlantActivity::class.java))
            }
        }




        searchBtn!!.setOnClickListener {
            Log.i("Search", "Search Activity Started")
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
        Log.i(TAG, "Current user is " + mAuth!!.currentUser)
        if(mAuth!!.currentUser == null){
            return true
        }

        return false
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

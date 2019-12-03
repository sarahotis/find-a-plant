package com.example.findaplant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var userInfo : TextView? = null
    private var loggedIn : TextView? = null
    private var loggedOut : TextView? = null
    private var accountCreated : TextView? = null
    private var logOut: Button? = null
    private var logIn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        userInfo = findViewById(R.id.user)
        loggedIn = findViewById(R.id.LoggedIn)
        loggedOut = findViewById(R.id.LoggedOut)
        accountCreated = findViewById(R.id.AccountCreated)
        logOut = findViewById(R.id.LogOut)
        logIn = findViewById(R.id.LogIn)

        val user = mAuth!!.currentUser

        //User is logged in or out - load the appropriate widgets
        if (notLoggedIn()) {
            loggedOut?.visibility = TextView.VISIBLE
            logIn?.visibility = Button.VISIBLE
        } else {
            userInfo?.visibility = TextView.VISIBLE
            userInfo?.text = user?.email?.split('@')?.get(0) //first part of email address
            loggedIn?.visibility = TextView.VISIBLE
            accountCreated?.visibility = TextView.VISIBLE
            accountCreated?.text = "Member since " + registrationDate()
            logOut?.visibility = Button.VISIBLE
        }
    }

    private fun notLoggedIn(): Boolean {
        //check if current user is null
        if(mAuth!!.currentUser == null){
            return true
        }

        return false
    }

    private fun registrationDate(): String {

        return "November 3" //TODO: implement firebase registration stats
    }
}


package com.example.findaplant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.sql.Timestamp
import java.sql.Date

class ProfileActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var userInfo : TextView? = null
    private var loggedIn : TextView? = null
    private var loggedOut : TextView? = null
    private var accountCreated : TextView? = null
    private var logOutButton: Button? = null
    private var logInButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)
        mAuth = FirebaseAuth.getInstance()

        userInfo = findViewById(R.id.user)
        loggedIn = findViewById(R.id.LoggedIn)
        loggedOut = findViewById(R.id.LoggedOut)
        accountCreated = findViewById(R.id.AccountCreated)
        logOutButton = findViewById(R.id.LogOut)
        logInButton = findViewById(R.id.LogIn)

        val user = mAuth!!.currentUser

        //User is logged in or out - load the appropriate widgets
        if (notLoggedIn()) {
            loggedOut?.visibility = TextView.VISIBLE
            logInButton?.visibility = Button.VISIBLE
            logInButton?.setOnClickListener {
                logIn()
            }
        } else {
            userInfo?.visibility = TextView.VISIBLE
            userInfo?.text = user?.email?.split('@')?.get(0) //first part of email address
            loggedIn?.visibility = TextView.VISIBLE
            logOutButton?.setOnClickListener {
                logOut()
            }
            accountCreated?.visibility = TextView.VISIBLE
            accountCreated?.text = "Member since " + registrationDate()
            logOutButton?.visibility = Button.VISIBLE
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
        val creation : Long = mAuth!!.currentUser?.metadata?.creationTimestamp?: 0L
        val stamp = Timestamp(creation)
        val date = Date(stamp.getTime())
        return date.toString()
    }

    private fun logIn() {
        startActivity(Intent( this@ProfileActivity, LoginActivity::class.java))
    }

    private fun logOut() {
        mAuth!!.signOut()
        //TODO: add oncomplete listener
        Toast.makeText(this, "Logged out!",
            Toast.LENGTH_LONG).show();
        startActivity(Intent( this@ProfileActivity, MainActivity::class.java))
    }
}


package com.udacity.project4.authentication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.udacity.project4.R

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Authen", "Authencreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
    }
}
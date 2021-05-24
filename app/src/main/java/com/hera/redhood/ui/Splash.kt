package com.hera.redhood.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hera.redhood.R
import com.hera.redhood.ui.authentication.Authentication
import com.hera.redhood.ui.base.Base

/**
 * Splash Activity.
 * Starts when user launches the app.
 */
class Splash : AppCompatActivity() {
    // Firebase auth.
    private lateinit var auth: FirebaseAuth

    /**
     * On Create.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Setting auth.
        auth = Firebase.auth
    }

    /**
     * On Resume.
     */
    override fun onResume() {
        super.onResume()
        // useless stuff.
        Handler().postDelayed({
            startApp()
            finish()
        }, 2000)
    }

    /**
     * Start App.
     */
    private fun startApp() {
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            startActivity(Intent(this, Base::class.java))
        } else {
            startActivity(Intent(this, Authentication::class.java))
        }
    }
}
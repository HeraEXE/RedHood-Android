package com.hera.redhood.ui.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hera.redhood.R
import com.hera.redhood.data.FirebaseKey.HOOD_KEY
import com.hera.redhood.data.FirebaseKey.USER_KEY

/**
 * Authentication Activity.
 * Handles following fragments:
 * LogIn, Registration, PasswordReset, EmailVerification.
 */
class Authentication : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    lateinit var dbUserRef: DatabaseReference
    /**
     * On Create.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        // Setting auth.
        auth = Firebase.auth

        // Setting database and references.
        database = Firebase.database
        dbUserRef = database.getReference(USER_KEY)

        // Setting action bar.
        val actionBar: Toolbar = findViewById(R.id.auth_action_bar)
        setSupportActionBar(actionBar)
    }
}
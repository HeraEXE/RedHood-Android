package com.hera.redhood.ui.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hera.redhood.R
import com.hera.redhood.data.FirebaseKey.USER_KEY
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.ActivityBaseBinding
import com.hera.redhood.ui.authentication.Authentication

/**
 * Base Activity.
 * Drawer Fragments - EditProfile, SavedHoodsFragment, CreateHoodsFragment.
 * BottomNav Fragments - HoodsFragments, SubscriptionsHoodsFragment, UserHoodsFragment.
 */
class Base : AppCompatActivity() {
    // firebase auth.
    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    // firebase database.
    lateinit var database: FirebaseDatabase
    lateinit var dbUserRef: DatabaseReference
    lateinit var currentUser: User
    // binding.
    private lateinit var binding: ActivityBaseBinding

    /**
     * On Create.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting auth and user.
        auth = Firebase.auth
        user = auth.currentUser!!

        // Setting database and dbUserRef.
        database = Firebase.database
        dbUserRef = database.getReference(USER_KEY)

        // Setting binding.
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting action bar.
        val actionBar = binding.baseMain.baseToolbar
        setSupportActionBar(actionBar)

        // Setting bottom nav.
        val baseFragment: FragmentContainerView = findViewById(R.id.base_fragment)
        binding.baseMain.baseBottomNav.setupWithNavController(baseFragment.findNavController())

        // Setting drawer.
        binding.baseNavView.setupWithNavController(baseFragment.findNavController())
        // drawer header.
        setDrawerHeader()
        // on logout button click.
        onLogoutButtonClick()
    }

    /**
     * Set Drawer Header.
     */
    fun setDrawerHeader() {
        dbUserRef.get()
                .addOnSuccessListener {
                    currentUser = it.child(user.uid).getValue(User::class.java)!!
                    val header = binding.baseNavView.getHeaderView(HEADER_ID)
                    val username: TextView = header.findViewById(R.id.base_drawer_username_tv)
                    val userEmail: TextView = header.findViewById(R.id.base_drawer_email_tv)
                    val userProfileImg: ImageView = header.findViewById(R.id.base_drawer_profile_img)
                    username.text = currentUser.username
                    userEmail.text = currentUser.email
                    Glide.with(binding.root)
                            .load(currentUser.profileImgUrl)
                            .error(R.drawable.redhood_logo)
                            .into(userProfileImg)
                }
    }

    /**
     * On Logout Button Click.
     */
    private fun onLogoutButtonClick() {
        binding.baseNavView.menu.getItem(LOGOUT_ID).setOnMenuItemClickListener {
            AlertDialog.Builder(this)
                    .setMessage(R.string.logout_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.logout_yes) { dialog, which ->
                        dialog.dismiss()
                        auth.signOut()
                        startActivity(Intent(this, Authentication::class.java))
                        finish()
                    }
                    .setNegativeButton(R.string.logout_no) { dialog, which ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            true
        }
    }

    companion object {
        const val HEADER_ID = 0
        const val LOGOUT_ID = 3
    }
}
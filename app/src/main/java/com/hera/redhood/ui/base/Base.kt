package com.hera.redhood.ui.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.Navigation
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
import com.hera.redhood.data.FirebaseKey.HOOD_KEY
import com.hera.redhood.data.FirebaseKey.USER_KEY
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.ActivityBaseBinding
import com.hera.redhood.ui.authentication.Authentication
import com.hera.redhood.utils.BundleKeys

/**
 * Base Activity.
 * Drawer Fragments - EditProfile, SavedHoodsFragment, CreateHoodsFragment.
 * BottomNav Fragments - HoodsFragments, SubscriptionsHoodsFragment, UserHoodsFragment.
 */
class Base : AppCompatActivity() {
    // firebase auth.
    lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    // firebase database.
    private lateinit var database: FirebaseDatabase
    lateinit var dbUserRef: DatabaseReference
    lateinit var dbHoodRef: DatabaseReference

    lateinit var user: User
    // binding.
    private lateinit var binding: ActivityBaseBinding

    /**
     * On Create.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting auth and user.
        auth = Firebase.auth
        currentUser = auth.currentUser!!

        // Setting database and (dbUserRef, dbHoodRef).
        database = Firebase.database
        dbUserRef = database.getReference(USER_KEY)
        dbHoodRef = database.getReference(HOOD_KEY)

        // Setting binding.
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setting action bar.
        val actionBar = binding.baseMain.baseToolbar
        setSupportActionBar(actionBar)
        supportActionBar?.title = getText(R.string.hoods_title)

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
                    user = it.child(currentUser.uid).getValue(User::class.java)!!
                    val header = binding.baseNavView.getHeaderView(HEADER_ID)
                    val username: TextView = header.findViewById(R.id.base_drawer_username_tv)
                    val userEmail: TextView = header.findViewById(R.id.base_drawer_email_tv)
                    val userProfileImg: ImageView = header.findViewById(R.id.base_drawer_profile_img)
                    username.text = user.username
                    userEmail.text = user.email
                    Glide.with(binding.root)
                            .load(user.profileImgUrl)
                            .error(R.drawable.redhood_logo)
                            .into(userProfileImg)
                    userProfileImg.setOnClickListener {
                        val bundle = bundleOf(
                            BundleKeys.ID_KEY to user.idKey,
                            BundleKeys.EMAIL to user.email,
                            BundleKeys.USERNAME to user.username,
                            BundleKeys.IMAGE_URL to user.profileImgUrl,
                            BundleKeys.SUBSCRIBERS to user.subscribers,
                            BundleKeys.TOTAL_LIKES to user.totalLikes
                        )
                        val baseFragment: FragmentContainerView = findViewById(R.id.base_fragment)
                        binding.baseDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        binding.baseDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        baseFragment.findNavController().navigate(R.id.action_global_profileSheet, bundle)
                    }
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
        const val LOGOUT_ID = 2
    }
}
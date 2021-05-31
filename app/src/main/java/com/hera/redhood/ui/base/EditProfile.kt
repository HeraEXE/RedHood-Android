package com.hera.redhood.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.hera.redhood.R
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.FragmentEditProfileBinding

/**
 * Edit Profile.
 */
class EditProfile : Fragment() {
    // binding.
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    // firebase auth.
    private lateinit var currentUser: FirebaseUser
    // firebase database.
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var user: User
    //
    private var updatedImgUri: String? = null

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting binding.
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.edit_profile_title)

        // Setting user.
        currentUser = (activity as Base).currentUser

        // Setting dbUserRef and currentUser.
        dbUserRef = (activity as Base).dbUserRef
        user = (activity as Base).user

        // Setting ui with current user data.
        Glide.with(binding.root)
                .load(user.profileImgUrl)
                .into(binding.editProfileImg)
        binding.editProfileUsernameEt.setText(user.username)

        // Setting image profile.
        binding.editProfileImgBtn.setOnClickListener {
            val intent = Intent().also {
                it.type = "image/*"
                it.action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, getText(R.string.chooser_title)), PICK_IMAGE)
        }

        // Setting on apply button click.
        binding.editProfileApplyBtn.setOnClickListener {
            hideKeyboard()
            val idKey = user.idKey
            val email = user.email
            val username = binding.editProfileUsernameEt.text.toString()
            val profileImgUrl = updatedImgUri ?: user.profileImgUrl
            val subscribers = user.subscribers
            val totalLikes = user.totalLikes
            val subscriptions = user.subscriptions
            val likedHoods = user.likedHoods
            if (validateUsername(username)) {
                binding.editProfileApplyBtn.visibility = View.INVISIBLE
                binding.editProfileProgressBar.visibility = View.VISIBLE
                val updatedUser = User(idKey, email, username, profileImgUrl, subscribers, totalLikes, subscriptions, likedHoods)
                confirmUserEdition(updatedUser)
            }
        }

        return binding.root
    }

    /**
     * On Destroy View.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * On Activity Result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                updatedImgUri = data.data.toString()
                Glide.with(binding.root)
                        .load(data.data)
                        .error(R.drawable.redhood_logo)
                        .into(binding.editProfileImg)
            }
        }
    }

    /**
     * Validate Username.
     */
    private fun validateUsername(username: String): Boolean {
        var isValid = true
        when {
            username.isEmpty() -> {
                isValid = false
                binding.editProfileUsernameEt.error = getText(R.string.empty_form)
            }
            else -> {
                binding.editProfileUsernameEt.error = null
            }
        }
        return isValid
    }

    /**
     * Confirm User Edition.
     */
    private fun confirmUserEdition(updatedUser: User) {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.edit_user_message)
                .setCancelable(false)
                .setPositiveButton(R.string.edit_user_yes) { dialog, which ->
                    dialog.dismiss()
                    editUser(updatedUser)
                    Toast.makeText(activity, getText(R.string.edit_user_toast), Toast.LENGTH_SHORT).show()
                    binding.editProfileApplyBtn.visibility = View.VISIBLE
                    binding.editProfileProgressBar.visibility = View.GONE
                }
                .setNegativeButton(R.string.edit_user_no) { dialog, which ->
                    dialog.dismiss()
                    binding.editProfileApplyBtn.visibility = View.VISIBLE
                    binding.editProfileProgressBar.visibility = View.GONE
                }
                .create()
                .show()
    }

    /**
     * Edit User.
     */
    private fun editUser(updatedUser: User) {
        dbUserRef.child(currentUser.uid).setValue(updatedUser)
        (activity as Base).setDrawerHeader()
    }

    /**
     * Hide Keyboard.
     */
    private fun hideKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {
        const val PICK_IMAGE = 69
    }
}
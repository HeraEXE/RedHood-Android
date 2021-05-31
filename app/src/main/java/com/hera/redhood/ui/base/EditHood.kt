package com.hera.redhood.ui.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.hera.redhood.R
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.FragmentEditHoodBinding
import com.hera.redhood.databinding.FragmentHoodBinding
import com.hera.redhood.utils.BundleKeys
import com.hera.redhood.utils.BundleKeys.CONTENT
import com.hera.redhood.utils.BundleKeys.ID_KEY
import com.hera.redhood.utils.BundleKeys.IMAGE_URL
import com.hera.redhood.utils.BundleKeys.LIKES
import com.hera.redhood.utils.BundleKeys.TITLE
import com.hera.redhood.utils.BundleKeys.USERNAME
import com.hera.redhood.utils.BundleKeys.USER_IMAGE_URL
import com.hera.redhood.utils.BundleKeys.USER_KEY


class EditHood : Fragment() {
    // binding.
    private var _binding: FragmentEditHoodBinding? = null
    private val binding get() = _binding!!
    // firebase database.
    private lateinit var dbHoodRef: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference
    // image url.
    private var updatedImageUrl: String? = null
    // currentUser.
    private lateinit var currentUser: FirebaseUser

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting binding.
        _binding = FragmentEditHoodBinding.inflate(inflater, container, false)

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?. title = "ffff"

        // Setting db hood ref.
        dbHoodRef = (activity as Base).dbHoodRef
        dbUserRef = (activity as Base).dbUserRef

        // Setting current user.
        currentUser = (activity as Base).currentUser

        // Retrieving data from bundle.
        binding.editHoodTitleEt.setText(arguments?.getString(TITLE))
        binding.editHoodContentEt.setText(arguments?.getString(CONTENT))
        binding.editHoodAuthorName.text = arguments?.getString(USERNAME)
        binding.editHoodLikesCounter.text = arguments?.getInt(LIKES).toString()
        Glide.with(binding.root)
            .load(arguments?.getString(IMAGE_URL))
            .error(R.drawable.redhood_logo)
            .into(binding.editHoodImage)
        Glide.with(binding.root)
            .load(arguments?.getString(USER_IMAGE_URL))
            .error(R.drawable.redhood_logo)
            .into(binding.editHoodAuthorImg)

        // Setting on image click.
        binding.editHoodImgBtn.setOnClickListener {
            val intent = Intent().also {
                it.type = "image/*"
                it.action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, getText(R.string.chooser_title)),
                CreateHoodsFragment.GET_HOOD_IMG
            )
        }

        // Setting on user icon click listener.
        binding.editHoodAuthorImg.setOnClickListener {
            dbUserRef.get()
                .addOnSuccessListener {
                    val userKey = arguments?.getString(USER_KEY)!!
                    val user = it.child(userKey).getValue(User::class.java)!!
                    val bundle = bundleOf(
                        ID_KEY to user.idKey,
                        BundleKeys.EMAIL to user.email,
                        USERNAME to user.username,
                        IMAGE_URL to user.profileImgUrl,
                        BundleKeys.SUBSCRIBERS to user.subscribers,
                        BundleKeys.TOTAL_LIKES to user.totalLikes
                    )
                    binding.root.findNavController().navigate(R.id.action_global_profileSheet, bundle)
                }
        }

        // Setting on apply button click.
        binding.editHoodApplyBtn.setOnClickListener {
            hideKeyboard()
            val idKey = arguments?.getString(ID_KEY)!!
            val userKey = arguments?.getString(USER_KEY)!!
            val title = binding.editHoodTitleEt.text.toString()
            val content = binding.editHoodContentEt.text.toString()
            val imageUrl = updatedImageUrl ?: arguments?.getString(IMAGE_URL)!!
            val likes = arguments?.getInt(LIKES)!!
            if (validateForm(title, content)) {
                binding.editHoodApplyBtn.visibility = View.INVISIBLE
                binding.editHoodProgressBar.visibility = View.VISIBLE
                hoodEditionConfirmation(idKey, userKey, title, content, imageUrl, likes)
            }
        }

        return binding.root
    }

    /**
     * On Activity Result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CreateHoodsFragment.GET_HOOD_IMG) {
            if (data != null) {
                updatedImageUrl = data.data.toString()
                Glide.with(binding.root)
                    .load(data.data)
                    .error(R.drawable.redhood_logo)
                    .into(binding.editHoodImage)
            }
        }
    }

    /**
     * Hood Creation Confirmation.
     */
    private fun hoodEditionConfirmation(idKey: String, userKey: String, title: String, content: String, imageUrl: String, likes: Int) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.edit_hood_message)
            .setCancelable(false)
            .setPositiveButton(R.string.edit_hood_yes) { dialog, which ->
                dialog.dismiss()
                editHood(idKey, userKey, title, content, imageUrl, likes)
                Toast.makeText(activity, getText(R.string.edit_hood_toast), Toast.LENGTH_SHORT).show()
                binding.editHoodApplyBtn.visibility = View.VISIBLE
                binding.editHoodProgressBar.visibility = View.GONE
            }
            .setNegativeButton(R.string.edit_hood_no) { dialog, which ->
                dialog.dismiss()
                binding.editHoodApplyBtn.visibility = View.VISIBLE
                binding.editHoodProgressBar.visibility = View.GONE
            }
            .create()
            .show()
    }

    /**
     * On Destroy View.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Validate Form.
     */
    private fun validateForm(title: String, content: String): Boolean  {
        var isValid = true

        when {
            title.isEmpty() -> {
                isValid = false
                binding.editHoodTitleEt.error = getText(R.string.empty_form)
            }
            else -> binding.editHoodTitleEt.error = null
        }

        when {
            content.isEmpty() -> {
                isValid = false
                binding.editHoodContentEt.error = getText(R.string.empty_form)
            }
            else -> binding.editHoodContentEt.error = null
        }
        return isValid
    }

    /**
     * Edit Hood.
     */
    private fun editHood(idKey: String, userKey: String, title: String, content: String, imageUrl: String?, likes: Int) {
        val editedHood = Hood(idKey, userKey, title, content, imageUrl, likes)
        dbHoodRef.child(idKey).setValue(editedHood)
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
}
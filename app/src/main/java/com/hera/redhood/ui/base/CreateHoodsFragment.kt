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
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.hera.redhood.R
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.FragmentCreateHoodsBinding
import com.hera.redhood.utils.BundleKeys.CONTENT
import com.hera.redhood.utils.BundleKeys.ID_KEY
import com.hera.redhood.utils.BundleKeys.IMAGE_URL
import com.hera.redhood.utils.BundleKeys.LIKES
import com.hera.redhood.utils.BundleKeys.TITLE
import com.hera.redhood.utils.BundleKeys.USERNAME
import com.hera.redhood.utils.BundleKeys.USER_IMAGE_URL
import com.hera.redhood.utils.BundleKeys.USER_KEY

/**
 * Create Hoods Fragment.
 */
class  CreateHoodsFragment : Fragment() {
    // binding.
    private var _binding: FragmentCreateHoodsBinding? = null
    private val binding get() = _binding!!
    // firebase user.
    private lateinit var currentUser: FirebaseUser
    // firebase database.
    private lateinit var dbHoodRef: DatabaseReference
    // user data.
    private lateinit var user: User
    // image url.
    private var updatedImageUrl: String? = null

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Setting user.
        currentUser = (activity as Base).currentUser

        // Setting references.
        dbHoodRef = (activity as Base).dbHoodRef

        // Setting user data.
        user = (activity as Base).user

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.create_hood_title)

        // Setting binding.
        _binding = FragmentCreateHoodsBinding.inflate(inflater, container, false)

        // Setting hood image.
        binding.createHoodImgBtn.setOnClickListener {
            val intent = Intent().also {
                it.type = "image/*"
                it.action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(Intent.createChooser(intent, getText(R.string.chooser_title)), GET_HOOD_IMG)
        }

        // Setting on create button click.
        binding.createHoodButton.setOnClickListener {
            hideKeyboard()
            val title = binding.createHoodTitleEt.text.toString()
            val content = binding.createHoodContentEt.text.toString()
            val imageUrl = updatedImageUrl ?: DEFAULT_HOOD_IMAGE
            if (validateForm(title, content)) {
                binding.createHoodButton.visibility = View.INVISIBLE
                binding.createHoodProgressBar.visibility = View.VISIBLE
                hoodCreationConfirmation(title, content, imageUrl)
            }
        }


        return binding.root
    }

    /**
     * On Activity Result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GET_HOOD_IMG) {
            if (data != null) {
                updatedImageUrl = data.data.toString()
                Glide.with(binding.root)
                        .load(data.data)
                        .error(R.drawable.redhood_logo)
                        .into(binding.createHoodImg)
            }
        }
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
                binding.createHoodTitleEt.error = getText(R.string.empty_form)
            }
            else -> binding.createHoodTitleEt.error = null
        }

        when {
            content.isEmpty() -> {
                isValid = false
                binding.createHoodContentEt.error = getText(R.string.empty_form)
            }
            else -> binding.createHoodContentEt.error = null
        }
        return isValid
    }


    /**
     * Hood Creation Confirmation.
     */
    private fun hoodCreationConfirmation(title: String, content: String, imageUrl: String) {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.create_hood_message)
                .setCancelable(false)
                .setPositiveButton(R.string.create_hood_yes) { dialog, which ->
                    dialog.dismiss()
                    val idKey = dbHoodRef.push().key!!
                    val userKey = user.idKey!!
                    val bundle = createNewHood(idKey, userKey, title, content, imageUrl)
                    Toast.makeText(activity, getText(R.string.create_hood_toast), Toast.LENGTH_SHORT).show()
                    binding.root.findNavController().navigate(R.id.action_global_editHood, bundle)
                }
                .setNegativeButton(R.string.create_hood_no) { dialog, which ->
                    dialog.dismiss()
                    binding.createHoodButton.visibility = View.VISIBLE
                    binding.createHoodProgressBar.visibility = View.GONE
                }
                .create()
                .show()
    }

    /**
     * Create New Hood.
     */
    private fun createNewHood(idKey: String, userKey: String, title: String, content: String, imageUrl: String): Bundle {
        val hood = Hood(idKey, userKey, title, content, imageUrl, 0)
        dbHoodRef.child(idKey).setValue(hood)

        return bundleOf(
                ID_KEY to idKey,
                USER_KEY to userKey,
                TITLE to title,
                CONTENT to content,
                IMAGE_URL to imageUrl,
                LIKES to 0,
                USERNAME to user.username,
                USER_IMAGE_URL to user.profileImgUrl
        )
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
        const val DEFAULT_HOOD_IMAGE = "https://cdn.discordapp.com/attachments/712342039030923329/845835928433459210/redhood_logo.png"
        const val GET_HOOD_IMG = 228
    }
}
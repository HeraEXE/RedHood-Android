package com.hera.redhood.ui.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.hera.redhood.R
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.FragmentRegistrationBinding
import com.hera.redhood.utils.BundleKeys.START
import kotlin.random.Random

/**
 * Registration Fragment.
 * Part of Authentication Activity.
 */
class Registration : Fragment() {
    // firebase auth.
    private lateinit var auth: FirebaseAuth
    // firebase references.
    private lateinit var dbUserRef: DatabaseReference

    // binding.
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // Setting auth.
        auth = (activity as Authentication).auth

        // Setting references.
        dbUserRef = (activity as Authentication).dbUserRef

        // Setting binding.
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.reg_appbar_title)

        // Setting on submit button click.
        binding.regSubmitButton.setOnClickListener {
            hideKeyboard()
            // get email and password from forms.
            val email = binding.regEmailEt.text.toString()
            val username = binding.regUsernameEt.text.toString()
            val password = binding.regPasswordEt.text.toString()
            val confirmPassword = binding.regConfirmPasswordEt.text.toString()
            // validate.
            if (validateForm(email, username, password, confirmPassword)) {
                binding.regSubmitButton.visibility = View.INVISIBLE
                binding.regProgressBar.visibility = View.VISIBLE
                // create new user.
                createNewUser(email, username, password)
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
     * Validate Form.
     */
    private fun validateForm(email: String, username: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        // email validation.
        when {
            email.isEmpty() -> {
                isValid = false
                binding.regEmailEt.error = getText(R.string.empty_form)
            }
//            Pattern.matches(".*@.*", email) -> {
//                isValid = false
//                binding.regEmailEt.error = "invalid email"
//            }
            else -> {
                binding.regEmailEt.error = null
            }
        }

        // username validation.
        when {
            username.isEmpty() -> {
                isValid = false
                binding.regUsernameEt.error = getText(R.string.empty_form)
            }
            else -> {
                binding.regUsernameEt.error = null
            }
        }

        // password validation.
        when {
            password.isEmpty() -> {
                isValid = false
                binding.regPasswordEt.error = getText(R.string.empty_form)
            }
            password.length < 6 -> {
                isValid = false
                binding.regPasswordEt.error = getText(R.string.weak_password)
            }
            else -> {
                binding.regPasswordEt.error = null
            }
        }

        // confirm password validation.
        when {
            confirmPassword != password -> {
                isValid = false
                binding.regConfirmPasswordEt.error = getText(R.string.pswrd_not_match)
            }
            else -> {
                binding.regConfirmPasswordEt.error = null
            }
        }
        return isValid
    }

    /**
     * Create News User.
     */
    private fun createNewUser(email: String, username: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    val uid = user.uid
                    // Set user to db.
                    setUserToDb(uid, email, username)
                    // send verification letter to user email.
                    sendEmailVerification(user)
                } else {
                    binding.regSubmitButton.visibility = View.VISIBLE
                    binding.regProgressBar.visibility = View.GONE
                    Toast.makeText(context, getText(R.string.got_wrong), Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Send Email Verification.
     */
    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding.root.findNavController().navigate(R.id.action_registration_to_emailVerification)
                } else {
                    binding.regSubmitButton.visibility = View.VISIBLE
                    binding.regProgressBar.visibility = View.GONE
                    Toast.makeText(context, getText(R.string.got_wrong), Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Set User To Db.
     */
    private fun setUserToDb(uid: String, email: String, username: String) {
        val profileImages = listOf(
                "https://cdn.discordapp.com/attachments/712342039030923329/845835824025305088/redhood_face1.png",
                "https://cdn.discordapp.com/attachments/712342039030923329/845835839523127316/redhood_face2.png",
                "https://cdn.discordapp.com/attachments/712342039030923329/845835860767145994/redhood_face3.png",
                "https://cdn.discordapp.com/attachments/712342039030923329/845835877119819786/redhood_face4.png"
        )
        val profileImgUrl = profileImages[Random.nextInt(profileImages.size)]
        val user = User(uid, email, username, profileImgUrl, 0, 0, mutableListOf(START), mutableListOf(START))
        dbUserRef.child(uid).setValue(user)
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
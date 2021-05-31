package com.hera.redhood.ui.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.VerifiedInputEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.hera.redhood.R
import com.hera.redhood.databinding.FragmentLogInBinding
import com.hera.redhood.ui.base.Base

/**
 * LogIn Fragment.
 * Part of Authentication Activity.
 */
class LogIn : Fragment() {
    // firebase auth.
    private lateinit var auth: FirebaseAuth
    // binding.
    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // Setting firebase auth.
        auth = (activity as Authentication).auth

        // Setting binding.
        _binding = FragmentLogInBinding.inflate(inflater, container, false)

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.login_appbar_title)

        // Filling email form.
        setCurrentUserEmailToForm()

        // Setting on submit button click.
        binding.loginSubmitButton.setOnClickListener {
            hideKeyboard()
            val email = binding.loginEmailEt.text.toString()
            val password = binding.loginPasswordEt.text.toString()
            if (validateForm(email, password)) {
                binding.loginSubmitButton.visibility = View.INVISIBLE
                binding.loginProgressBar.visibility = View.VISIBLE
                signInUserWithEmailAndPassword(email, password)
            }
        }

        // Setting on password reset tv click.
        binding.loginPswrdResetTv.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_logIn_to_passwordReset)
        }

        // Setting on registration tv click.
        binding.loginRegTv.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_logIn_to_registration)
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
     * Set Current User Email To Form.
     */
    private fun setCurrentUserEmailToForm() {
        val user = auth.currentUser
        if (user != null) {
            binding.loginEmailEt.setText(user.email)
        }
    }

    /**
     * Validate Form.
     */
    private fun validateForm(email: String, password: String): Boolean {
        var isValid = true
        // email validation.
        if (email.isEmpty()) {
            isValid = false
            binding.loginEmailEt.error = getText(R.string.empty_form)
        } else {
            binding.loginEmailEt.error = null
        }
        // password validation.
        if (password.isEmpty()) {
            isValid = false
            binding.loginPasswordEt.error = getText(R.string.empty_form)
        } else {
            binding.loginPasswordEt.error = null
        }
        return isValid
    }

    /**
     * Sign In User With Email And Password.
     */
    private fun signInUserWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user!!.isEmailVerified) {
                        startActivity(Intent(activity, Base::class.java))
                        activity?.finish()
                    } else {
                        binding.loginSubmitButton.visibility = View.VISIBLE
                        binding.loginProgressBar.visibility = View.GONE
                        Toast.makeText(context, getText(R.string.email_not_verified), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.loginSubmitButton.visibility = View.VISIBLE
                    binding.loginProgressBar.visibility = View.GONE
                    Toast.makeText(context, getText(R.string.login_wrong_data), Toast.LENGTH_SHORT).show()
                }
            }
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
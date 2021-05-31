package com.hera.redhood.ui.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.hera.redhood.R

/**
 * Password Reset Fragment.
 * Part of Authentication Activity.
 */
class PasswordReset : Fragment() {
    // firebase auth.
    private lateinit var auth: FirebaseAuth
    // views.
    private lateinit var _view: View
    private lateinit var emailForm: EditText
    private lateinit var okButton: Button

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Setting auth.
        auth = (activity as Authentication).auth

        // Setting views.
        _view = inflater.inflate(R.layout.fragment_password_reset, container, false)
        emailForm = _view.findViewById(R.id.pswrd_res_et)
        okButton = _view.findViewById(R.id.pswrd_res_ok_button)

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.reset_appbar_title)

        // Setting on ok button click.
        okButton.setOnClickListener {
            hideKeyboard()
            val email = emailForm.text.toString()
            if (validateForm(email)) {
                sendPasswordResetEmail(email)
            }
        }

        return _view
    }

    /**
     * Validate Form.
     */
    private fun validateForm(email: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            isValid = false
            emailForm.error = getText(R.string.empty_form)
        } else {
            emailForm.error = null
        }
        return isValid
    }

    /**
     * Send Password Reset Email.
     */
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    _view.findNavController().navigate(R.id.action_passwordReset_to_logIn)
                } else {
                    Toast.makeText(context, getText(R.string.got_wrong), Toast.LENGTH_SHORT).show()
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
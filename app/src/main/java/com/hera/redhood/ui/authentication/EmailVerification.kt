package com.hera.redhood.ui.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.hera.redhood.R

/**
 * Email Verification Fragment.
 * Part of Authentication Activity.
 */
class EmailVerification : Fragment() {
    // views.
    private lateinit var _view: View
    private lateinit var continueButton: Button

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting view.
        _view = inflater.inflate(R.layout.fragment_email_verification, container, false)

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.verification_appbar_title)

        // Setting on continue button click.
        continueButton = _view.findViewById(R.id.email_ver_continue_button)
        continueButton.setOnClickListener {
            _view.findNavController().navigate(R.id.action_emailVerification_to_logIn)
        }

        return _view
    }
}
package com.hera.redhood.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.hera.redhood.R
import com.hera.redhood.ui.base.Base


class CreateHoodsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val database = (activity as Base).database

        val view = inflater.inflate(R.layout.fragment_create_hoods, container, false)

        return view
    }
}
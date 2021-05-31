package com.hera.redhood.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.hera.redhood.R
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.FragmentHoodBinding
import com.hera.redhood.utils.BundleKeys.CONTENT
import com.hera.redhood.utils.BundleKeys.EMAIL
import com.hera.redhood.utils.BundleKeys.ID_KEY
import com.hera.redhood.utils.BundleKeys.IMAGE_URL
import com.hera.redhood.utils.BundleKeys.LIKES
import com.hera.redhood.utils.BundleKeys.SUBSCRIBERS
import com.hera.redhood.utils.BundleKeys.TITLE
import com.hera.redhood.utils.BundleKeys.TOTAL_LIKES
import com.hera.redhood.utils.BundleKeys.USERNAME
import com.hera.redhood.utils.BundleKeys.USER_IMAGE_URL
import com.hera.redhood.utils.BundleKeys.USER_KEY

/**
 * Hood Fragment.
 */
class HoodFragment : Fragment() {
    // binding.
    private var _binding: FragmentHoodBinding? = null
    private val binding get() = _binding!!
    // database.
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var dbHoodRef: DatabaseReference
    // user.
    private lateinit var user: User

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Setting binding.
        _binding = FragmentHoodBinding.inflate(inflater, container, false)

        // Setting action bar title.
        (activity as AppCompatActivity).supportActionBar?. title = "ffff"

        // Setting ref.
        dbUserRef = (activity as Base).dbUserRef
        dbHoodRef = (activity as Base).dbHoodRef

        // Setting user.
        user = (activity as Base).user

        // Retrieving data from bundle.
        binding.hoodLytTitle.text = arguments?.getString(TITLE)
        binding.hoodLytContent.text = arguments?.getString(CONTENT)
        binding.hoodLytAuthorName.text = arguments?.getString(USERNAME)
        binding.hoodLytLikesCounter.text = arguments?.getInt(LIKES).toString()
        Glide.with(binding.root)
            .load(arguments?.getString(IMAGE_URL))
            .error(R.drawable.redhood_logo)
            .into(binding.hoodLytImage)
        Glide.with(binding.root)
            .load(arguments?.getString(USER_IMAGE_URL))
            .error(R.drawable.redhood_logo)
            .into(binding.hoodLytAuthorImg)

        // Setting on user icon click listener.
        binding.hoodLytAuthorImg.setOnClickListener {
            dbUserRef.get()
                    .addOnSuccessListener {
                        val userKey = arguments?.getString(USER_KEY)!!
                        val user = it.child(userKey).getValue(User::class.java)!!
                        val bundle = bundleOf(
                                ID_KEY to user.idKey,
                                EMAIL to user.email,
                                USERNAME to user.username,
                                IMAGE_URL to user.profileImgUrl,
                                SUBSCRIBERS to user.subscribers,
                                TOTAL_LIKES to user.totalLikes
                        )
                        binding.root.findNavController().navigate(R.id.action_global_userSheet, bundle)
                    }
        }

        // Setting on like button click.
        binding.hoodLytLikesBtn.setOnClickListener {
            val hoodKey = arguments?.getString(ID_KEY)!!
            if (user.likedHoods?.contains(hoodKey)!!) {
                dbHoodRef.get()
                    .addOnSuccessListener {
                        val hood = it.child(hoodKey).getValue(Hood::class.java)!!
                        hood.likes = hood.likes?.minus(1)
                        dbHoodRef.child(hoodKey).setValue(hood)
                        user.likedHoods?.remove(hoodKey)
                        dbUserRef.child(user.idKey!!).setValue(user)
                        binding.hoodLytLikesCounter.text = hood.likes.toString()
                        dbUserRef.get()
                            .addOnSuccessListener {
                                val userKey = arguments?.getString(USER_KEY)!!
                                val userAuthor = it.child(userKey).getValue(User::class.java)!!
                                userAuthor.totalLikes = userAuthor.totalLikes?.minus(1)
                                dbUserRef.child(userKey).setValue(userAuthor)
                            }
                    }
            } else {
                dbHoodRef.get()
                    .addOnSuccessListener {
                        val hood = it.child(hoodKey).getValue(Hood::class.java)!!
                        hood.likes = hood.likes?.plus(1)
                        dbHoodRef.child(hoodKey).setValue(hood)
                        user.likedHoods?.add(hoodKey)
                        dbUserRef.child(user.idKey!!).setValue(user)
                        binding.hoodLytLikesCounter.text = hood.likes.toString()
                        dbUserRef.get()
                            .addOnSuccessListener {
                                val userKey = arguments?.getString(USER_KEY)!!
                                val userAuthor = it.child(userKey).getValue(User::class.java)!!
                                userAuthor.totalLikes = userAuthor.totalLikes?.plus(1)
                                dbUserRef.child(userKey).setValue(userAuthor)
                            }
                    }
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
}
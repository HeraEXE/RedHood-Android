package com.hera.redhood.ui.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hera.redhood.R
import com.hera.redhood.adapters.HoodAdapter
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User
import com.hera.redhood.databinding.FragmentProfileSheetBinding
import com.hera.redhood.utils.BundleKeys
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

class ProfileSheet : Fragment() {
    // binding.
    private var _binding: FragmentProfileSheetBinding? = null
    private val binding get() = _binding!!
    // database ref.
    private lateinit var dbHoodRef: DatabaseReference
    // adapter.
    private lateinit var adapter: HoodAdapter
    private lateinit var behavior: Behavior
    // user.
    private lateinit var user: User

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting binding.
        _binding = FragmentProfileSheetBinding.inflate(inflater, container, false)

        // Setting user.
        user = (activity as Base).user

        // Setting ref.
        dbHoodRef = (activity as Base).dbHoodRef

        // Setting adapter.
        behavior = Behavior(user, binding.root)
        adapter = HoodAdapter(behavior)
        binding.profileSheetRecycler.adapter = adapter
        binding.profileSheetRecycler.layoutManager = CustomLinearLayoutManager(requireContext())

        // Retrieving data from bundle.
        binding.profileSheetName.text = arguments?.getString(USERNAME)
        binding.profileSheetEmail.text = arguments?.getString(EMAIL)
        binding.profileSheetSubsCount.text = arguments?.getInt(SUBSCRIBERS).toString()
        binding.profileSheetTotalLikesCount.text = arguments?.getInt(TOTAL_LIKES).toString()
        Glide.with(binding.root)
            .load(arguments?.getString(IMAGE_URL))
            .error(R.drawable.redhood_logo)
            .into(binding.profileSheetImg)

        // Setting Value Event Listener.
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                for (child in children) {
                    val hood = child.getValue(Hood::class.java)!!
                    if (hood.userKey == user.idKey) {
                        behavior.addHood(hood)
                        binding.profileSheetRecycler.adapter?.notifyDataSetChanged()
                    }
                }
                binding.profileSheetProgressBar.visibility = View.GONE
            }
        }
        dbHoodRef.addValueEventListener(listener)

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
     * Custom Linear Layout Manager.
     */
    class CustomLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
        override fun canScrollVertically(): Boolean {
            return false
        }
    }


    /**
     * HoodAdapter Behavior interface realization.
     */
    class Behavior(private val user: User,
                   private val view: View,
                   private val hoods: MutableList<Hood> = mutableListOf())
        : HoodAdapter.Behavior {
        override fun getItemCount(): Int {
            return hoods.size
        }

        override fun getHood(position: Int): Hood {
            return hoods[position]
        }

        override fun getUser(uid: String): User {
            return user
        }

        override fun onClick(
                hoodImg: String?,
                hoodTitle: String?,
                hoodContent: String?,
                authorName: String?,
                authorImg: String?,
                likes: Int?,
                userKey: String?,
                idKey: String?
        ) {
            val bundle = bundleOf(
                    ID_KEY to idKey,
                    USER_KEY to userKey,
                    TITLE to hoodTitle,
                    CONTENT to hoodContent,
                    IMAGE_URL to hoodImg,
                    LIKES to likes,
                    USERNAME to authorName,
                    USER_IMAGE_URL to authorImg
            )
            view.findNavController().navigate(R.id.action_global_editHood, bundle)
        }

        fun addHood(hood: Hood) {
            hoods.add(0, hood)
        }
    }
}
package com.hera.redhood.ui.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.hera.redhood.databinding.FragmentUserSheetBinding
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
 * User Sheet Fragment.
 */
class UserSheet : Fragment() {
    // binding.
    private var _binding: FragmentUserSheetBinding? = null
    private val binding get() = _binding!!
    // database ref.
    private lateinit var dbUserRef: DatabaseReference
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
        _binding = FragmentUserSheetBinding.inflate(inflater, container, false)

        // Setting database ref.
        dbUserRef = (activity as Base).dbUserRef
        dbHoodRef = (activity as Base).dbHoodRef

        // Setting user.
        user = (activity as Base).user

        // Setting adapter.
        behavior = Behavior(binding.root, user)
        adapter = HoodAdapter(behavior)
        binding.userSheetRecycler.adapter = adapter
        binding.userSheetRecycler.layoutManager = CustomLinearLayoutManager(requireContext())


        // Retrieving data from bundle.
        binding.userSheetName.text = arguments?.getString(USERNAME)
        binding.userSheetEmail.text = arguments?.getString(EMAIL)
        binding.userSheetSubsCount.text = arguments?.getInt(SUBSCRIBERS).toString()
        binding.userSheetTotalLikesCount.text = arguments?.getInt(TOTAL_LIKES).toString()
        Glide.with(binding.root)
                .load(arguments?.getString(IMAGE_URL))
                .error(R.drawable.redhood_logo)
                .into(binding.userSheetImg)


        // Setting Value Event Listener.
        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                val userKey = arguments?.getString(ID_KEY)!!
                dbUserRef.get()
                        .addOnSuccessListener {
                            val userAuthor = it.child(userKey).getValue(User::class.java)!!
                            behavior.setUser(userAuthor)
                            binding.userSheetRecycler.adapter?.notifyDataSetChanged()
                        }
                for (child in snapshot.children) {
                    val hood = child.getValue(Hood::class.java)!!
                    if (hood.userKey == userKey) {
                        behavior.addHood(hood)
                        binding.userSheetRecycler.adapter?.notifyDataSetChanged()
                    }
                }
                binding.userSheetProgressBar.visibility = View.GONE
            }
        }
        dbHoodRef.addValueEventListener(listener)

        // Setting on subscribe button click.
        binding.userSheetSubBtn.setOnClickListener {
            val userKey = arguments?.getString(ID_KEY)!!
            if (user.subscriptions?.contains(userKey)!!) {
                dbUserRef.get()
                    .addOnSuccessListener {
                        val userAuthor = it.child(userKey).getValue(User::class.java)!!
                        userAuthor.subscribers = userAuthor.subscribers?.minus(1)
                        dbUserRef.child(userKey).setValue(userAuthor)
                        user.subscriptions?.remove(userKey)
                        dbUserRef.child(user.idKey!!).setValue(user)
                        binding.userSheetSubsCount.text = userAuthor.subscribers.toString()
                    }
            } else {
                dbUserRef.get()
                    .addOnSuccessListener {
                        val userAuthor = it.child(userKey).getValue(User::class.java)!!
                        userAuthor.subscribers = userAuthor.subscribers?.plus(1)
                        dbUserRef.child(userKey).setValue(userAuthor)
                        user.subscriptions?.add(userKey)
                        dbUserRef.child(user.idKey!!).setValue(user)
                        binding.userSheetSubsCount.text = userAuthor.subscribers.toString()
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
    class Behavior(private val view: View,
                   private var userAuthor: User,
                   private val hoods: MutableList<Hood> = mutableListOf())
        : HoodAdapter.Behavior {
        override fun getItemCount(): Int {
            return hoods.size
        }

        override fun getHood(position: Int): Hood {
            return hoods[position]
        }

        override fun getUser(uid: String): User {
            return userAuthor
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
            view.findNavController().navigate(R.id.action_global_hoodFragment, bundle)
        }

        fun addHood(hood: Hood) {
            hoods.add(0, hood)
        }

        fun setUser(user: User) {
            userAuthor = user
        }
    }
}
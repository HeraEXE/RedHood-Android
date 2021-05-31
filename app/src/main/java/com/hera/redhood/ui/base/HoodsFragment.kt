package com.hera.redhood.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hera.redhood.R
import com.hera.redhood.adapters.HoodAdapter
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User
import com.hera.redhood.utils.BundleKeys

/**
 * Hoods Fragment.
 */
class HoodsFragment : Fragment() {
    // views.
    private lateinit var _view: View
    private lateinit var behavior: Behavior
    private lateinit var adapter: HoodAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    // user.
    private lateinit var currentUser: FirebaseUser
    // database references.
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var dbHoodRef: DatabaseReference

    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Setting user.
        currentUser = (activity as Base).currentUser

        // Setting database references.
        dbUserRef = (activity as Base).dbUserRef
        dbHoodRef = (activity as Base).dbHoodRef

        // Setting views.
        _view = inflater.inflate(R.layout.fragment_hoods, container, false)
        recycler = _view.findViewById(R.id.hoods_recycler)
        progressBar = _view.findViewById(R.id.hoods_progress_bar)

        // Setting adapter.
        behavior = Behavior(currentUser, _view)
        adapter = HoodAdapter(behavior)
        recycler.adapter = adapter

        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val hood = child.getValue(Hood::class.java)!!
                    behavior.addHood(hood)
                    dbUserRef.get().addOnSuccessListener {
                        val user = it.child(hood.userKey!!).getValue(User::class.java)!!
                        behavior.addUser(hood.userKey, user)
                        recycler.adapter?.notifyDataSetChanged()
                    }
                }
                progressBar.visibility = View.GONE
            }
        }
        dbHoodRef.addValueEventListener(listener)


        return _view
    }

    /**
     * Behavior.
     */
    class Behavior(private val currentUser: FirebaseUser,
                   private val view: View,
                   private val users: MutableMap<String, User> = mutableMapOf(),
                   private val hoods: MutableList<Hood> = mutableListOf())
        : HoodAdapter.Behavior {
        override fun getItemCount(): Int {
            return hoods.size
        }

        override fun getHood(position: Int): Hood {
            return hoods[position]
        }

        override fun getUser(uid: String): User {
            return users[uid] ?: User("g", "df", "f")
        }

        override fun onClick(hoodImg: String?,
                             hoodTitle: String?,
                             hoodContent: String?,
                             authorName: String?,
                             authorImg: String?,
                             likes: Int?,
                             userKey: String?,
                             idKey: String?
        ) {
            val bundle = bundleOf(
                BundleKeys.ID_KEY to idKey,
                BundleKeys.USER_KEY to userKey,
                BundleKeys.TITLE to hoodTitle,
                BundleKeys.CONTENT to hoodContent,
                BundleKeys.IMAGE_URL to hoodImg,
                BundleKeys.LIKES to likes,
                BundleKeys.USERNAME to authorName,
                BundleKeys.USER_IMAGE_URL to authorImg
            )
            if (currentUser.uid == userKey) {
                view.findNavController().navigate(R.id.action_global_editHood, bundle)
            } else {
                view.findNavController().navigate(R.id.action_global_hoodFragment, bundle)
            }
        }


        fun addHood(hood: Hood) {
            hoods.add(0, hood)
        }

        fun addUser(uid: String, user: User) {
            users[uid] = user
        }
    }
}
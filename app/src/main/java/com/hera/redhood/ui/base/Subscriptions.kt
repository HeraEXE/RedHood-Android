package com.hera.redhood.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hera.redhood.R
import com.hera.redhood.adapters.HoodAdapter
import com.hera.redhood.adapters.UserAdapter
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User
import com.hera.redhood.utils.BundleKeys
import com.hera.redhood.utils.BundleKeys.EMAIL
import com.hera.redhood.utils.BundleKeys.ID_KEY
import com.hera.redhood.utils.BundleKeys.IMAGE_URL
import com.hera.redhood.utils.BundleKeys.SUBSCRIBERS
import com.hera.redhood.utils.BundleKeys.TOTAL_LIKES
import com.hera.redhood.utils.BundleKeys.USERNAME


class Subscriptions : Fragment() {
    // views.
    private lateinit var _view: View
    private lateinit var behavior: Behavior
    private lateinit var adapter: UserAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    // user.
    private lateinit var user: User
    // database references.
    private lateinit var dbUserRef: DatabaseReference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Setting user.
        user = (activity as Base).user

        // Setting database ref.
        dbUserRef = (activity as Base).dbUserRef

        // Setting views.
        _view = inflater.inflate(R.layout.fragment_subscriptions, container, false)
        recycler = _view.findViewById(R.id.subscriptions_recycler)
        progressBar = _view.findViewById(R.id.subscriptions_progress_bar)

        // Setting adapter.
        behavior = Behavior(user, _view)
        adapter = UserAdapter(behavior)
        recycler.adapter = adapter
        recycler.layoutManager = GridLayoutManager(context, 2)

        val listener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val userAuthor = child.getValue(User::class.java)!!
                    if (user.subscriptions?.contains(userAuthor.idKey)!!) {
                        behavior.addUser(userAuthor)
                        recycler.adapter?.notifyDataSetChanged()
                    }
                }
                progressBar.visibility = View.GONE
            }
        }
        dbUserRef.addValueEventListener(listener)

        return _view
    }

    /**
     * Behavior.
     */
    class Behavior(private val user: User,
                   private val view: View,
                   private val users: MutableList<User> = mutableListOf())
        : UserAdapter.Behavior {
        override fun getItemCount(): Int {
            return users.size
        }

        override fun getUser(position: Int): User {
            return users[position]
        }

        override fun onClick(idKey: String?,
                             email: String?,
                             username: String?,
                             imageUrl: String?,
                             subscribers: Int?,
                             totalLikes: Int?
        ) {
            val bundle = bundleOf(
                ID_KEY to idKey,
                EMAIL to email,
                USERNAME to username,
                IMAGE_URL to imageUrl,
                SUBSCRIBERS to subscribers,
                TOTAL_LIKES to totalLikes
            )
            view.findNavController().navigate(R.id.action_global_userSheet, bundle)
        }

        fun addUser(user: User) {
            users.add(0, user)
        }
    }

}
package com.hera.redhood.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
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
 * User Hoods Fragment.
 */
class UserHoodsFragment : Fragment() {
    // views.
    private lateinit var _view: View
    private lateinit var behavior: Behavior
    private lateinit var adapter: HoodAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    // user.
    private lateinit var user: User
    // database ref.
    private lateinit var dbHoodRef: DatabaseReference


    /**
     * On Create View.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Setting user.
        user = (activity as Base).user

        // Setting database ref.
        dbHoodRef = (activity as Base).dbHoodRef

        // Setting action bar.
        (activity as AppCompatActivity).supportActionBar?.title = getText(R.string.user_hood_title)

        // Setting views.
        _view = inflater.inflate(R.layout.fragment_user_hoods, container, false)
        recycler = _view.findViewById(R.id.user_hoods_recycler)
        progressBar = _view.findViewById(R.id.user_hoods_progress_bar)

        // Setting adapter.
        behavior = Behavior(user, _view)
        adapter = HoodAdapter(behavior)
        recycler.adapter = adapter


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
                BundleKeys.ID_KEY to idKey,
                BundleKeys.USER_KEY to userKey,
                BundleKeys.TITLE to hoodTitle,
                BundleKeys.CONTENT to hoodContent,
                BundleKeys.IMAGE_URL to hoodImg,
                BundleKeys.LIKES to likes,
                BundleKeys.USERNAME to authorName,
                BundleKeys.USER_IMAGE_URL to authorImg
            )
            view.findNavController().navigate(R.id.action_global_editHood, bundle)
        }

        fun addHood(hood: Hood) {
            hoods.add(0, hood)
        }
    }
}
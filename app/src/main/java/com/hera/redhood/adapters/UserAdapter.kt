package com.hera.redhood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hera.redhood.R
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User

class UserAdapter(private val behavior: Behavior) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val userBlock: ConstraintLayout = view.findViewById(R.id.user_item_block)
        val userImg: de.hdodenhof.circleimageview.CircleImageView = view.findViewById(R.id.user_item_img)
        val userName: TextView = view.findViewById(R.id.user_item_name)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return behavior.getItemCount()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = behavior.getUser(position)
        holder.userName.text = user.username
        Glide.with(holder.view)
            .load(user.profileImgUrl)
            .error(R.drawable.redhood_logo)
            .into(holder.userImg)
        holder.userBlock.setOnClickListener {
            behavior.onClick(user.idKey, user.email, user.username, user.profileImgUrl, user.subscribers, user.totalLikes)
        }
    }

    interface Behavior {
        fun getItemCount(): Int

        fun getUser(position: Int): User

        fun onClick(
            idKey: String?,
            email: String?,
            username: String?,
            imageUrl: String?,
            subscribers: Int?,
            totalLikes: Int?
        )
    }
}
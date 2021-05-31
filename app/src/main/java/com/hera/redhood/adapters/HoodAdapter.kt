package com.hera.redhood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hera.redhood.R
import com.hera.redhood.data.models.Hood
import com.hera.redhood.data.models.User

class HoodAdapter(private val behavior: Behavior) : RecyclerView.Adapter<HoodAdapter.ViewHolder>() {

    /**
     * View Holder class.
     */
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val hoodItem: ConstraintLayout = view.findViewById(R.id.hood_item)
        val hoodImg: ImageView = view.findViewById(R.id.hood_img)
        val hoodTitle: TextView = view.findViewById(R.id.hood_title)
        val hoodAuthorImg: ImageView = view.findViewById(R.id.hood_author_img)
        val hoodAuthorName: TextView = view.findViewById(R.id.hood_author_name)
    }

    /**
     * On Create View Holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.hood_item_view, parent, false)
        return ViewHolder(view)
    }

    /**
     * Get Item Count.
     */
    override fun getItemCount(): Int {
        return behavior.getItemCount()
    }

    /**
     * On Bind View Holder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hood = behavior.getHood(position)
        val user = behavior.getUser(hood.userKey!!)
        holder.hoodTitle.text = hood.title
        holder.hoodAuthorName.text = user.username
        Glide.with(holder.view)
                .load(hood.imageUrl)
                .error(R.drawable.redhood_logo)
                .into(holder.hoodImg)
        Glide.with(holder.view)
                .load(user.profileImgUrl)
                .error(R.drawable.redhood_logo)
                .into(holder.hoodAuthorImg)
        holder.hoodItem.setOnClickListener {
            behavior.onClick(hood.imageUrl, hood.title, hood.content, user.username, user.profileImgUrl, hood.likes, hood.userKey, hood.idKey)
        }
    }

    /**
     * Interface Behavior.
     */
    interface Behavior {
        fun getItemCount(): Int

        fun getHood(position: Int): Hood

        fun getUser(uid: String): User

        fun onClick(hoodImg: String?,
                    hoodTitle: String?,
                    hoodContent: String?,
                    authorName: String?,
                    authorImg: String?,
                    likes: Int?,
                    userKey: String?,
                    idKey: String?
        )
    }
}
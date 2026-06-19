package com.example.paws.ui.screens.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.paws.R

class SearchAdapter(
    private var items: List<Any>,
    private val onPostClick: (PuppyPost) -> Unit,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_USER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is PuppyPost) TYPE_POST else TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_POST) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_card, parent, false)
            FeedAdapter.FeedViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false)
            UserViewHolder(view)
        }
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivSearchUserAvatar)
        val tvUsername: TextView = view.findViewById(R.id.tvSearchUsername)
        val tvAccountType: TextView = view.findViewById(R.id.tvSearchAccountType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is FeedAdapter.FeedViewHolder && item is PuppyPost) {
            bindPost(holder, item)
        } else if (holder is UserViewHolder && item is User) {
            bindUser(holder, item)
        }
    }

    private fun bindPost(holder: FeedAdapter.FeedViewHolder, post: PuppyPost) {
        holder.tvName.text = post.name
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("favorites")
            .whereEqualTo("postId", post.id)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    holder.tvLikesCount.text = snapshots.size().toString()
                }
            }

        val radiusPx = (32 * holder.itemView.context.resources.displayMetrics.density).toInt()

        if (post.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.bg_add_puppy_placeholder)
        }
        
        if (post.gender.lowercase() == "female") {
            holder.ivGender.setImageResource(R.drawable.ic_female)
            holder.containerGender.setBackgroundResource(R.drawable.bg_gender_female_circle)
        } else {
            holder.ivGender.setImageResource(R.drawable.ic_male)
            holder.containerGender.setBackgroundResource(R.drawable.bg_gender_circle)
        }

        holder.btnInfo.setOnClickListener { onPostClick(post) }
    }

    private fun bindUser(holder: UserViewHolder, user: User) {
        holder.tvUsername.text = user.username
        holder.tvAccountType.text = user.accountType

        ProfileImageManager.loadProfileImageForUid(holder.itemView.context, user.uid, holder.ivAvatar)

        holder.itemView.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount() = items.size

    fun getItemAt(position: Int): Any {
        return items[position]
    }

    fun updateItems(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }
}

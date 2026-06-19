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

class FeedAdapter(
    private var posts: List<PuppyPost>,
    private val onInfoClick: (PuppyPost) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivFeedImage)
        val tvName: TextView = view.findViewById(R.id.tvFeedName)
        val ivGender: ImageView = view.findViewById(R.id.ivGenderIcon)
        val containerGender: View = view.findViewById(R.id.containerGender)
        val btnInfo: View = view.findViewById(R.id.btnPuppyInfo)
        val tvLikesCount: TextView = view.findViewById(R.id.tvLikesCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_card, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val post = posts[position]
        holder.tvName.text = post.name
        
        // Listener in tempo reale per il numero di salvataggi effettivi
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
        
        // Gender logic
        if (post.gender.lowercase() == "female") {
            holder.ivGender.setImageResource(R.drawable.ic_female)
            holder.containerGender.setBackgroundResource(R.drawable.bg_gender_female_circle)
        } else {
            holder.ivGender.setImageResource(R.drawable.ic_male)
            holder.containerGender.setBackgroundResource(R.drawable.bg_gender_circle)
        }

        // Animal Type Icon
        // ivBreed is not in layout anymore, or we should find it if needed.
        // For now, let's remove the broken references.

        holder.btnInfo.setOnClickListener {
            onInfoClick(post)
        }
    }

    override fun getItemCount() = posts.size

    fun getPostAt(position: Int): PuppyPost {
        return posts[position]
    }

    fun updatePosts(newPosts: List<PuppyPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
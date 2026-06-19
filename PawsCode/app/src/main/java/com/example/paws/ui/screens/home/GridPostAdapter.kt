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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GridPostAdapter(
    private var posts: List<PuppyPost>,
    private val onPostClick: (PuppyPost) -> Unit,
    private val onFavoriteClick: (PuppyPost) -> Unit
) : RecyclerView.Adapter<GridPostAdapter.GridViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivGridPostImage)
        val tvName: TextView = view.findViewById(R.id.tvGridPostName)
        val btnFavorite: View = view.findViewById(R.id.btnGridFavorite)
        val ivHeart: ImageView = view.findViewById(R.id.ivGridFavoriteHeart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_post, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val post = posts[position]
        holder.tvName.text = post.name

        val radiusPx = (24 * holder.itemView.context.resources.displayMetrics.density).toInt()
        if (post.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.bg_add_puppy_placeholder)
        }

        // Check favorite status in real-time
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val favId = "${currentUser.uid}_${post.id}"
            db.collection("favorites").document(favId)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        holder.ivHeart.setImageResource(R.drawable.ic_heart_filled)
                        holder.ivHeart.setColorFilter(android.graphics.Color.parseColor("#FF9800")) // Orange
                    } else {
                        holder.ivHeart.setImageResource(R.drawable.ic_heart_outline)
                        holder.ivHeart.setColorFilter(android.graphics.Color.parseColor("#4A3B32")) // Dark Brown
                    }
                }
        }

        holder.itemView.setOnClickListener { onPostClick(post) }
        holder.btnFavorite.setOnClickListener { onFavoriteClick(post) }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<PuppyPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
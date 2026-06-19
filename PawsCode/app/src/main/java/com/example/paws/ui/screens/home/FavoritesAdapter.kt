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

class FavoritesAdapter(
    private var favorites: List<PuppyPost>,
    private val onUnlikeClick: (PuppyPost) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivFavoriteImage)
        val tvName: TextView = view.findViewById(R.id.tvFavoriteName)
        val btnUnlike: ImageView = view.findViewById(R.id.btnUnlike)
        val tvLikesCount: TextView = view.findViewById(R.id.tvFavoriteLikesCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_card, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val post = favorites[position]
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

        val radiusPx = (24 * holder.itemView.context.resources.displayMetrics.density).toInt()

        if (post.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.bg_add_puppy_placeholder)
        }

        holder.btnUnlike.setOnClickListener {
            onUnlikeClick(post)
        }
    }

    override fun getItemCount() = favorites.size

    fun updateFavorites(newFavorites: List<PuppyPost>) {
        favorites = newFavorites
        notifyDataSetChanged()
    }
}
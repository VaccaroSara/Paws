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

data class PuppyPost(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val gender: String = "male",
    val type: String = "Dog",
    val age: String = "1 years",
    val caption: String = "",
    val userType: String = "Private User"
)

class PostAdapter(
    private var posts: List<PuppyPost>,
    private val onAddClick: () -> Unit,
    private val onEditClick: (PuppyPost) -> Unit,
    private val onDeleteClick: (PuppyPost) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_POST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD else TYPE_POST
    }

    class AddViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivPostImage)
        val tvName: TextView = view.findViewById(R.id.tvPostName)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditPost)
        val btnDelete: View = view.findViewById(R.id.btnDeletePost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_button, parent, false)
            AddViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_post, parent, false)
            PostViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val radiusPx = (24 * holder.itemView.context.resources.displayMetrics.density).toInt()
        
        if (holder is AddViewHolder) {
            holder.itemView.setOnClickListener { onAddClick() }
        } else if (holder is PostViewHolder) {
            val post = posts[position - 1] // Subtract 1 for the ADD item
            holder.tvName.text = post.name
            
            if (post.imageUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(post.imageUrl)
                    .transform(CenterCrop(), RoundedCorners(radiusPx))
                    .into(holder.ivImage)
            } else {
                // Fallback image if needed
                holder.ivImage.setImageResource(R.drawable.bg_add_puppy_placeholder)
            }

            holder.btnEdit.setOnClickListener { onEditClick(post) }
            holder.btnDelete.setOnClickListener { onDeleteClick(post) }
        }
    }

    override fun getItemCount() = posts.size + 1

    fun updatePosts(newPosts: List<PuppyPost>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
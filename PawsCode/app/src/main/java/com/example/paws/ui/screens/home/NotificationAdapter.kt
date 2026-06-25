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

data class NotificationItem(
    val id: String = "",
    val text: String = "",
    val postImageUrl: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)

class NotificationAdapter(
    private var notifications: List<NotificationItem>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText: TextView = view.findViewById(R.id.tvNotificationText)
        val ivPostThumb: ImageView = view.findViewById(R.id.ivNotificationPostThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = notifications[position]
        holder.tvText.text = item.text

        if (item.postImageUrl.isNotEmpty()) {
            val radiusPx = (8 * holder.itemView.context.resources.displayMetrics.density).toInt()
            Glide.with(holder.itemView.context)
                .load(item.postImageUrl)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(holder.ivPostThumb)
            holder.ivPostThumb.visibility = View.VISIBLE
        } else {
            holder.ivPostThumb.visibility = View.GONE
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
package com.example.uniride

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private val notificationList: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNotificationMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.tvNotificationMessage.text = notification.message

        // Handle clicking the notification to call the number
        holder.itemView.setOnClickListener {
            // This regex looks for a sequence of 10-14 digits (your phone number)
            val numberPattern = "\\d{10,14}".toRegex()
            val match = numberPattern.find(notification.message)

            val phoneNumber = match?.value

            if (phoneNumber != null) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}
package com.example.moodconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for displaying a list of message items in a RecyclerView
class MessagesListAdapter(
    private var messages: List<MessageListItem>, // List of message items
    private val onItemClick: (MessageListItem) -> Unit // Click listener for each item
) : RecyclerView.Adapter<MessagesListAdapter.MessageListViewHolder>() {

    // ViewHolder class for holding the views of each item
    inner class MessageListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAvatar: ImageView = itemView.findViewById(R.id.imageViewAvatar) // ImageView for user avatar
        val textViewUserName: TextView = itemView.findViewById(R.id.textViewUserName) // TextView for user name
        val textViewLatestMessage: TextView = itemView.findViewById(R.id.textViewLatestMessage) // TextView for latest message
        val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp) // TextView for timestamp

        init {
            // Set click listener for the item view
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(messages[position]) // Call the onItemClick function with the clicked item
                }
            }
        }
    }

    // Inflate the item layout and create a ViewHolder for it
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_list, parent, false)
        return MessageListViewHolder(view)
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: MessageListViewHolder, position: Int) {
        val message = messages[position]
        holder.imageViewAvatar.setImageResource(message.avatarResId) // Set the avatar image
        holder.textViewUserName.text = message.userName // Set the user name
        holder.textViewLatestMessage.text = message.latestMessage // Set the latest message content
        holder.textViewTimestamp.text = android.text.format.DateFormat.format("hh:mm a", message.timestamp) // Set the timestamp
    }

    // Return the total number of items
    override fun getItemCount(): Int = messages.size

    // Update the list of messages and notify the adapter
    fun updateMessages(newMessages: List<MessageListItem>) {
        messages = newMessages
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}

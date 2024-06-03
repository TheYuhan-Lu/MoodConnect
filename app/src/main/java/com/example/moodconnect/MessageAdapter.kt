package com.example.moodconnect

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import java.io.File
import java.io.FileInputStream

// Adapter for handling different types of messages in a RecyclerView
class MessageAdapter(private var messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Constants representing different view types
    private val VIEW_TYPE_TEXT = 1
    private val VIEW_TYPE_IMAGE = 2
    private val VIEW_TYPE_LOCATION = 3

    // Create a new ViewHolder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_text, parent, false)
                TextMessageViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_image, parent, false)
                ImageMessageViewHolder(view)
            }
            VIEW_TYPE_LOCATION -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_location, parent, false)
                LocationMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    // Bind the data to the ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is TextMessageViewHolder -> holder.bind(message)
            is ImageMessageViewHolder -> holder.bind(message)
            is LocationMessageViewHolder -> holder.bind(message)
        }
    }

    // Get the total number of items
    override fun getItemCount(): Int = messages.size

    // Return the view type based on the message type
    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            MessageType.TEXT -> VIEW_TYPE_TEXT
            MessageType.IMAGE -> VIEW_TYPE_IMAGE
            MessageType.LOCATION -> VIEW_TYPE_LOCATION
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    // ViewHolder for text messages
    inner class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSenderName: TextView = itemView.findViewById(R.id.textViewSenderName)
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)

        fun bind(message: Message) {
            textViewSenderName.text = message.senderId // Set sender's ID or name
            textViewMessage.text = message.content // Set message content
            textViewTimestamp.text = android.text.format.DateFormat.format("hh:mm a", message.timestamp) // Set timestamp
        }
    }

    // ViewHolder for image messages
    inner class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSenderName: TextView = itemView.findViewById(R.id.textViewSenderName)
        private val imageViewMessage: ImageView = itemView.findViewById(R.id.imageViewMessage)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)

        fun bind(message: Message) {
            textViewSenderName.text = message.senderId // Set sender's ID or name
            textViewTimestamp.text = android.text.format.DateFormat.format("hh:mm a", message.timestamp) // Set timestamp

            // Set image based on either bitmap or URI
            if (message.imageBitmap != null) {
                imageViewMessage.setImageBitmap(message.imageBitmap)
            } else if (message.imageUri != null) {
                imageViewMessage.setImageURI(message.imageUri)
            }

            // Enlarge image on click
            imageViewMessage.setOnClickListener {
                val dialog = Dialog(itemView.context)
                dialog.setContentView(R.layout.dialog_image_fullscreen)
                val imageViewFullScreen = dialog.findViewById<ImageView>(R.id.imageViewFullScreen)

                if (message.imageBitmap != null) {
                    imageViewFullScreen.setImageBitmap(message.imageBitmap)
                } else if (message.imageUri != null) {
                    imageViewFullScreen.setImageURI(message.imageUri)
                }

                imageViewFullScreen.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
    }

    // ViewHolder for location messages
    inner class LocationMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSenderName: TextView = itemView.findViewById(R.id.textViewSenderName)
        private val textViewLocation: TextView = itemView.findViewById(R.id.textViewLocation)
        private val imageViewMap: ImageView = itemView.findViewById(R.id.imageViewMap)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)

        fun bind(message: Message) {
            val latLng = message.location ?: return

            textViewLocation.text = message.content // Display address information
            textViewSenderName.text = message.senderId // Set sender's ID or name
            textViewTimestamp.text = android.text.format.DateFormat.format("hh:mm a", message.timestamp) // Set timestamp

            // Set map image if available
            if (message.imageBitmap != null) {
                imageViewMap.setImageBitmap(message.imageBitmap)
            }

            // Open location in maps on click
            itemView.setOnClickListener {
                val uri = Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}(Location)")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                itemView.context.startActivity(intent)
            }
        }
    }

    // Function to add a new message to the list and notify the adapter
    fun addMessage(message: Message) {
        messages = messages + message
        notifyItemInserted(messages.size - 1)
    }
}

package com.example.moodconnect

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

// Data class representing a Message, which implements Parcelable for easy passing between components
data class Message(
    val id: String, // Unique identifier for the message
    val senderId: String, // ID of the sender
    val receiverId: String, // ID of the receiver
    val content: String, // Content of the message
    val timestamp: Long, // Timestamp of when the message was sent
    val type: MessageType, // Type of the message (e.g., TEXT, IMAGE, LOCATION)
    val imageBitmap: Bitmap? = null, // Optional: Bitmap for image messages
    val imageUri: Uri? = null, // Optional: URI for image messages
    val location: LatLng? = null // Optional: Location data for location messages
) : Parcelable {
    // Constructor to create a Message object from a Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Read id from the parcel
        parcel.readString() ?: "", // Read senderId from the parcel
        parcel.readString() ?: "", // Read receiverId from the parcel
        parcel.readString() ?: "", // Read content from the parcel
        parcel.readLong(), // Read timestamp from the parcel
        MessageType.valueOf(parcel.readString() ?: MessageType.TEXT.name), // Read and convert type from the parcel
        parcel.readParcelable(Bitmap::class.java.classLoader), // Read imageBitmap from the parcel
        parcel.readParcelable(Uri::class.java.classLoader), // Read imageUri from the parcel
        parcel.readParcelable(LatLng::class.java.classLoader) // Read location from the parcel
    )

    // Method to write the Message object to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id) // Write id to the parcel
        parcel.writeString(senderId) // Write senderId to the parcel
        parcel.writeString(receiverId) // Write receiverId to the parcel
        parcel.writeString(content) // Write content to the parcel
        parcel.writeLong(timestamp) // Write timestamp to the parcel
        parcel.writeString(type.name) // Write type to the parcel
        parcel.writeParcelable(imageBitmap, flags) // Write imageBitmap to the parcel
        parcel.writeParcelable(imageUri, flags) // Write imageUri to the parcel
        parcel.writeParcelable(location, flags) // Write location to the parcel
    }

    // Method to describe the contents of the Parcelable (default implementation)
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to generate instances of the Parcelable Message class
    companion object CREATOR : Parcelable.Creator<Message> {
        // Method to create a new instance of Message from a Parcel
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        // Method to create a new array of Message objects
        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }
}

// Enum class representing the type of the message
enum class MessageType {
    TEXT, // Text message
    IMAGE, // Image message
    LOCATION // Location message
}

package com.example.moodconnect

import android.os.Parcel
import android.os.Parcelable

// Data class representing a message list item, which implements Parcelable for easy passing between components
data class MessageListItem(
    val userId: String, // ID of the user
    val userName: String, // Name of the user
    val latestMessage: String, // Latest message content
    val timestamp: Long, // Timestamp of the latest message
    val avatarResId: Int // Resource ID for the user's avatar
) : Parcelable {
    // Constructor to create a MessageListItem object from a Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // Read userId from the parcel
        parcel.readString() ?: "", // Read userName from the parcel
        parcel.readString() ?: "", // Read latestMessage from the parcel
        parcel.readLong(), // Read timestamp from the parcel
        parcel.readInt() // Read avatarResId from the parcel
    )

    // Method to write the MessageListItem object to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId) // Write userId to the parcel
        parcel.writeString(userName) // Write userName to the parcel
        parcel.writeString(latestMessage) // Write latestMessage to the parcel
        parcel.writeLong(timestamp) // Write timestamp to the parcel
        parcel.writeInt(avatarResId) // Write avatarResId to the parcel
    }

    // Method to describe the contents of the Parcelable (default implementation)
    override fun describeContents(): Int {
        return 0
    }

    // Companion object to generate instances of the Parcelable MessageListItem class
    companion object CREATOR : Parcelable.Creator<MessageListItem> {
        // Method to create a new instance of MessageListItem from a Parcel
        override fun createFromParcel(parcel: Parcel): MessageListItem {
            return MessageListItem(parcel)
        }

        // Method to create a new array of MessageListItem objects
        override fun newArray(size: Int): Array<MessageListItem?> {
            return arrayOfNulls(size)
        }
    }
}

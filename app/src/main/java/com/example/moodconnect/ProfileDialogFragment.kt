package com.example.moodconnect

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

// Fragment class for displaying a profile dialog
class ProfileDialogFragment : DialogFragment() {

    // Method to create and return the view hierarchy associated with the fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_dialog, container, false)
    }

    // Method called when the fragment is visible to the user and actively running
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) // Set the dialog window size
        dialog?.window?.setGravity(Gravity.TOP or Gravity.END) // Position the dialog at the top right
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // Set the background of the dialog to be transparent
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation // Apply custom animations to the dialog
    }
}

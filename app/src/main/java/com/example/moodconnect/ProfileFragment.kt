package com.example.moodconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView

// Fragment class for displaying and editing the user's profile
class ProfileFragment : Fragment() {

    private lateinit var displayModeLayout: CardView // Layout for display mode
    private lateinit var editModeLayout: LinearLayout // Layout for edit mode

    private lateinit var imageViewProfile: ImageView // Profile image view
    private lateinit var textViewUserName: TextView // Text view for user name
    private lateinit var textViewBirthday: TextView // Text view for birthday
    private lateinit var textViewEmail: TextView // Text view for email
    private lateinit var textViewContact: TextView // Text view for contact
    private lateinit var textViewAddress: TextView // Text view for address

    private lateinit var editTextUserName: EditText // Edit text for user name
    private lateinit var editTextBirthday: EditText // Edit text for birthday
    private lateinit var editTextEmail: EditText // Edit text for email
    private lateinit var editTextContact: EditText // Edit text for contact
    private lateinit var editTextAddress: EditText // Edit text for address

    private lateinit var buttonEdit: Button // Button for edit action
    private lateinit var buttonSave: Button // Button for save action
    private lateinit var buttonCancel: Button // Button for cancel action

    // Method to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        displayModeLayout = view.findViewById(R.id.displayModeLayout)
        editModeLayout = view.findViewById(R.id.editModeLayout)

        imageViewProfile = view.findViewById(R.id.imageViewProfile)
        textViewUserName = view.findViewById(R.id.textViewUserName)
        textViewBirthday = view.findViewById(R.id.textViewBirthday)
        textViewEmail = view.findViewById(R.id.textViewEmail)
        textViewContact = view.findViewById(R.id.textViewContact)
        textViewAddress = view.findViewById(R.id.textViewAddress)

        editTextUserName = view.findViewById(R.id.editTextUserName)
        editTextBirthday = view.findViewById(R.id.editTextBirthday)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextContact = view.findViewById(R.id.editTextContact)
        editTextAddress = view.findViewById(R.id.editTextAddress)

        buttonEdit = view.findViewById(R.id.buttonEdit)
        buttonSave = view.findViewById(R.id.buttonSave)
        buttonCancel = view.findViewById(R.id.buttonCancel)

        // Set click listeners for buttons
        buttonEdit.setOnClickListener {
            toggleEditMode(true)
        }

        buttonSave.setOnClickListener {
            saveProfile()
            toggleEditMode(false)
        }

        buttonCancel.setOnClickListener {
            toggleEditMode(false)
        }

        // Show main UI elements (logo bar and navigation) when this fragment is created
        (activity as MainActivity).showMainUI()
        return view
    }

    // Method to toggle between display and edit modes
    private fun toggleEditMode(editMode: Boolean) {
        if (editMode) {
            displayModeLayout.visibility = View.GONE // Hide display mode layout
            editModeLayout.visibility = View.VISIBLE // Show edit mode layout
            // Populate edit texts with current profile information
            editTextUserName.setText(textViewUserName.text)
            editTextBirthday.setText(textViewBirthday.text.removePrefix("Birthday: "))
            editTextEmail.setText(textViewEmail.text.removePrefix("Email: "))
            editTextContact.setText(textViewContact.text.removePrefix("Contact: "))
            editTextAddress.setText(textViewAddress.text.removePrefix("Address: "))
        } else {
            displayModeLayout.visibility = View.VISIBLE // Show display mode layout
            editModeLayout.visibility = View.GONE // Hide edit mode layout
        }
    }

    // Method to save the profile information
    private fun saveProfile() {
        textViewUserName.text = editTextUserName.text.toString() // Save user name
        textViewBirthday.text = "Birthday: ${editTextBirthday.text}" // Save birthday
        textViewEmail.text = "Email: ${editTextEmail.text}" // Save email
        textViewContact.text = "Contact: ${editTextContact.text}" // Save contact
        textViewAddress.text = "Address: ${editTextAddress.text}" // Save address
    }
}

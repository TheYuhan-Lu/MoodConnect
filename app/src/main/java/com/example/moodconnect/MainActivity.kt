package com.example.moodconnect

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // Declare UI components
    private lateinit var logoBar: LinearLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    // Override the onCreate method to initialize the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the layout for this activity

        // Initialize UI components
        logoBar = findViewById(R.id.logoBarContainer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Set a listener for the bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                // Handle navigation to the Messages screen
                R.id.navigation_messages -> {
                    showMainUI() // Ensure main UI components are visible
                    loadFragment(MessagesListFragment()) // Load the MessagesListFragment
                    true
                }
                // Handle navigation to the Profile screen
                R.id.navigation_profile -> {
                    showMainUI() // Ensure main UI components are visible
                    loadFragment(ProfileFragment()) // Load the ProfileFragment
                    true
                }
                else -> false // Return false for any other unhandled items
            }
        }

        // Load the default fragment (MessagesListFragment) if no state is saved
        if (savedInstanceState == null) {
            loadFragment(MessagesListFragment())
        }
    }

    // Function to load a fragment into the frame layout
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment) // Replace the current fragment
            .commit() // Commit the transaction
    }

    // Function to show the chat fragment when a message list item is selected
    fun showChatFragment(messageListItem: MessageListItem) {
        hideMainUI() // Hide main UI components
        val chatFragment = MessagesFragment.newInstance(messageListItem) // Create a new instance of MessagesFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, chatFragment) // Replace the current fragment with the chat fragment
            .addToBackStack(null) // Add the transaction to the back stack
            .commit() // Commit the transaction
    }

    // Function to hide main UI components (logo bar and bottom navigation)
    fun hideMainUI() {
        logoBar.visibility = View.GONE
        bottomNavigationView.visibility = View.GONE
    }

    // Function to show main UI components (logo bar and bottom navigation)
    fun showMainUI() {
        logoBar.visibility = View.VISIBLE
        bottomNavigationView.visibility = View.VISIBLE
    }
}

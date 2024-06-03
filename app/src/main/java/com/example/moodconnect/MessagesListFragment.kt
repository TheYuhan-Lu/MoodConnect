package com.example.moodconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Fragment for displaying a list of message items
class MessagesListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messagesListAdapter: MessagesListAdapter

    // Method to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages_list, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMessagesList)
        messagesListAdapter = MessagesListAdapter(listOf()) { messageListItem ->
            val fragment = MessagesFragment.newInstance(messageListItem)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = messagesListAdapter

        // Add some sample data
        val sampleData = listOf(
            MessageListItem("1", "John Doe", "", System.currentTimeMillis(), R.drawable.baseline_account_circle_24),
            MessageListItem("2", "User 2", "Hi", System.currentTimeMillis(), R.drawable.baseline_account_circle_24)
        )
        messagesListAdapter.updateMessages(sampleData)

        // Show main UI elements (logo bar and navigation) when this fragment is created
        (activity as MainActivity).showMainUI()
        return view
    }
}

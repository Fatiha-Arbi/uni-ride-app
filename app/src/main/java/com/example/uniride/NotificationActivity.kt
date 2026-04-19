package com.example.uniride

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationList: ArrayList<Notification>
    private val db = FirebaseFirestore.getInstance()
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Make the Back button work
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Closes this screen and returns to the Feed
        }

        // Set up the RecyclerView
        rvNotifications = findViewById(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        notificationList = arrayListOf()
        notificationAdapter = NotificationAdapter(notificationList)
        rvNotifications.adapter = notificationAdapter

        // Fetch the data
        fetchNotifications()
    }

    private fun fetchNotifications() {
        if (currentUserEmail == null) return

        // Ask Firebase ONLY for notifications meant for the logged-in user
        db.collection("notifications")
            .whereEqualTo("receiverEmail", currentUserEmail)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FirestoreError", error.message.toString())
                    return@addSnapshotListener
                }

                if (value != null) {
                    notificationList.clear()
                    for (document in value.documents) {
                        val notification = document.toObject(Notification::class.java)
                        if (notification != null) {
                            notificationList.add(notification)
                        }
                    }

                    // Sort the list so the newest notifications appear at the very top!
                    notificationList.sortByDescending { it.timestamp }

                    notificationAdapter.notifyDataSetChanged()
                }
            }
    }
}
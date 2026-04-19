package com.example.uniride

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange

class RideFeedActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var rideAdapter: RideAdapter
    private lateinit var rideList: ArrayList<Ride>
    private lateinit var db: FirebaseFirestore

    // We capture the exact time the user opens this screen
    private val loginTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_feed)

        recyclerView = findViewById(R.id.recyclerViewRides)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        rideList = arrayListOf()
        rideAdapter = RideAdapter(rideList)
        recyclerView.adapter = rideAdapter

        db = FirebaseFirestore.getInstance()

        fetchRides()

        // --- NEW: Start listening for popups! ---
        listenForNewPopups()

        val btnGoToCreateRide = findViewById<Button>(R.id.btnGoToCreateRide)
        btnGoToCreateRide.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = android.content.Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // NOTE: This uses findViewById<TextView> now because we changed it in the XML!
        val btnNotifications = findViewById<TextView>(R.id.btnNotifications)
        btnNotifications.setOnClickListener {
            val intent = android.content.Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchRides() {
        db.collection("rides")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FirestoreError", error.message.toString())
                    Toast.makeText(this, "Error loading rides", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (value != null) {
                    rideList.clear()
                    for (document in value.documents) {
                        val ride = document.toObject(Ride::class.java)
                        if (ride != null) {
                            rideList.add(ride)
                        }
                    }
                    rideAdapter.notifyDataSetChanged()
                }
            }
    }

    // --- NEW POPUP LOGIC ---
    private fun listenForNewPopups() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return

        // Listen ONLY for notifications sent to this user, created AFTER they opened the app
        db.collection("notifications")
            .whereEqualTo("receiverEmail", currentUserEmail)
            .whereGreaterThan("timestamp", loginTime)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                if (snapshots != null) {
                    // Check if the database change is a brand NEW document
                    for (dc in snapshots.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val newNotification = dc.document.toObject(Notification::class.java)
                            showPopupDialog(newNotification.message)
                        }
                    }
                }
            }
    }

    private fun showPopupDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("🔔 New Update!")
            .setMessage(message)
            .setPositiveButton("Awesome") { dialog, _ ->
                dialog.dismiss() // Close the popup when clicked
            }
            .create()
            .show()
    }
}
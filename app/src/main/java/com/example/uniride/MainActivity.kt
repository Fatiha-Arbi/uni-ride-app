package com.example.uniride

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var etPickup: EditText
    private lateinit var etDestination: EditText
    private lateinit var etFare: EditText
    private lateinit var etSeats: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnCreateRide: Button

    private lateinit var db: FirebaseFirestore

    // NEW: Variables to store coordinates
    private var pickupLat: Double = 0.0
    private var pickupLng: Double = 0.0
    private var destLat: Double = 0.0
    private var destLng: Double = 0.0

    // Launcher for the MapPickerActivity
    private val mapPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val lat = data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lng = data?.getDoubleExtra("lng", 0.0) ?: 0.0
            val type = data?.getStringExtra("type")

            if (type == "pickup") {
                pickupLat = lat
                pickupLng = lng
                etPickup.setText("Location Set (Map) 📍")
            } else if (type == "dest") {
                destLat = lat
                destLng = lng
                etDestination.setText("Location Set (Map) 🏁")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()

        etPickup = findViewById(R.id.etPickup)
        etDestination = findViewById(R.id.etDestination)
        etFare = findViewById(R.id.etFare)
        etSeats = findViewById(R.id.etSeats)
        etPhone = findViewById(R.id.etPhone)
        btnCreateRide = findViewById(R.id.btnCreateRide)

        // Button to pick Pickup Location
        findViewById<Button>(R.id.btnPickPickup).setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            intent.putExtra("type", "pickup")
            mapPickerLauncher.launch(intent)
        }

        // Button to pick Destination
        findViewById<Button>(R.id.btnPickDest).setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            intent.putExtra("type", "dest")
            mapPickerLauncher.launch(intent)
        }

        btnCreateRide.setOnClickListener { saveRideToDatabase() }

        findViewById<Button>(R.id.btnViewRides).setOnClickListener {
            startActivity(Intent(this, RideFeedActivity::class.java))
        }
    }

    private fun saveRideToDatabase() {
        val pickup = etPickup.text.toString().trim()
        val destination = etDestination.text.toString().trim()
        val fareString = etFare.text.toString().trim()
        val seatsString = etSeats.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (pickup.isEmpty() || destination.isEmpty() || fareString.isEmpty() || seatsString.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure they actually used the map
        if (pickupLat == 0.0 || destLat == 0.0) {
            Toast.makeText(this, "Please select locations on the map", Toast.LENGTH_SHORT).show()
            return
        }

        val fare = fareString.toDoubleOrNull() ?: 0.0
        val seats = seatsString.toIntOrNull() ?: 0
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown"

        val newRide = Ride(
            hostId = currentUserEmail,
            hostPhone = phone,
            pickupLocation = pickup,
            destination = destination,
            fare = fare,
            totalSeats = seats,
            availableSeats = seats,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            destLat = destLat,
            destLng = destLng
        )

        db.collection("rides").add(newRide).addOnSuccessListener {
            Toast.makeText(this, "Ride created with Map Data!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
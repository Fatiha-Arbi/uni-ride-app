package com.example.uniride

import com.google.firebase.firestore.DocumentId

data class Ride(
    @DocumentId val rideId: String = "",
    val hostId: String = "",
    val hostPhone: String = "",
    val pickupLocation: String = "",
    val destination: String = "",
    val fare: Double = 0.0,
    val totalSeats: Int = 0,
    var availableSeats: Int = 0,
    // NEW: Coordinate fields
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val destLat: Double = 0.0,
    val destLng: Double = 0.0
)
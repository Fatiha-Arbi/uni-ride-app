package com.example.uniride

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RideAdapter(private val rideList: List<Ride>) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRoute: TextView = itemView.findViewById(R.id.tvRoute)
        val tvFare: TextView = itemView.findViewById(R.id.tvFare)
        val tvSeats: TextView = itemView.findViewById(R.id.tvSeats)
        val btnJoinRide: Button = itemView.findViewById(R.id.btnJoinRide)
        val btnDeleteRide: Button = itemView.findViewById(R.id.btnDeleteRide)
        val btnViewMap: Button = itemView.findViewById(R.id.btnViewMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ride, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val currentRide = rideList[position]

        holder.tvRoute.text = "${currentRide.pickupLocation} → ${currentRide.destination}"
        holder.tvFare.text = "Fare: ৳${currentRide.fare}"
        holder.tvSeats.text = "Seats Available: ${currentRide.availableSeats}"

        // --- UPDATED MAP BUTTON LOGIC ---
        holder.btnViewMap.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, ViewRideMapActivity::class.java)

            // Passing coordinates
            intent.putExtra("pLat", currentRide.pickupLat)
            intent.putExtra("pLng", currentRide.pickupLng)
            intent.putExtra("dLat", currentRide.destLat)
            intent.putExtra("dLng", currentRide.destLng)

            // NEW: Passing the location names for the map markers
            intent.putExtra("pName", currentRide.pickupLocation)
            intent.putExtra("dName", currentRide.destination)

            holder.itemView.context.startActivity(intent)
        }

        if (currentRide.hostId == currentUserEmail) {
            holder.btnJoinRide.visibility = View.GONE
            holder.btnDeleteRide.visibility = View.VISIBLE

            holder.btnDeleteRide.setOnClickListener {
                db.collection("rides").document(currentRide.rideId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Ride Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to cancel ride", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            holder.btnDeleteRide.visibility = View.GONE
            holder.btnJoinRide.visibility = View.VISIBLE

            if (currentRide.availableSeats <= 0) {
                holder.btnJoinRide.text = "Ride Full"
                holder.btnJoinRide.isEnabled = false
                holder.btnJoinRide.setBackgroundColor(Color.GRAY)
            } else {
                holder.btnJoinRide.text = "Join Ride"
                holder.btnJoinRide.isEnabled = true
                holder.btnJoinRide.setBackgroundColor(Color.parseColor("#28A745"))

                holder.btnJoinRide.setOnClickListener {
                    val input = android.widget.EditText(holder.itemView.context)
                    input.inputType = android.text.InputType.TYPE_CLASS_PHONE
                    input.hint = "Enter your phone number (e.g. 017...)"

                    android.app.AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Confirm Booking")
                        .setMessage("Please enter your phone number so the host can contact you.")
                        .setView(input)
                        .setPositiveButton("Confirm") { dialog, _ ->
                            val joinerPhone = input.text.toString().trim()
                            if (joinerPhone.isEmpty()) {
                                Toast.makeText(holder.itemView.context, "Phone number required!", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }

                            val newSeatCount = currentRide.availableSeats - 1
                            db.collection("rides").document(currentRide.rideId)
                                .update("availableSeats", newSeatCount)
                                .addOnSuccessListener {
                                    val hostNotification = hashMapOf(
                                        "receiverEmail" to currentRide.hostId,
                                        "message" to "$currentUserEmail joined your ride! Call them at: $joinerPhone",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    db.collection("notifications").add(hostNotification)

                                    val joinerNotification = hashMapOf(
                                        "receiverEmail" to currentUserEmail,
                                        "message" to "Booked ride to ${currentRide.destination}! Call the host at: ${currentRide.hostPhone}",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    db.collection("notifications").add(joinerNotification)

                                    Toast.makeText(holder.itemView.context, "Seat Booked! Check notifications for host's number.", Toast.LENGTH_LONG).show()
                                }
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return rideList.size
    }
}
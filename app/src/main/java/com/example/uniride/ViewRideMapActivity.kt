package com.example.uniride

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ViewRideMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_view_ride_map)

        val mapView = findViewById<MapView>(R.id.viewMapView)
        mapView.setMultiTouchControls(true)

        // Get coordinates AND names passed from the Adapter
        val pLat = intent.getDoubleExtra("pLat", 0.0)
        val pLng = intent.getDoubleExtra("pLng", 0.0)
        val dLat = intent.getDoubleExtra("dLat", 0.0)
        val dLng = intent.getDoubleExtra("dLng", 0.0)
        val pName = intent.getStringExtra("pName") ?: "Pickup"
        val dName = intent.getStringExtra("dName") ?: "Destination"

        val pickupPoint = GeoPoint(pLat, pLng)
        val destPoint = GeoPoint(dLat, dLng)

        // 1. Setup Pickup Marker (RED as requested)
        val pickupMarker = Marker(mapView)
        pickupMarker.position = pickupPoint
        pickupMarker.title = "Pickup: $pName"

        // Changing color to RED
        val redIcon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default)
        redIcon?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
        pickupMarker.icon = redIcon

        pickupMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(pickupMarker)

        // 2. Setup Destination Marker (GREEN as requested)
        val destMarker = Marker(mapView)
        destMarker.position = destPoint
        destMarker.title = "Destination: $dName"

        // Changing color to GREEN
        val greenIcon = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default)
        greenIcon?.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN)
        destMarker.icon = greenIcon

        destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(destMarker)

        // Zoom and Center
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(pickupPoint)

        findViewById<Button>(R.id.btnBackFromMap).setOnClickListener { finish() }
    }
}
package com.example.uniride

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import android.widget.Button

class MapPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var selectedPoint: GeoPoint? = null
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSM needs to know your package name to download tiles
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_map_picker)

        mapView = findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        // Set starting point (East West University/Aftabnagar area)
        val startPoint = GeoPoint(23.7685, 90.4255)
        mapView.controller.setZoom(17.0)
        mapView.controller.setCenter(startPoint)

        // Listen for taps on the map
        val eventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    selectedPoint = it
                    updateMarker(it)
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }

        val overlay = MapEventsOverlay(eventsReceiver)
        mapView.overlays.add(overlay)

        // UPDATED: Now passes the 'type' back to MainActivity
        findViewById<Button>(R.id.btnConfirmLocation).setOnClickListener {
            selectedPoint?.let {
                val resultIntent = Intent()
                resultIntent.putExtra("lat", it.latitude)
                resultIntent.putExtra("lng", it.longitude)

                // This ensures MainActivity knows if this was for Pickup or Destination
                resultIntent.putExtra("type", intent.getStringExtra("type"))

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun updateMarker(point: GeoPoint) {
        if (marker == null) {
            marker = Marker(mapView)
            mapView.overlays.add(marker)
        }
        marker?.position = point
        marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.invalidate() // Refresh map
    }
}
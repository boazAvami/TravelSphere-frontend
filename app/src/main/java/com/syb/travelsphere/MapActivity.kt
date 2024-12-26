package com.syb.travelsphere

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {

    lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        mapView = findViewById(R.id.map)

        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(37.7749, -122.4194))  // Default center

        // Add markers
        addMarker(37.7749, -122.4194, "San Francisco")
        addMarker(34.0522, -118.2437, "Los Angeles")
        addMarker(40.7128, -74.0060, "New York")
    }

    private fun addMarker(lat: Double, lon: Double, title: String) {
        val marker = Marker(mapView)
        marker.icon = resources.getDrawable(R.drawable.location, null)

        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        mapView.overlays.add(marker)
        mapView.invalidate()  // Refresh the map to show markers
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()  // This is important for pausing and resuming the map lifecycle
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Don't forget to pause the map on pause
    }
}

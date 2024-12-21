package com.syb.travelsphere

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint

class MapActivity : AppCompatActivity() {

    lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Load osmdroid configuration
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        // Initialize the MapView
        mapView = findViewById(R.id.map)

        // Set zoom and center to a default location (e.g., San Francisco)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(37.7749, -122.4194))
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

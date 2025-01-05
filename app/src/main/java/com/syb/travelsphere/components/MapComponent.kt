package com.syb.travelsphere.components

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.syb.travelsphere.R
import com.syb.travelsphere.services.Post
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MapView(context, attrs) {

    init {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        controller.setZoom(15.0)
        controller.setCenter(GeoPoint(37.7749, -122.4194))  // Default center
    }

    // Method to add markers to the map based on the posts
    fun displayPosts(posts: List<Post>?) {
        posts?.forEach { post ->
            val geotag = post.geotag
            post._id?.let {
                addMarker(geotag.coordinates[1], geotag.coordinates[0], post.location, it, post.description)
            }
        }
    }

    // Method to add a marker on the map
    private fun addMarker(lat: Double, lon: Double, title: String, postId: String, description: String) {
        val marker = Marker(this)
        marker.icon = resources.getDrawable(R.drawable.location, null)

        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        // Set up marker click listener
        marker.setOnMarkerClickListener { _, _ ->
            // Show post details in a Toast
            Toast.makeText(context, "Post ID: $postId\nDescription: $description", Toast.LENGTH_LONG).show()
            true
        }

        // Add the marker to the map
        overlays.add(marker)
        invalidate()  // Refresh the map to show markers
    }

    // Method to center the map on a specific location
    fun centerMapOnLocation(lat: Double, lon: Double) {
        val geoPoint = GeoPoint(lat, lon)
        controller.setCenter(geoPoint)
    }
}


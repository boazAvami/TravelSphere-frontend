package com.syb.travelsphere.components

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.syb.travelsphere.R
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User
import com.syb.travelsphere.utils.ImagePickerUtil
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
        clearMap();

        posts?.forEach { post ->
            val geotag = post.location
            addPostMarker(geotag.latitude, geotag.longitude, post.locationName, post.id, post.description)
        }

        posts?.forEach { post ->
            val geoPoint = post.location
            if (geoPoint != null) {
                addPostMarker(
                    geoPoint.latitude,
                    geoPoint.longitude,
                    post.locationName,
                    post.id,
                    post.description
                )
            }
        }
    }

    // Method to add a marker on the map
    private fun addPostMarker(lat: Double, lon: Double, title: String, postId: String, description: String) {
        val marker = Marker(this)
        marker.icon = resources.getDrawable(R.drawable.location, null)

        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        // Set up marker click listener
        marker.setOnMarkerClickListener { _, _ ->
            // Show post details in a Toast
            // TODO : add pop up of post with all the details
            Toast.makeText(context, "Post ID: $postId\nDescription: $description", Toast.LENGTH_LONG).show()
            true
        }

        // Add the marker to the map
        overlays.add(marker)
        invalidate()  // Refresh the map to show markers
    }

    // Method to add markers to the map based on the users
    fun displayUsers(users: List<User>?) {
        clearMap();

        users?.forEach { user ->
            val geoPoint  = user.location
            if (geoPoint  != null) {
                addUserMarker(
                    geoPoint.latitude,
                    geoPoint.longitude,
                    user
                )
            }
        }
    }

    // Method to add a marker on the map for a user
    private fun addUserMarker(lat: Double, lon: Double, user: User) {
        val marker = Marker(this)
        marker.icon = resources.getDrawable(R.drawable.location, null) // Placeholder icon

        // Set the marker's position and title
        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = user.userName

        // Optionally, set the profile picture if provided (this assumes you have a method to load images)
        user.profilePictureUrl?.let {
            // Set the icon to the user's profile picture if available (example, you need an image loader)
//                Model.shared.getImageByUrl(
//                    imageUrl = it
//                ) { bitmap ->
//                    marker.icon = bitmap
//                }
             //marker.icon = TODO: set icon to image profile picture
        }

        // Set up marker click listener
        marker.setOnMarkerClickListener { _, _ ->
            // Show user details in a Toast
            Toast.makeText(context, "User: ${user.userName}\nProfile: ${user.profilePictureUrl}", Toast.LENGTH_LONG).show()
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

    // New method to clear all markers from the map
    fun clearMap() {
        overlays.clear() // Clears all overlays (markers)
        invalidate() // Refresh the map to remove all markers
    }
}

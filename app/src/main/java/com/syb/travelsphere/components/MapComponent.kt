package com.syb.travelsphere.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.Toast
import androidx.navigation.NavController
import com.syb.travelsphere.R
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MapView(context, attrs) {

    private var navController: NavController? = null

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    init {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        controller.setZoom(15.0)
        controller.setCenter(GeoPoint(37.7749, -122.4194))  // Default center
    }

    // Method to add markers to the map based on the posts
    fun displayPosts(posts: List<Post>?,
                     onPostClick: (String) -> Unit
    ) {
        clearMap()

        posts?.forEach { post ->
            val geotag = post.location
            addPostMarker(
                geotag.latitude,
                geotag.longitude,
                post.locationName,
                post.id,
                post.description,
                onPostClick
            )
        }

        posts?.forEach { post ->
            val geoPoint = post.location
            if (geoPoint != null) {
                addPostMarker(
                    geoPoint.latitude,
                    geoPoint.longitude,
                    post.locationName,
                    post.id,
                    post.description,
                    onPostClick
                )
            }
        }
    }

    // Method to add a marker on the map
    private fun addPostMarker(
        lat: Double,
        lon: Double,
        title: String,
        postId: String,
        description: String,
        onPostClick: (String) -> Unit
    ) {
        val marker = Marker(this)
        marker.icon = resources.getDrawable(R.drawable.location, null)

        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title

        // Set up marker click listener

        marker.setOnMarkerClickListener { _, _ ->
            onPostClick(postId) // Call the navigation function passed from the fragment
            true
        }

        // Add the marker to the map
        overlays.add(marker)
        invalidate()  // Refresh the map to show markers
    }

    // Method to add markers to the map based on the users
    fun displayUsers(users: List<User>?,
                     onPostClick: (String, String) -> Unit) {
        clearMap();

        users?.forEach { user ->
            val geoPoint  = user.location
            if (geoPoint  != null) {
                addUserMarker(
                    geoPoint.latitude,
                    geoPoint.longitude,
                    user,
                    onPostClick
                )
            }
        }
    }

    // Method to add a marker on the map for a user
    private fun addUserMarker(lat: Double, lon: Double, user: User, onPostClick: (String, String) -> Unit) {
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
//                    // Convert 100dp to pixels dynamically based on screen density
//                    val sizeInPx = TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics
//                    ).toInt()
//
//                    // Resize the bitmap to 100dp x 100dp
//                    val resizedBitmap = bitmap?.let { it1 -> Bitmap.createScaledBitmap(it1, sizeInPx, sizeInPx, false) }
//
//                    // Make the bitmap round
//                    val roundedBitmap = resizedBitmap?.let { it1 -> getCircularBitmap(it1) }
//
//                    // Convert resized Bitmap to Drawable
//                    val drawable = BitmapDrawable(resources, roundedBitmap)
//
//                    marker.icon = drawable
//                }
        }

        // Set up marker click listener
        marker.setOnMarkerClickListener { _, _ ->
            // Show user details in a Toast
            onPostClick(user.id, user.userName)
            Toast.makeText(context, "User: ${user.userName}\nProfile: ${user.profilePictureUrl}", Toast.LENGTH_LONG).show()
            true
        }

        // Add the marker to the map
        overlays.add(marker)
        invalidate()  // Refresh the map to show markers
    }

    /**
     * Converts a square Bitmap into a circular one.
     */
    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val radius = bitmap.width / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        return output
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

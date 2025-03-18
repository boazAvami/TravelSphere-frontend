package com.syb.travelsphere.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
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

    // Method to add a marker on the map for a user
    private fun addUserMarker(lat: Double, lon: Double, user: User, onPostClick: (String, String) -> Unit) {
        val marker = Marker(this)

        // Convert 30dp to pixels for consistent sizing
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics
        ).toInt()

        // Set default icon, properly sized and circular
        val defaultDrawable = resources.getDrawable(R.drawable.profile_icon, null)
        val defaultBitmap = (defaultDrawable as BitmapDrawable).bitmap
        val resizedDefaultBitmap = Bitmap.createScaledBitmap(defaultBitmap, sizeInPx, sizeInPx, false)
        val roundedDefaultBitmap = getCircularBitmap(resizedDefaultBitmap)
        marker.icon = BitmapDrawable(resources, roundedDefaultBitmap)

        // Set the marker's position and title
        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = user.userName

        // Optionally, set the profile picture if provided
        if (!user.profilePictureUrl.isNullOrEmpty()) {
            Model.shared.getImageByUrl(
                imageUrl = user.profilePictureUrl!!
            ) { bitmap ->
                if (bitmap != null) {
                    // Resize the bitmap to match our standard size
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, sizeInPx, sizeInPx, false)

                    // Make the bitmap round
                    val roundedBitmap = getCircularBitmap(resizedBitmap)

                    // Convert resized Bitmap to Drawable
                    val drawable = BitmapDrawable(resources, roundedBitmap)

                    marker.icon = drawable
                    invalidate() // Refresh to show the updated icon
                }
            }
        }

        // Set up marker click listener
        marker.setOnMarkerClickListener { _, _ ->
            // Show user details in a Toast
            onPostClick(user.id, user.userName)
            Toast.makeText(context, "User: ${user.userName}", Toast.LENGTH_LONG).show()
            true
        }

        // Add the marker to the map
        overlays.add(marker)
        invalidate()  // Refresh the map to show markers
    }


    // Converts a square Bitmap into a circular one with a white background and border
    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val radius = bitmap.width / 2f

        // Draw white circle as background
        val backgroundPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }
        canvas.drawCircle(radius, radius, radius, backgroundPaint)

        // Draw the image
        val imagePaint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val imageRadius = radius * 0.92f  // 92% of the full radius to allow for border
        canvas.drawCircle(radius, radius, imageRadius, imagePaint)

        // Draw a border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.05f  // 5% of radius for border width
        }
        canvas.drawCircle(radius, radius, radius * 0.95f, borderPaint)

        return output
    }
}

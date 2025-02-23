package com.syb.travelsphere.components


import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.Toast
import com.syb.travelsphere.R
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.model.User
import com.squareup.picasso.Picasso
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


    // **Fetch posts from Firestore and display them on the map**
//    fun displayPosts() {
//        clearMap()
//
//        Model.shared.getAllPosts { posts ->
//            posts?.forEach { post ->
//                addPostMarker(
//                    post.location.latitude,
//                    post.location.longitude,
//                    post.id,
//                    post.description
//                )
//            }
//        }
//    }

    fun displayPosts(posts: List<Post>?) {
        clearMap()

        posts?.forEach { post ->
            val lat = post.location.latitude
            val lon = post.location.longitude

            addPostMarker(lat, lon, post.id, post.description)
        }

        invalidate() // Refresh the map after adding markers
    }

    // **Add a marker for a post**
    private fun addPostMarker(lat: Double, lon: Double, postId: String, description: String) {
        val marker = Marker(this).apply {
            icon = resources.getDrawable(R.drawable.location, null)
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
            setOnMarkerClickListener { _, _ ->
                Toast.makeText(context, "Post ID: $postId\nDescription: $description", Toast.LENGTH_LONG).show()
                true
            }
        }

        overlays.add(marker)
        invalidate() // Refresh the map
    }

    // **Fetch users from Firestore and display them on the map**
    fun displayUsers(users: List<User>?) {
        clearMap()

        users?.forEach { user ->
            user.location?.let { location ->
                addUserMarker(
                    location.latitude,
                    location.longitude,
                    user.userName,
                    user.profilePictureUrl
                )
            }
        }

        invalidate() // Refresh the map after adding markers
    }
//    fun displayUsers() {
//        clearMap()
//
//        Model.shared.getAllUsers { users ->
//            users?.forEach { user ->
//                user.location?.let { location ->
//                    addUserMarker(
//                        location.latitude,
//                        location.longitude,
//                        user.userName,
//                        user.profilePictureUrl
//                    )
//                }
//            }
//        }
//    }


    // **Add a marker for a user**
//    private fun addUserMarker(lat: Double, lon: Double, name: String, profilePictureUrl: String?) {
//        val marker = Marker(this).apply {
//            icon = resources.getDrawable(R.drawable.location, null)
//            position = GeoPoint(lat, lon)
//            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//            this.title = name
//            setOnMarkerClickListener { _, _ ->
//                Toast.makeText(context, "User: $name\nProfile: $profilePictureUrl", Toast.LENGTH_LONG).show()
//                true
//            }
//        }
//
//
//        // Load user profile picture if available
//        profilePictureUrl?.let { url ->
//            Picasso.get().load(url).into(marker.icon)
//        }
//
//
//        overlays.add(marker)
//        invalidate() // Refresh the map
//    }
//

    private fun addUserMarker(lat: Double, lon: Double, name: String, profilePictureUrl: String?) {
        val marker = Marker(this)
        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = name

        // Load profile picture if available, otherwise use a default icon
        if (!profilePictureUrl.isNullOrBlank()) {
            Picasso.get()
                .load(profilePictureUrl)
                .resize(100, 100) // Resize to fit marker
                .centerCrop()
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        bitmap?.let {
                            val drawable = BitmapDrawable(context.resources, it)
                            marker.icon = drawable
                            invalidate() // Refresh the map
                        }
                    }
                    override fun onBitmapFailed(e: Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
                        marker.icon = resources.getDrawable(R.drawable.location, null) // Default icon
                    }
                    override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                        marker.icon = resources.getDrawable(R.drawable.location, null) // Placeholder
                    }
                })
        } else {
            marker.icon = resources.getDrawable(R.drawable.location, null) // Default icon
        }

        // Handle marker click
        marker.setOnMarkerClickListener { _, _ ->
            Toast.makeText(context, "User: $name\nProfile: $profilePictureUrl", Toast.LENGTH_LONG).show()
            true
        }

        overlays.add(marker)
        invalidate() // Refresh the map
    }

    // **Center map on a specific location**
    fun centerMapOnLocation(lat: Double, lon: Double) {
        controller.setCenter(GeoPoint(lat, lon))
    }


    // **Clear all markers from the map**
    fun clearMap() {
        overlays.clear()
        invalidate()
    }
}






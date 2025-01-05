package com.syb.travelsphere.pages

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.services.TravelService
import com.syb.travelsphere.services.Post
import com.syb.travelsphere.R
import com.syb.travelsphere.components.MapComponent
import com.syb.travelsphere.ui.PostListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class AllPostsActivity : AppCompatActivity() {

    private lateinit var travelService: TravelService
    private lateinit var postListRecyclerView: RecyclerView
    private lateinit var mapComponent: MapComponent

    private var posts = listOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_posts)

        travelService = TravelService()

        // Initialize the RecyclerView
        postListRecyclerView = findViewById(R.id.postListRecyclerView)
        postListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch posts and set up RecyclerView
        fetchPostsAndSetUpScreen()

        // Set up map view
        mapComponent = findViewById(R.id.mapComponent)
    }

    private fun fetchPostsAndSetUpScreen() {
        lifecycleScope.launch {
            try {
                // Fetch the posts
                val fetchedPosts = withContext(Dispatchers.IO) {
                    travelService.getAllPosts()  // Get posts for the logged-in user
                }

                if (fetchedPosts != null) {
                    posts = fetchedPosts

                    // Update RecyclerView with the new data
                    postListRecyclerView.adapter = PostListAdapter(posts) { post ->
                        // When a post is clicked, center the map on its location
                        centerMapOnPost(post)
                    }

                    mapComponent.displayPosts(posts)
                }

            } catch (e: Exception) {
                Toast.makeText(this@AllPostsActivity, "Error fetching posts: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("Error", "Error fetching posts: ${e.message}")
            }
        }
    }

    // Method to center the map on the selected post's location
    private fun centerMapOnPost(post: Post) {
        val geotag = post.geotag
        val lat = geotag.coordinates[1]
        val lon = geotag.coordinates[0]
        mapComponent.centerMapOnLocation(lat, lon)
    }
}

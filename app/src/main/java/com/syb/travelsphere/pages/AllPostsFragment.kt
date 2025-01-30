package com.syb.travelsphere.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.services.TravelService
import com.syb.travelsphere.services.Post
import com.syb.travelsphere.R
import com.syb.travelsphere.components.MapComponent
import com.syb.travelsphere.databinding.FragmentAllPostsBinding
import com.syb.travelsphere.ui.PostListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllPostsFragment : Fragment() {

    private var binding: FragmentAllPostsBinding? = null

    private lateinit var travelService: TravelService

    private var posts = listOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        travelService = TravelService()

        binding?.postListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        fetchPostsAndSetUpScreen()
    }

    private fun fetchPostsAndSetUpScreen() {
        lifecycleScope.launch {
            try {
                val fetchedPosts = withContext(Dispatchers.IO) {
                    travelService.getAllPosts()
                }

                if (fetchedPosts != null) {
                    posts = fetchedPosts
                    binding?.postListRecyclerView?.adapter = PostListAdapter(posts) { post ->
                        centerMapOnPost(post)
                    }
                    binding?.mapComponent?.displayPosts(posts)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching posts: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("Error", "Error fetching posts: ${e.message}")
            }
        }
    }

    private fun centerMapOnPost(post: Post) {
        val geotag = post.geotag
        val lat = geotag.coordinates[1]
        val lon = geotag.coordinates[0]
        binding?.mapComponent?.centerMapOnLocation(lat, lon)
    }
}

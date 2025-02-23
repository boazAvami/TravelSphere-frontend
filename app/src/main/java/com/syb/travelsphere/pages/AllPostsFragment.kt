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
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentAllPostsBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.ui.PostListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllPostsFragment : Fragment() {

    private var binding: FragmentAllPostsBinding? = null
    private var posts = listOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.postListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

//        fetchPostsAndSetUpScreen()
    }

    private fun fetchPostsAndSetUpScreen() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Model.shared.getAllPosts { fetchedPosts ->
                        if (fetchedPosts != null && fetchedPosts.isNotEmpty()) {
                            posts = fetchedPosts
                            binding?.postListRecyclerView?.adapter = PostListAdapter(posts) { post ->
                                centerMapOnPost(post)
                            }
                        } else {
                            Log.e(TAG, "No posts found or failed to fetch posts.")
                            Toast.makeText(requireContext(), "No posts available.", Toast.LENGTH_SHORT).show()
                        }
                        binding?.mapComponent?.displayPosts(posts)
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching posts: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Error fetching posts: ${e.message}")
            }
        }
    }

    private fun centerMapOnPost(post: Post) {
        val lat = post.location.latitude
        val lon = post.location.longitude
        binding?.mapComponent?.centerMapOnLocation(lat, lon)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        private const val TAG = "AllPostsFragment"
    }
}

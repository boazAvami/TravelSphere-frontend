package com.syb.travelsphere.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.syb.travelsphere.databinding.FragmentAllPostsBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.ui.PostListAdapter

class AllPostsFragment : Fragment() {

    private var binding: FragmentAllPostsBinding? = null

    private lateinit var postListAdapter: PostListAdapter
//    private lateinit var travelService: TravelService

    private var posts = listOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        binding?.postListRecyclerView?.setHasFixedSize(true)
        setupRecyclerView()
        observePosts()

        binding?.swipeToRefresh?.setOnRefreshListener {
            onResume()
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        travelService = TravelService()

//        binding?.postListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

//        fetchPostsAndSetUpScreen()
    }

    override fun onResume() {
        super.onResume()
        getAllPosts()
    }

    private fun setupRecyclerView() {
        binding?.postListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        postListAdapter = PostListAdapter(mutableListOf()) { post -> centerMapOnPost(post) }
        binding?.postListRecyclerView?.adapter = postListAdapter
    }

    private fun observePosts() {
        Model.shared.posts.observe(viewLifecycleOwner) { posts ->
            Log.d(TAG, "UI updated: Received ${posts.size} posts")

            if (posts.isNotEmpty()) {
                postListAdapter.updatePosts(posts)
                binding?.mapComponent?.displayPosts(posts)

                Log.d(TAG, "UI Updated: Showing ${posts.size} posts")
            } else {
                Log.d(TAG, "observePosts: No new posts available yet")
            }
        }
    }

    private fun getAllPosts() {
        binding?.progressBar?.visibility = View.VISIBLE

        Model.shared.refreshAllUsers {
            Model.shared.refreshAllPosts {
                binding?.progressBar?.visibility = View.GONE // Hide progress bar when done
                binding?.swipeToRefresh?.isRefreshing = false // Stop swipe refresh
            }
//            binding?.progressBar?.visibility = View.GONE
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
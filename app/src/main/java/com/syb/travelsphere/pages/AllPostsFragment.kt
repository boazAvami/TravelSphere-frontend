package com.syb.travelsphere.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.databinding.FragmentAllPostsBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.ui.PostListAdapter

class AllPostsFragment : Fragment() {

    private var binding: FragmentAllPostsBinding? = null
    private lateinit var postListAdapter: PostListAdapter
    private val viewModel: AllPostsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        viewModel.postOwnerUsers.observe(viewLifecycleOwner) { usersMap ->
            viewModel.posts.value?.let { posts ->
                updateUI(posts, usersMap)
            }
        }

        // Pass the Fragment's NavController to the MapComponent
        val navController = findNavController()
        binding?.mapComponent?.setNavController(navController)

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            Log.d(TAG, "UI updated: Received ${posts.size} posts")

            viewModel.fetchPostOwnerUsers(posts) {
                viewModel.postOwnerUsers.value?.let { usersMap ->
                    updateUI(posts, usersMap)
                }
            }

            Log.d(TAG, "UI Updated: Showing ${posts.size} posts")
            postListAdapter?.notifyDataSetChanged()
//            }
        }

        binding?.swipeToRefresh?.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPosts()
    }


    private fun centerMapOnPost(point: GeoPoint) {
        val lat = point.latitude
        val lon = point.longitude
        binding?.mapComponent?.centerMapOnLocation(lat, lon)
    }

    private fun setupRecyclerView() {
        binding?.postListRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            // Initialize adapter with empty data
            postListAdapter = PostListAdapter(emptyList(), emptyMap()) { post ->
                centerMapOnPost(post.location)
            }
            adapter = postListAdapter
        }
    }

    private fun updateUI(posts: List<Post>, usersMap: Map<String, String>) {
        postListAdapter.update(posts, usersMap)
        binding?.mapComponent?.displayPosts(posts) { postId, ownerId ->
            val action = AllPostsFragmentDirections.actionGlobalSinglePostFragment(postId).apply {
                this.ownerName = usersMap[ownerId] ?: "User"
            }
            findNavController().navigate(action)
        }

        // Center the map on the first post if available
        if (posts.isNotEmpty()) {
            posts[0].location?.let { location ->
                binding?.mapComponent?.centerMapOnLocation(location.latitude, location.longitude)
            }
        }

        Log.d(TAG, "UI Updated: Showing ${posts.size} posts")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        private const val TAG = "AllPostsFragment"
    }
}
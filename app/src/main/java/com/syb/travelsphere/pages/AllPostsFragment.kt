package com.syb.travelsphere.pages

import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.syb.travelsphere.databinding.FragmentAllPostsBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.ui.PostListAdapter

class AllPostsFragment : Fragment() {

    private var binding: FragmentAllPostsBinding? = null

    private lateinit var postListAdapter: PostListAdapter

    private var posts: LiveData<List<Post>> = Model.shared.posts

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        posts.observe(viewLifecycleOwner) { posts ->
            Log.d(TAG, "UI updated: Received ${posts.size} posts")

            postListAdapter.update(posts)
            binding?.mapComponent?.displayPosts(posts)
            Log.d(TAG, "UI Updated: Showing ${posts.size} posts")
            postListAdapter?.notifyDataSetChanged()
        }

        binding?.swipeToRefresh?.setOnRefreshListener {
            getAllPosts()
        }

        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
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
        binding?.postListRecyclerView?.setHasFixedSize(true)
        binding?.postListRecyclerView?.layoutManager = LinearLayoutManager(context)
        postListAdapter = PostListAdapter(posts.value) { post -> centerMapOnPost(post) }
        binding?.postListRecyclerView?.adapter = postListAdapter
    }

    private fun getAllPosts() {
        Model.shared.refreshAllPosts()
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
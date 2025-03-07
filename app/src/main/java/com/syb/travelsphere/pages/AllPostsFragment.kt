package com.syb.travelsphere.pages

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.databinding.FragmentAllPostsBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.ui.PostListAdapter

class AllPostsFragment : Fragment() {

    private var binding: FragmentAllPostsBinding? = null
    private lateinit var postListAdapter: PostListAdapter
    private val viewModel: AllPostsViewModel by viewModels() // ViewModel instance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAllPostsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            Log.d(TAG, "UI updated: Received ${posts.size} posts")

            postListAdapter.update(posts)
            binding?.mapComponent?.displayPosts(posts)
            Log.d(TAG, "UI Updated: Showing ${posts.size} posts")
            postListAdapter?.notifyDataSetChanged()
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

    private fun setupRecyclerView() {
        binding?.postListRecyclerView?.setHasFixedSize(true)
        binding?.postListRecyclerView?.layoutManager = LinearLayoutManager(context)
        postListAdapter = PostListAdapter(viewModel.posts.value) { post -> centerMapOnPost(post.location) }
        binding?.postListRecyclerView?.adapter = postListAdapter
    }

    private fun centerMapOnPost(point: GeoPoint) {
        val lat = point.latitude
        val lon = point.longitude
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
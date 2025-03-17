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
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentDisplayUserBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User
import com.syb.travelsphere.ui.PostListAdapter


class DisplayUserFragment : Fragment() {
    private var binding: FragmentDisplayUserBinding? = null
    private val viewModel: ProfileViewModel by viewModels() // ViewModel instance

    private lateinit var postListAdapter: PostListAdapter
    private lateinit var userObject: User
    private var userId: String? = null  // Store userId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDisplayUserBinding.inflate(inflater, container, false)

        userId = arguments?.let {
            DisplayUserFragmentArgs.fromBundle(it).userId
        }

        if (userId != null) {
            Model.shared.getUserById(userId!!) { user ->
                if (user != null) {
                    userObject = user
                    fetchUserData()

                    if (userObject != null) {
                        setupRecyclerView(mapOf(userObject.id to userObject.userName))
                        Log.d(TAG, "onCreateView: ${userObject.id} ${userObject.userName}")
                    }
                }
            }
        }

        // Pass the Fragment's NavController to the MapComponent
        val navController = findNavController()
        binding?.mapComponent?.setNavController(navController)


        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            Log.d(TAG, "UI updated: Received ${posts.size} posts")

                userId?.let {
                    Model.shared.getUserById(it) { user ->
                        if (user != null) {
                            userObject = user
                            postListAdapter.update(posts, mapOf(user.id to user.userName))

                            binding?.mapComponent?.displayPosts(posts) { postId ->
                                val action = DisplayUserFragmentDirections.actionGlobalSinglePostFragment(postId)
                                findNavController().navigate(action)
                            }

                            Log.d(TAG, "UI Updated: Showing ${posts.size} posts")
                            postListAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            // Update adapter with posts & username


        }

        binding?.swipeToRefresh?.setOnRefreshListener {
            fetchUserData()

            viewModel.refreshUserPosts(userObject.id)
        }

        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        return binding?.root
    }

    private fun fetchUserData() {
        binding?.userPhone?.text = userObject.phoneNumber
        binding?.userName?.text = userObject.userName
        Log.d(TAG, "onCreateView: Image URL loading photo $userObject?.profilePictureUrl")
        if (!userObject.profilePictureUrl.isNullOrEmpty()) {
            try {
                val userProfilePictureUrl = userObject.profilePictureUrl
                if (userProfilePictureUrl != null) {
                    Model.shared.getImageByUrl(userProfilePictureUrl) { bitmap ->
                        binding?.userProfilePicture?.setImageBitmap(bitmap)
                        Log.d(TAG, "fetchUserData: bitmap $bitmap")
                    }
                } else {
                    // If decoding fails, set a default image
                    binding?.userProfilePicture?.setImageResource(R.drawable.default_user)
                }
            } catch (e: Exception) {
                // If decoding fails, set a default image
                binding?.userProfilePicture?.setImageResource(R.drawable.default_user)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        userId?.let {
            Model.shared.getUserById(it) { user ->
                if (user != null) {
                    userObject = user
                    viewModel.refreshUserPosts(userObject.id)
                }
            }
        }
    }

    private fun setupRecyclerView(userMap: Map<String, String>) {
        binding?.postListRecyclerView?.setHasFixedSize(true)
        binding?.postListRecyclerView?.layoutManager = LinearLayoutManager(context)
        postListAdapter = PostListAdapter(viewModel.userPosts.value, userMap) {
                post -> centerMapOnPost(post.location)
        }
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
        private const val TAG = "DisplayUserFragment"
    }
}

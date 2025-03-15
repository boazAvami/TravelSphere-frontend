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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.R
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.FragmentProfileBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.User
import com.syb.travelsphere.ui.PostListAdapter

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private val viewModel: ProfileViewModel by viewModels() // ViewModel instance

    private lateinit var authManager: AuthManager
    private lateinit var postListAdapter: PostListAdapter
    private lateinit var userObject: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        authManager = AuthManager()  // Initialize AuthManager
        val currentUser = authManager.getCurrentUser()

        // Pass the Fragment's NavController to the MapComponent
        val navController = findNavController()
        binding?.mapComponent?.setNavController(navController)

        fetchUserData(currentUser)

        if (currentUser != null) {
            Model.shared.getUserById(currentUser.uid) { user ->
                if (user != null) {
                    userObject = user
                    setupRecyclerView(mapOf(currentUser.uid to userObject.id))
                    Log.d(TAG, "onCreateView: ${currentUser.uid} ${userObject.userName}")
                }
            }
        }

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            Log.d(TAG, "UI updated: Received ${posts.size?:0} posts")

            if (currentUser != null) {
                Model.shared.getUserById(currentUser.uid) { user ->
                    if (user != null) {
                        userObject = user

                        postListAdapter.update(posts, mapOf(currentUser.uid to user.userName))

                        binding?.mapComponent?.displayPosts(posts) { postId ->
                            val action = ProfileFragmentDirections.actionGlobalSinglePostFragment(postId)
                            findNavController().navigate(action)
                        }

                        Log.d(TAG, "UI Updated: Showing ${posts.size} posts")
                        postListAdapter?.notifyDataSetChanged()
                    }
                }// Update adapter with posts & username

                }
            }

        binding?.swipeToRefresh?.setOnRefreshListener {
            fetchUserData(currentUser)

            viewModel.refreshUserPosts(userObject.id)
        }

        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        return binding?.root
    }

    private fun fetchUserData(currentUser: FirebaseUser?) {
        // Get the current user and display details
        if (currentUser != null) {
            Model.shared.getUserById(currentUser.uid) { user ->

                binding?.userEmail?.text = currentUser.email
                binding?.userPhone?.text = user?.phoneNumber
                binding?.userName?.text = user?.userName
                Log.d(TAG, "onCreateView: Image URL loading photo $user?.profilePictureUrl")
                if (!user?.profilePictureUrl.isNullOrEmpty()) {
                    try {
                        val userProfilePictureUrl = user?.profilePictureUrl
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
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            viewModel.refreshUserPosts(currentUser.uid)
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
        private const val TAG = "ProfileFragment"
    }
}
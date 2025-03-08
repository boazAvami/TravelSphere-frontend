import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.FragmentProfileBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.services.TravelService
import com.syb.travelsphere.ui.PostListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private lateinit var travelService: TravelService
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
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
        fetchUserData()
        fetchPostsAndSetUpScreen()
    }

    private fun fetchUserData() {
        authManager = AuthManager()  // Initialize AuthManager
        // ✅ Get the current user and display details
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            Model.shared.getUserById(currentUser.uid) {user ->

                binding?.userEmail?.setText(currentUser.email)
                binding?.userPhone?.setText(user?.phoneNumber)
                binding?.userName?.setText(user?.userName)
                user?.profilePictureUrl?.let { it1 ->
                    Model.shared.getImageByUrl(it1) { image ->
                        run {
                            binding?.userProfilePicture?.setImageBitmap(image)
                        }
                    }
                }
             }
        }
    }

    private fun fetchPostsAndSetUpScreen() {
        lifecycleScope.launch {
            try {
                 withContext(Dispatchers.IO) {
                    authManager.getCurrentUser()?.let {
                        Model.shared.getPostsByUserId(it.uid) { posts ->
                            binding?.postListRecyclerView?.adapter = PostListAdapter(posts.orEmpty().toMutableList(), emptyMap()) { post ->
                                centerMapOnPost(post)
                            }
                            binding?.mapComponent?.displayPosts(posts, EditPostFragment(), EditPostFragment::class.java)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error fetching posts: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("Error", "Error fetching posts: ${e.message}")
            }
        }
    }

    private fun centerMapOnPost(post: Post) {
        val lat = post.location.latitude
        val lon = post.location.longitude
        binding?.mapComponent?.centerMapOnLocation(lat, lon)
    }
}

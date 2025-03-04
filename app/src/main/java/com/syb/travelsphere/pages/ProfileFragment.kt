import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.syb.travelsphere.R
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.FragmentProfileBinding
import com.syb.travelsphere.model.FirebaseModel
import com.syb.travelsphere.services.TravelService
import com.syb.travelsphere.services.User
import com.syb.travelsphere.ui.PostListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private lateinit var travelService: TravelService
    private var posts = listOf<com.syb.travelsphere.services.Post>()
    private lateinit var user: User;
    private lateinit var authManager: AuthManager
    private val firebaseModel = FirebaseModel()

    //todo: change to
    //  private val model = Model.shared.getUser()


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
            val userInfo = firebaseModel.getUserById(currentUser.uid, { user ->
                if (user != null) {
                    binding?.userEmail?.setText(currentUser.email)
                    binding?.userPhone?.setText(user.phoneNumber)
                    binding?.userName?.setText(user.userName)

                    // Load the profile picture from base64
                    val base64Image = user.profilePictureUrl
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val bitmap = decodeBase64Image(base64Image)
                            binding?.userProfilePicture?.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            // If decoding fails, set a default image
                            binding?.userProfilePicture?.setImageResource(R.drawable.default_user)
                        }
                    }
                }
            })
        }

    }

    // Helper function to decode base64 string to Bitmap
    private fun decodeBase64Image(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }


    private fun fetchPostsAndSetUpScreen() {
        lifecycleScope.launch {
            try {
                val fetchedPosts = withContext(Dispatchers.IO) {
                    //todo: getAllPostsByUserId()
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
                Toast.makeText(
                    requireContext(),
                    "Error fetching posts: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("Error", "Error fetching posts: ${e.message}")
            }
        }
    }

    private fun centerMapOnPost(post: com.syb.travelsphere.services.Post) {
        val geotag = post.geotag
        val lat = geotag.coordinates[1]
        val lon = geotag.coordinates[0]
        binding?.mapComponent?.centerMapOnLocation(lat, lon)
    }

}

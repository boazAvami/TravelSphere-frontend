import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentSinglePostBinding
import com.syb.travelsphere.model.FirebaseModel
import com.syb.travelsphere.model.Post

class ViewPostFragment : Fragment() {
    private var binding: FragmentSinglePostBinding? = null
    private lateinit var post: Post // Your post model
    private var fireBase = FirebaseModel();

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSinglePostBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load post data (Example: getting it from arguments or ViewModel)
        //todo: get data from the constructor / from model
        post;
        var userName =  fireBase.getUserById(post.ownerId) { user ->
            binding?.userNameText?.text = user?.userName
            // Load the profile picture from base64
            val base64Image = user?.profilePictureUrl
            if (!base64Image.isNullOrEmpty()) {
                try {
                    val bitmap = decodeBase64Image(base64Image)
                    binding?.userProfilePicture?.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // If decoding fails, set a default image
                    binding?.userProfilePicture?.setImageResource(R.drawable.default_user)
                }
            } else {
                // Set default image if no profile picture is provided
                binding?.userProfilePicture?.setImageResource(R.drawable.default_user)
            }
        }

        // Display the post data
        // Load the profile picture from base64
        //todo: make it work with Url
        val base64Image = post.imageUrl
        if (!base64Image.isNullOrEmpty()) {
            try {
                val bitmap = decodeBase64Image(base64Image)
                binding?.photoViewPager?.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // If decoding fails, set a default image
                binding?.photoViewPager?.setImageResource(R.drawable.default_user)
            }
        } else {
            // Set default image if no profile picture is provided
            binding?.photoViewPager?.setImageResource(R.drawable.default_user)
        }
        //todo: take location name
      // binding?.locationNameText?.text = post.locationName
        binding?.descriptionText?.text = post.description
        binding?.timestampText?.text = "Created on: ${post.creationTime}"

        // Load user profile picture
    //    Glide.with(this).load(post.userProfilePic).into(binding?.userProfilePicture!!)

        // Hide edit button in read-only mode
        binding?.editButton?.visibility = View.GONE
    }


    // Helper function to decode base64 string to Bitmap
    private fun decodeBase64Image(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}

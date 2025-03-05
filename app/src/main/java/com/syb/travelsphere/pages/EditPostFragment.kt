import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.syb.travelsphere.R
import com.syb.travelsphere.databinding.FragmentSinglePostBinding
import com.syb.travelsphere.model.FirebaseModel
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class EditPostFragment : Fragment() {
    private var binding: FragmentSinglePostBinding? = null
    private lateinit var post: Post
    private var fireBase = FirebaseModel();

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSinglePostBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load post data
        //todo: get it from somewhere
        // maybe:         Model.shared.getPostById(postId)
        var userName = fireBase.getUserById(post.ownerId) { user ->
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

//        binding?.locationNameText?.text = post.locationName
        binding?.descriptionText?.text = post.description
        binding?.timestampText?.text = "Created on: ${post.creationTime}"

        // Show edit button in edit mode
        binding?.editButton?.visibility = View.VISIBLE

        // Handle Edit Button Click
        binding?.editButton?.setOnClickListener {
            val newDescription = binding?.descriptionText?.text.toString()
            var newPost = post;
            //todo: put the new data
            //newPost.description = newDescription;
            updatePostDescription(newPost)
        }
    }

    private fun updatePostDescription(newPost: Post) {
        //take the wright model to update
        //todo: is that correct??
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(newPost.id)

        Model.shared.editPost(post) {}
        postRef.update("description", newPost.description)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Update failed!", Toast.LENGTH_SHORT).show()
            }
    }

    // Helper function to decode base64 string to Bitmap
    private fun decodeBase64Image(base64String: String): Bitmap {
        val decodedString = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}

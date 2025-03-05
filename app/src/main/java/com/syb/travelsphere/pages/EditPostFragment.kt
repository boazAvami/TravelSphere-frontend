import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.syb.travelsphere.databinding.FragmentSinglePostBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class EditPostFragment : Fragment() {
    private var binding: FragmentSinglePostBinding? = null
    private lateinit var post: Post

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSinglePostBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)

        //todo: get data from the constructor / from model
        post;
        Model.shared.getUserById(post.ownerId) { user ->
            binding?.userNameText?.text = user.value?.userName
            user.value?.profilePictureUrl?.let { it1 ->
                Model.shared.getImageByUrl(it1) { image ->
                    run {
                        binding?.userProfilePicture?.setImageBitmap(image)
                    }
                }
            }
        }

        binding?.locationNameText?.text = post.locationName
        binding?.descriptionText?.text = post.description
        binding?.timestampText?.text = "Created on: ${post.creationTime}"
        post.photos[0].let { it1 ->
            Model.shared.getImageByUrl(it1) { image ->
                run {
                    binding?.photoViewPager?.setImageBitmap(image)
                }
            }
        }

        // Show edit button in edit mode
        binding?.editButton?.visibility = View.VISIBLE

        // Handle Edit Button Click
        binding?.editButton?.setOnClickListener {
            val newDescription = binding?.descriptionText?.text.toString()
            val newLocationName = binding?.locationNameText?.text.toString().trim()

            //todo: put the new data
            var newPost = post.copy(
                description = newDescription,
                locationName = newLocationName
            )
            //todo: is that correct??
            Model.shared.editPost(newPost) {}
        }
    }
}

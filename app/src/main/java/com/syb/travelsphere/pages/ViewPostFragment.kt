import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.syb.travelsphere.databinding.FragmentSinglePostBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post

class ViewPostFragment : Fragment() {
    private var binding: FragmentSinglePostBinding? = null
    private lateinit var post: Post // Your post model

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSinglePostBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        // Hide edit button in read-only mode
        binding?.editButton?.visibility = View.GONE
    }
}

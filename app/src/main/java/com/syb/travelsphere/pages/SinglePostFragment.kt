package com.syb.travelsphere.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.syb.travelsphere.databinding.FragmentSinglePostBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.utils.TimeUtil.formatTimestamp

class SinglePostFragment : Fragment() {
    private var binding: FragmentSinglePostBinding? = null
    private var postId: String? = null  // Store postId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSinglePostBinding.inflate(inflater, container, false)
        // Get postId from arguments
        postId =  arguments?.let {
            SinglePostFragmentArgs.fromBundle(it).postId
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TAG", "onViewCreated: postID")

        if (postId != null) {
            Model.shared.getPostById(postId!!) { post ->
                Log.d("TAG", "onViewCreated: $post")
                post?.ownerId?.let {
                    Model.shared.getUserById(it) { user ->
                        Log.d("TAG", "onViewCreated: $user")
                        binding?.userNameText?.text = user?.userName
                        user?.profilePictureUrl?.let { it1 ->
                            Model.shared.getImageByUrl(it1) { image ->
                                binding?.userProfilePicture?.setImageBitmap(image)
                            }
                        }
                    }
                }

                binding?.locationNameText?.text = "Took place in: ${post?.locationName}"
                binding?.descriptionText?.text = "Description: ${post?.description}"
                binding?.timestampText?.text = "Created at: ${formatTimestamp(post!!.creationTime)}"
                post.photos[0].let { it1 ->
                    Model.shared.getImageByUrl(it1) { image ->
                        run {
                            binding?.photoViewPager?.setImageBitmap(image)
                        }
                    }
                }
            }
        }
    }
}
package com.syb.travelsphere.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.syb.travelsphere.databinding.FragmentEditPostBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.utils.InputValidator
import com.syb.travelsphere.utils.TimeUtil.formatTimestamp

class EditPostFragment : Fragment() {
    private var binding: FragmentEditPostBinding? = null
    private lateinit var post: Post
    private var postId: String? = null // Store postId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPostBinding.inflate(inflater, container, false)

        postId = arguments?.let {
            SinglePostFragmentArgs.fromBundle(it).postId
        }

        getPost()

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)

        if (postId != null) {
            fillPostData()
        }

        // Handle Edit Button Click
        binding?.editButton?.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener  // Stop execution if validation fails
            editPost()
            Navigation.findNavController(view).popBackStack()
        }
        binding?.deleteButton?.setOnClickListener {
            deletePost()
            Navigation.findNavController(view).popBackStack()
        }
    }

    private fun editPost() {
        val newDescription = binding?.descriptionText?.text.toString()
        val newLocationName = binding?.locationNameText?.text.toString().trim()

        val newPost = post.copy(
            description = newDescription,
            locationName = newLocationName
        )
        //todo: is that correct??
        Log.d("TAG", "onViewCreated: $newDescription $newLocationName ")
        Model.shared.editPost(newPost) {
        }
    }

    private fun deletePost() {
        Model.shared.deletePost(post) {

        }
    }

    private fun getPost() {
        Log.d(TAG, "getPost: test")
        postId?.let {
            Model.shared.getPostById(it) {
                Log.d(TAG, "getPost: $it")
                if (it != null) {
                    this.post = it
                }
            }
        }
    }

    private fun fillPostData(){
        Model.shared.getPostById(postId!!) { post ->
            post?.ownerId?.let {
                Model.shared.getUserById(it) { user ->
                    binding?.userNameText?.text = user?.userName
                    user?.profilePictureUrl?.let { it1 ->
                        Model.shared.getImageByUrl(it1) { image ->
                            run {
                                binding?.userProfilePicture?.setImageBitmap(image)
                            }
                        }
                    }
                }
            }

            binding?.locationNameText?.setText(post?.locationName)
            binding?.descriptionText?.setText(post?.description)
            binding?.timestampText?.text = "Created on: ${post?.creationTime?.let {formatTimestamp(it)}}"
            post?.photos?.get(0).let { it1 ->
                if (it1 != null) {
                    Model.shared.getImageByUrl(it1) { image ->
                        run {
                            binding?.photoViewPager?.setImageBitmap(image)
                        }
                    }
                }
            }
        }
    }

    fun validateInputs(): Boolean {
        val description = binding?.descriptionText?.text.toString().trim()
        val phone = binding?.locationNameText?.text.toString().trim()

        var isValid = true

        if (!InputValidator.validateDescription(description, binding?.descriptionTextTextInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validateRequiredTextField(phone, binding?.locationNameTextTextInputLayout)) {
            isValid = false
        }

        return isValid
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val TAG = "EditPostFragment"
    }
}
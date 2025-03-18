package com.syb.travelsphere.pages

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.ArrayAdapter
import com.syb.travelsphere.components.PhotosGridAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.osmdroid.util.GeoPoint as OSGeoPoint
import com.syb.travelsphere.databinding.FragmentAddPostBinding
import com.syb.travelsphere.utils.ImagePickerUtil
import com.syb.travelsphere.utils.InputValidator

class AddPostFragment : Fragment() {

    private var binding: FragmentAddPostBinding? = null
    private lateinit var viewModel: AddPostViewModel
    private lateinit var imagePicker: ImagePickerUtil
    private lateinit var photosGridAdapter: PhotosGridAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupUI()
    }

    private fun setupObservers() {
        viewModel.selectedImages.observe(viewLifecycleOwner) { images ->
            photosGridAdapter.notifyDataSetChanged()
        }

        viewModel.locationSuggestions.observe(viewLifecycleOwner) { suggestions ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
            binding?.searchLocationTextView?.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

        viewModel.currentGeoPoint.observe(viewLifecycleOwner) { geoPoint ->
            geoPoint?.let {
                binding?.mapView?.controller?.setCenter(OSGeoPoint(it.latitude, it.longitude))
                binding?.mapView?.controller?.setZoom(15.0)
            }
        }

        viewModel.postCreated.observe(viewLifecycleOwner) { isCreated ->
            if (isCreated) {
                Toast.makeText(requireContext(), "Post shared successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(AddPostFragmentDirections.actionAddPostFragmentToAllPostsFragment())
            }
        }
    }

    private fun setupUI() {
        setupImagePicker()
        setupPhotoRecyclerView()
        setupListeners()
    }

    private fun setupImagePicker() {
        imagePicker = ImagePickerUtil(this) { bitmap ->
            bitmap?.let { viewModel.addImage(it) }
        }
    }

    private fun setupPhotoRecyclerView() {
        photosGridAdapter = PhotosGridAdapter(viewModel.selectedImages.value ?: mutableListOf()) { position ->
            viewModel.removeImage(position)
        }
        binding?.photosGridRecyclerView?.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = photosGridAdapter
        }
    }

    private fun setupListeners() {
        binding?.addPhotosButton?.setOnClickListener { imagePicker.showImagePickerDialog() }

        binding?.sharePostButton?.setOnClickListener {
            val description = binding?.descriptionEditText?.text.toString().trim()
            val locationName = binding?.locationNameEditText?.text.toString().trim()

            if (validateInputs(description, locationName)) {
                viewModel.createPost(description, locationName)
            }
        }

        binding?.searchLocationTextView?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank() && s.length > 2) {
                    viewModel.fetchAddressSuggestions(s.toString())
                }
            }
        })

        binding?.searchLocationTextView?.setOnItemClickListener { _, _, position, _ ->
            val selectedAddress = binding?.searchLocationTextView?.adapter?.getItem(position) as String
            binding?.searchLocationTextView?.setText(selectedAddress)
            viewModel.fetchGeoLocation(selectedAddress)
        }
    }

    private fun validateInputs(description: String, locationName: String): Boolean {
        var isValid = true

        if (!InputValidator.validateRequiredTextField(locationName, binding?.locationInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validateRequiredTextField(description, binding?.descriptionInputLayout)) {
            isValid = false
        }
        if (viewModel.selectedImages.value.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one photo", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
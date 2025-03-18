package com.syb.travelsphere.pages

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.fragment.app.viewModels
import com.syb.travelsphere.components.PhotosGridAdapter
import org.osmdroid.config.Configuration
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import org.osmdroid.util.GeoPoint as OSGeoPoint
import com.google.firebase.firestore.GeoPoint
import com.syb.travelsphere.auth.AuthManager
import com.syb.travelsphere.databinding.FragmentAddPostBinding
import com.syb.travelsphere.model.Model
import com.syb.travelsphere.model.Post
import com.syb.travelsphere.utils.GeoUtils
import com.syb.travelsphere.utils.ImagePickerUtil
import com.syb.travelsphere.utils.InputValidator
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

class AddPostFragment : Fragment() {

    private var binding: FragmentAddPostBinding? = null
    private val viewModel: AddPostViewModel by viewModels()
    private lateinit var authManager: AuthManager

    private lateinit var imagePicker: ImagePickerUtil
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var photosGridAdapter: PhotosGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(layoutInflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager()

        setupObservers()
        setupMap()
        setupImagePicker()
        setupPhotoRecyclerView()
        setupListeners()

        // Get current location immediately
        GeoUtils.getCurrentLocation(requireContext()) { userLocation ->
            if (userLocation != null) {
                Log.d(TAG, "Initial Location: Lat=${userLocation.latitude}, Lon=${userLocation.longitude}")
                binding?.mapView?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
            }
        }

        // Start observing location changes
        GeoUtils.observeLocationChanges(requireContext()) { userLocation ->
            Log.d(TAG, "New Location: Lat=${userLocation.latitude}, Lon=${userLocation.longitude}")
            binding?.mapView?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
        }
    }

    private fun setupObservers() {
        // Observe location-related LiveData from the ViewModel
        viewModel.locationSuggestions.observe(viewLifecycleOwner) { suggestions ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions ?: emptyList())
            binding?.searchLocationTextView?.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }

        viewModel.currentGeoPoint.observe(viewLifecycleOwner) { geoPoint ->
            geoPoint?.let {
                binding?.mapView?.controller?.setCenter(OSGeoPoint(it.latitude, it.longitude))
                binding?.mapView?.controller?.setZoom(15.0)
                binding?.selectedLocationTextView?.text = "Selected Location: (${it.latitude}, ${it.longitude})"
            }
        }
    }

    private fun setupPhotoRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 4) // 4 columns in the grid
        binding?.photosGridRecyclerView?.layoutManager = layoutManager

        // initialize adapter with the viewModel's selectedImages list
        photosGridAdapter = PhotosGridAdapter(viewModel.selectedImages) { position ->
            viewModel.removeImage(position)
            photosGridAdapter.notifyDataSetChanged()
        }

        binding?.photosGridRecyclerView?.adapter = photosGridAdapter
    }

    override fun onResume() {
        super.onResume()
        setupMap()

        // Start continuous location updates
        GeoUtils.observeLocationChanges(requireContext()) { userLocation ->
            Log.d(TAG, "Updated Location: Lat=${userLocation.latitude}, Lon=${userLocation.longitude}")
            binding?.mapView?.centerMapOnLocation(userLocation.latitude, userLocation.longitude)
        }
    }

    private fun centerMapOnUser(point: GeoPoint) {
        val lat = point.latitude
        val lon = point.longitude
        binding?.mapView?.centerMapOnLocation(lat, lon)
    }

    private fun setupMap() {
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
        binding?.mapView?.setMultiTouchControls(true)

        // Get user location if available
        GeoUtils.getCurrentLocation(requireContext()) { userGeoPoint ->
            val osGeoPoint = userGeoPoint?.let { OSGeoPoint(it.latitude, it.longitude) }
            binding?.mapView?.controller?.setCenter(osGeoPoint)
            binding?.mapView?.controller?.setZoom(15.0)
        }

        // Add an overlay to capture map clicks
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: OSGeoPoint): Boolean {
                val geoPoint = GeoPoint(p.latitude, p.longitude)
                binding?.selectedLocationTextView?.text = "Selected Location: (${p.latitude}, ${p.longitude})"
                return true
            }

            override fun longPressHelper(p: OSGeoPoint): Boolean = false
        })
        binding?.mapView?.overlays?.add(mapEventsOverlay)
    }

    private fun setupImagePicker() {
        imagePicker = ImagePickerUtil(this) { bitmap ->
            bitmap?.let {
                viewModel.addImage(it)
                photosGridAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupListeners() {
        binding?.addPhotosButton?.setOnClickListener {
            // Open gallery to add photos
            imagePicker.showImagePickerDialog()
        }

        binding?.sharePostButton?.setOnClickListener {
            val description = binding?.descriptionEditText?.text.toString().trim()
            val locationName = binding?.locationNameEditText?.text.toString().trim()

            if (validateInputs(description, locationName)) {
                viewModel.createPost(description, locationName) {
                    // This will be called when post is created successfully
                    Toast.makeText(requireContext(), "Post shared successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(AddPostFragmentDirections.actionAddPostFragmentToAllPostsFragment())
                }
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
            binding?.selectedLocationTextView?.text = selectedAddress
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
        if (viewModel.selectedImages.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one photo", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val TAG = "AddPostFragment"
    }
}
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
import com.google.gson.JsonParser
import okhttp3.*
import java.io.IOException
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
import com.google.gson.JsonSyntaxException
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
    private lateinit var authManager: AuthManager

    private lateinit var imagePicker: ImagePickerUtil
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val selectedImages = mutableListOf<Bitmap>()

    private lateinit var photosGridAdapter: PhotosGridAdapter
    private var currentGeoPoint: GeoPoint? = null // To store current location's coordinates

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(layoutInflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupMap()
        setupImagePicker()
        setupPhotoRecyclerView()
        setupListeners()

        val searchLocation = binding?.searchLocationTextView
        val locationEditText = binding?.selectedLocationTextView

        searchLocation?.setAdapter(ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, mutableListOf<String>()))
        searchLocation?.threshold = 2 // Start search after 2 characters

        searchLocation?.setOnItemClickListener { _, _, position, _ ->
            val selectedAddress = searchLocation.adapter.getItem(position) as String
            searchLocation.setText(selectedAddress)
            locationEditText?.text = selectedAddress
            fetchGeoLocation(selectedAddress)
        }

        searchLocation?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 2) {
                    fetchAddressSuggestions(s.toString())
                }
            }
        })
    }

    private fun setupPhotoRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 4) // 4 columns in the grid
        binding?.photosGridRecyclerView?.layoutManager = layoutManager

        // initialize adapter with the actual selectedImages list
        photosGridAdapter = PhotosGridAdapter(selectedImages, onDeletePhoto = { position ->
            selectedImages.removeAt(position) // Now removes from the correct list
            photosGridAdapter.notifyItemRemoved(position) // Efficient update instead of full refresh
        })

        binding?.photosGridRecyclerView?.adapter = photosGridAdapter
    }


    private fun fetchAddressSuggestions(query: String) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/search?format=json&q=$query"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "YourAppName") // Required by Nominatim
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    Log.d(TAG, "API_RESPONSE: Response: $json") // 🔍 Log the full response

                    try {
                        val jsonArray = JsonParser.parseString(json).asJsonArray
                        val suggestions = mutableListOf<String>()

                        for (element in jsonArray) {
                            val obj = element.asJsonObject
                            val displayName = obj.get("display_name").asString
                            suggestions.add(displayName)
                        }

                        activity?.runOnUiThread {
                            val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, suggestions)
                            binding?.searchLocationTextView?.setAdapter(adapter)
                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: JsonSyntaxException) {
                        Log.e(TAG, "API_ERROR JSON parsing error: ${e.message}")
                    }
                }
            }
        })
    }
    private fun fetchGeoLocation(address: String) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/search?format=json&q=$address&limit=1"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "YourAppName") // Required by Nominatim
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    val jsonArray = JsonParser.parseString(json).asJsonArray

                    if (jsonArray.size() > 0) {
                        val obj = jsonArray[0].asJsonObject
                        val lat = obj.get("lat").asString.toDouble()
                        val lon = obj.get("lon").asString.toDouble()

                        activity?.runOnUiThread {
                            val firebaseGeoPoint  = GeoPoint(lat, lon)
                            val osmdroidGeoPoint = org.osmdroid.util.GeoPoint(lat, lon) // ✅ Convert to osmdroid GeoPoint

                            binding?.mapView?.controller?.setCenter(osmdroidGeoPoint)
                            binding?.mapView?.controller?.setZoom(15.0)
                            currentGeoPoint = firebaseGeoPoint
                        }
                    }
                }
            }
        })
    }

    private fun setupMap() {
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
        binding?.mapView?.setMultiTouchControls(true)

        // Get user location if available
        getCurrentUserLocation { userGeoPoint ->
            currentGeoPoint = userGeoPoint
            binding?.mapView?.controller?.setCenter(OSGeoPoint(userGeoPoint.latitude, userGeoPoint.longitude))
            binding?.mapView?.controller?.setZoom(15.0)
        }

        // Add an overlay to capture map clicks
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: OSGeoPoint): Boolean {
                currentGeoPoint = GeoPoint(p.latitude, p.longitude)
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
                if (!selectedImages.contains(it)) { // Prevent duplicates
                    selectedImages.add(it)
                    photosGridAdapter.notifyItemInserted(selectedImages.size - 1) // Notify adapter
                }
            }
        }
    }

    private fun setupListeners() {
        binding?.addPhotosButton?.setOnClickListener {
            // Open gallery to add photos
            imagePicker.showImagePickerDialog() // Opens image picker
        }

        binding?.sharePostButton?.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener  // Stop execution if validation fails
            createPost()
        }
    }

    private fun createPost() {
        val description = binding?.descriptionEditText?.text.toString().trim()
        val locationName = binding?.locationNameEditText?.text.toString().trim()
        val timestamp = Timestamp.now()

        authManager.getCurrentUser()?.let {
            val post = Post(
                id = "", // Auto-generated by Firestore
                locationName = locationName,
                description = description,
                photos = listOf(), // Handled in Model
                location = currentGeoPoint ?: GeoPoint(0.0, 0.0),
                creationTime = timestamp,
                ownerId = it.uid
            )

            Model.shared.addPost(post, selectedImages) {
                Log.d(TAG, "createPost: added photos: ${selectedImages.size}")
                Toast.makeText(requireContext(), "Post shared successfully!", Toast.LENGTH_SHORT).show()

                val action = AddPostFragmentDirections.actionAddPostFragmentToAllPostsFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun getCurrentUserLocation(callback: (GeoPoint) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(GeoPoint(location.latitude, location.longitude))
            } else {
                callback(GeoPoint(0.0, 0.0)) // Default location
            }
        }
    }

    private fun validateInputs(): Boolean {
        val description = binding?.descriptionEditText?.text.toString().trim()
        val locationName = binding?.locationNameEditText?.text.toString().trim()

        var isValid = true

        if (!InputValidator.validateRequiredTextField(locationName, binding?.locationInputLayout)) {
            isValid = false
        }
        if (!InputValidator.validateRequiredTextField(description, binding?.descriptionInputLayout)) {
            isValid = false
        }
        if (selectedImages.isEmpty()) {
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

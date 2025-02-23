package com.syb.travelsphere.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.ArrayAdapter
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import java.io.IOException
import com.syb.travelsphere.components.PhotosGridAdapter
import com.syb.travelsphere.services.Geotag
import com.syb.travelsphere.services.TravelService
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.syb.travelsphere.databinding.FragmentAddPostBinding
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime

class AddPostFragment : Fragment() {

    private var binding: FragmentAddPostBinding? = null

    private val photos = mutableListOf<String>() // Stores Base64 photo strings
    private val travelService = TravelService()

    private val imagePickerRequestCode = 1001
    private lateinit var photosGridAdapter: PhotosGridAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentGeoPoint: GeoPoint? = null // To store current location's coordinates

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(layoutInflater, container, false)

        setupPhotoRecyclerView()
        setupListeners()

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupMap()

        val searchLocation = binding?.searchLocation
        val locationEditText = binding?.selectedLocationTextView
        val mapView = binding?.mapView

        searchLocation?.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf<String>()))
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
        photosGridAdapter = PhotosGridAdapter(photos, onDeletePhoto = { position ->
            photos.removeAt(position)
            photosGridAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Photo removed", Toast.LENGTH_SHORT).show()
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
                    val jsonArray = JsonParser.parseString(json).asJsonArray

                    val suggestions = mutableListOf<String>()
                    for (element in jsonArray) {
                        val obj = element.asJsonObject
                        val displayName = obj.get("display_name").asString
                        suggestions.add(displayName)
                    }

                    activity?.runOnUiThread {
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                        binding?.searchLocation?.setAdapter(adapter)
                        adapter.notifyDataSetChanged()
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
                            val geoPoint = GeoPoint(lat, lon)
                            binding?.mapView?.controller?.setCenter(geoPoint)
                            binding?.mapView?.controller?.setZoom(15.0)
                            currentGeoPoint = geoPoint
                        }
                    }
                }
            }
        })
    }



    private fun setupMap() {
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
        binding?.mapView?.setMultiTouchControls(true)

        // Get the current location of the user
        val currentLocation = getCurrentUserLocation()

        // Set the map's initial zoom and center to the user's location
        binding?.mapView?.controller?.setZoom(100.0) // Adjust zoom level as needed
        binding?.mapView?.controller?.setCenter(currentLocation) // Set the center to user's location

        // Add an overlay to capture map clicks
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                // Update the map center and current geo-point
                binding?.mapView?.controller?.setCenter(p)
                currentGeoPoint = p
                binding?.selectedLocationTextView?.text = "Geotag: (Lat: ${p.latitude}, Lon: ${p.longitude})"

                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                // Handle long press if needed
                return false
            }
        })

        // Add the overlay to the map
        binding?.mapView?.overlays?.add(mapEventsOverlay)
    }

    private fun setupListeners() {
        binding?.addPhotosButton?.setOnClickListener {
            // Open gallery to add photos
            openGallery()
        }

        binding?.sharePostButton?.setOnClickListener {
            sharePost()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, imagePickerRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePickerRequestCode && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                // Convert image URI to Base64 string
                val base64Image = convertImageToBase64(imageUri)
                addPhotoToGrid(base64Image)
            }
        }
    }

    private fun convertImageToBase64(imageUri: android.net.Uri): String {
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun addPhotoToGrid(photoBase64: String) {
        photos.add(photoBase64)
        photosGridAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "Photo added", Toast.LENGTH_SHORT).show()
    }

    fun getCurrentTimeISO(): String {
        val currentTime = ZonedDateTime.now() // Get current time with time zone
        return currentTime.format(DateTimeFormatter.ISO_INSTANT) // Format to ISO 8601
    }

    private fun sharePost() {
        val location = binding?.selectedLocationTextView?.text.toString() // Now contains the geotag info
        val desc = binding?.descriptionEditText?.text.toString()
        val visitDate = getCurrentTimeISO()
        val photosToUpload =  photos.map { it.replace("data:image/jpeg;base64,", "") }

        val geoTag = currentGeoPoint?.let {
            Geotag("Point", listOf(it.latitude, it.longitude))
        } ?: Geotag("Point", listOf(0.0, 0.0)) // Default geo-tag if no location is selected

        lifecycleScope.launch {
            try {
                val response = travelService.createPost(location, desc, visitDate, photosToUpload, geoTag)
                if (response != null) {
                    Toast.makeText(requireContext(), "Post shared successfully!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(requireContext(), "Failed to share post.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle the exception
                e.printStackTrace()
                Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getCurrentUserLocation(): GeoPoint {
        var userLocation = GeoPoint(31.771959, 34.651401) // Default location (Jerusalem, Israel)

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )

            return userLocation // Returning default location
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Success: Get the current location
                Log.d("UserLocation", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                userLocation = GeoPoint(location.latitude, location.longitude)
            } else {
                // Location is null, handle this case (e.g., use default location)
                Log.d("UserLocation", "Location is null, using default: $userLocation")
            }
        }

        return userLocation // Return updated location once fetched
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

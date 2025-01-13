package com.syb.travelsphere.pages

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.syb.travelsphere.R
import com.syb.travelsphere.components.PhotosGridAdapter
import com.syb.travelsphere.services.Geotag
import com.syb.travelsphere.services.TravelService
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.location.Geocoder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime

class AddPostFragment : Fragment() {

    private lateinit var mapView: MapView

    //    private lateinit var searchLocation: EditText
    private lateinit var selectedLocation: TextView
    private lateinit var description: EditText
    private lateinit var spotName: EditText
    private lateinit var addPhotosButton: Button
    private lateinit var sharePostButton: Button
    private lateinit var photosRecyclerView: RecyclerView

    private val photos = mutableListOf<String>() // Stores Base64 photo strings
    private val travelService = TravelService()

    private val imagePickerRequestCode = 1001
    private lateinit var photosGridAdapter: PhotosGridAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentGeoPoint: GeoPoint? = null // To store current location's coordinates

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)
        initViews(view)
        setupPhotoRecyclerView()
        setupListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        setupMap()
    }




    private fun initViews(view: View) {
        mapView = view.findViewById(R.id.mapView)
//        searchLocation = view.findViewById(R.id.search_location)
        selectedLocation = view.findViewById(R.id.selected_location)
        description = view.findViewById(R.id.description)
        spotName = view.findViewById(R.id.locationName)

        addPhotosButton = view.findViewById(R.id.add_photos)
        sharePostButton = view.findViewById(R.id.share_post)
        photosRecyclerView = view.findViewById(R.id.photos_grid)
    }

    private fun setupPhotoRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 4) // 4 columns in the grid
        photosRecyclerView.layoutManager = layoutManager
        photosGridAdapter = PhotosGridAdapter(photos, onDeletePhoto = { position ->
            photos.removeAt(position)
            photosGridAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Photo removed", Toast.LENGTH_SHORT).show()
        })
        photosRecyclerView.adapter = photosGridAdapter
    }

    private fun setupMap() {
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0))
        mapView.setMultiTouchControls(true)

        // Get the current location of the user
        val currentLocation = getCurrentUserLocation()

        // Set the map's initial zoom and center to the user's location
        mapView.controller.setZoom(100.0) // Adjust zoom level as needed
        mapView.controller.setCenter(currentLocation) // Set the center to user's location

        // Add an overlay to capture map clicks
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                // Update the map center and current geo-point
                mapView.controller.setCenter(p)
                currentGeoPoint = p
                selectedLocation.text = "Geotag: (Lat: ${p.latitude}, Lon: ${p.longitude})"

                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                // Handle long press if needed
                return false
            }
        })

        // Add the overlay to the map
        mapView.overlays.add(mapEventsOverlay)
    }





    private fun setupListeners() {
        addPhotosButton.setOnClickListener {
            // Open gallery to add photos
            openGallery()
        }

        sharePostButton.setOnClickListener {
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
        val location = spotName.text.toString() // Now contains the geotag info
        val desc = description.text.toString()
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
                } else {
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
}

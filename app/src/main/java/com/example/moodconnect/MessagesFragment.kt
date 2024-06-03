package com.example.moodconnect

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.core.view.drawToBitmap
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import android.location.Geocoder
import android.location.Address

private const val ARG_MESSAGE_LIST_ITEM = "messageListItem"
private const val LOCATION_PERMISSION_REQUEST_CODE = 101

// Fragment for displaying and managing messages
class MessagesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonProfile: ImageButton
    private lateinit var buttonMoreOptions: ImageButton
    private lateinit var moreOptionsLayout: LinearLayout
    private lateinit var buttonSendImage: ImageButton
    private lateinit var buttonShareLocation: ImageButton
    private lateinit var buttonShareWeather: ImageButton
    private lateinit var buttonShareMood: ImageButton
    private lateinit var imagePreview: ImageView
    private var imagePreviewDialog: BottomSheetDialog? = null

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PHOTO_REQUEST = 2
    private val CAMERA_REQUEST_CODE = 1003
    private val GALLERY_REQUEST_CODE = 1005
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001

    private var messageListItem: MessageListItem? = null
    private var selectedImageUri: Uri? = null
    private var capturedImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            messageListItem = it.getParcelable(ARG_MESSAGE_LIST_ITEM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSend = view.findViewById(R.id.buttonSend)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonProfile = view.findViewById(R.id.buttonProfile)
        buttonMoreOptions = view.findViewById(R.id.buttonMoreOptions)
        moreOptionsLayout = view.findViewById(R.id.moreOptionsLayout)
        buttonSendImage = view.findViewById(R.id.buttonSendImage)
        buttonShareLocation = view.findViewById(R.id.buttonShareLocation)
        buttonShareWeather = view.findViewById(R.id.buttonShareWeather)
        buttonShareMood = view.findViewById(R.id.buttonShareMood)
        imagePreview = view.findViewById(R.id.imagePreview)

        // Set up RecyclerView
        messageAdapter = MessageAdapter(listOf())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = messageAdapter

        // Handle send button click
        buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString()
            val receiverId = messageListItem?.userId ?: "user2"
            val senderId = "Yuhan Lu"
            val timestamp = System.currentTimeMillis()

            if (messageText.isNotEmpty()) {
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    senderId = senderId,
                    receiverId = receiverId,
                    content = messageText,
                    timestamp = timestamp,
                    type = MessageType.TEXT
                )
                messageAdapter.addMessage(message)
                editTextMessage.text.clear()
            }
        }

        // Handle back button click
        buttonBack.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        // Handle profile button click
        buttonProfile.setOnClickListener {
            val profileDialogFragment = ProfileDialogFragment()
            profileDialogFragment.show(parentFragmentManager, "ProfileDialogFragment")
        }

        // Handle more options button click
        buttonMoreOptions.setOnClickListener {
            if (moreOptionsLayout.visibility == View.GONE) {
                moreOptionsLayout.visibility = View.VISIBLE
            } else {
                moreOptionsLayout.visibility = View.GONE
            }
        }

        // Handle send image button click
        buttonSendImage.setOnClickListener {
            showImagePickerDialog()
        }

        // Handle share location button click
        buttonShareLocation.setOnClickListener {
            showLocationBottomDialog()
        }

        // Handle share weather button click
        buttonShareWeather.setOnClickListener {
            showWeatherBottomDialog()
        }

        // Handle share mood button click
        buttonShareMood.setOnClickListener {
            showMoodPickerBottomDialog()
        }

        // Hide main UI elements when this fragment is created
        (activity as MainActivity).hideMainUI()

        return view
    }

    @SuppressLint("MissingInflatedId")
    private fun showLocationBottomDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_location_picker, null)

        val mapView: MapView = view.findViewById(R.id.mapView)
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync { googleMap ->
            // Initialize location and set marker
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                return@getMapAsync
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    var addressText = "Unknown Location"

                    try {
                        val addresses: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.toList() ?: emptyList()
                        if (addresses.isNotEmpty()) {
                            val address: Address = addresses[0]
                            addressText = address.getAddressLine(0)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Unable to get address for location", Toast.LENGTH_SHORT).show()
                    }

                    view.findViewById<Button>(R.id.buttonSendLocation).setOnClickListener {
                        googleMap.snapshot { bitmap ->
                            if (bitmap != null) {
                                val receiverId = messageListItem?.userId ?: "user2"
                                val senderId = "Yuhan Lu"
                                val timestamp = System.currentTimeMillis()

                                val message = Message(
                                    id = UUID.randomUUID().toString(),
                                    senderId = senderId,
                                    receiverId = receiverId,
                                    content = addressText,
                                    timestamp = timestamp,
                                    type = MessageType.LOCATION,
                                    imageBitmap = bitmap,
                                    location = latLng
                                )
                                messageAdapter.addMessage(message)
                                recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                                dialog.dismiss()
                            } else {
                                Toast.makeText(requireContext(), "Failed to capture map view.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    view.findViewById<Button>(R.id.buttonCancelLocation).setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showWeatherBottomDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_weather_bottom_dialog, null)

        // Initialize weather UI elements
        val tvCityName: TextView = view.findViewById(R.id.tvCityName)
        val tvUpdatedTime: TextView = view.findViewById(R.id.tvUpdatedTime)
        val tvWeatherCondition: TextView = view.findViewById(R.id.tvWeatherCondition)
        val tvTemperature: TextView = view.findViewById(R.id.tvTemperature)
        val tvMinMaxTemp: TextView = view.findViewById(R.id.tvMinMaxTemp)
        val tvSunrise: TextView = view.findViewById(R.id.tvSunrise)
        val tvSunset: TextView = view.findViewById(R.id.tvSunset)
        val tvWind: TextView = view.findViewById(R.id.tvWind)
        val tvPressure: TextView = view.findViewById(R.id.tvPressure)
        val tvHumidity: TextView = view.findViewById(R.id.tvHumidity)

        // Initialize weather API elements
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val requestQueue = Volley.newRequestQueue(requireContext())

        view.findViewById<Button>(R.id.buttonSendWeather).setOnClickListener {
            val weatherView = view.findViewById<View>(R.id.weatherView)
            val weatherBitmap = weatherView.drawToBitmap()
            onWeatherSend(weatherBitmap)
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.buttonCancelWeather).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()

        // Fetch and display weather data
        getCurrentLocationWeather(tvCityName, tvUpdatedTime, tvWeatherCondition, tvTemperature, tvMinMaxTemp, tvSunrise, tvSunset, tvWind, tvPressure, tvHumidity, fusedLocationClient, requestQueue)
    }

    // Handle sending the weather information
    private fun onWeatherSend(weatherBitmap: Bitmap) {
        val receiverId = messageListItem?.userId ?: "user2"
        val senderId = "Yuhan Lu"
        val timestamp = System.currentTimeMillis()

        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId,
            content = "Weather Image",
            timestamp = timestamp,
            type = MessageType.IMAGE,
            imageBitmap = weatherBitmap
        )
        messageAdapter.addMessage(message)
        recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
    }

    // Fetch current location weather data
    private fun getCurrentLocationWeather(tvCityName: TextView, tvUpdatedTime: TextView, tvWeatherCondition: TextView, tvTemperature: TextView, tvMinMaxTemp: TextView, tvSunrise: TextView, tvSunset: TextView, tvWind: TextView, tvPressure: TextView, tvHumidity: TextView, fusedLocationClient: FusedLocationProviderClient, requestQueue: RequestQueue) {
        if (!isNetworkAvailable()) {
            Toast.makeText(requireContext(), "Please connect to internet", Toast.LENGTH_SHORT).show()
            return
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 101)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val lat = it.latitude
                val lon = it.longitude
                getWeatherForLocation(lat, lon, tvCityName, tvUpdatedTime, tvWeatherCondition, tvTemperature, tvMinMaxTemp, tvSunrise, tvSunset, tvWind, tvPressure, tvHumidity, requestQueue)
            }
        }
    }

    // Get weather data for the current location
    private fun getWeatherForLocation(lat: Double, lon: Double, tvCityName: TextView, tvUpdatedTime: TextView, tvWeatherCondition: TextView, tvTemperature: TextView, tvMinMaxTemp: TextView, tvSunrise: TextView, tvSunset: TextView, tvWind: TextView, tvPressure: TextView, tvHumidity: TextView, requestQueue: RequestQueue) {
        val apiKey = "your_openweathermap_api_key" // Replace with your OpenWeatherMap API key
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$apiKey"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response -> parseWeatherResponse(response, tvCityName, tvUpdatedTime, tvWeatherCondition, tvTemperature, tvMinMaxTemp, tvSunrise, tvSunset, tvWind, tvPressure, tvHumidity) },
            { Toast.makeText(requireContext(), "Failed to get weather for current location", Toast.LENGTH_SHORT).show() })
        requestQueue.add(stringRequest)
    }

    // Parse the weather response from the API
    private fun parseWeatherResponse(response: String, tvCityName: TextView, tvUpdatedTime: TextView, tvWeatherCondition: TextView, tvTemperature: TextView, tvMinMaxTemp: TextView, tvSunrise: TextView, tvSunset: TextView, tvWind: TextView, tvPressure: TextView, tvHumidity: TextView) {
        val jsonObject = JSONObject(response)
        val city = jsonObject.getString("name")
        val weatherArray = jsonObject.getJSONArray("weather")
        val weather = weatherArray.getJSONObject(0).getString("description")
        val main = jsonObject.getJSONObject("main")
        val temp = main.getDouble("temp")
        val tempMin = main.getDouble("temp_min")
        val tempMax = main.getDouble("temp_max")
        val pressure = main.getDouble("pressure")
        val humidity = main.getInt("humidity")
        val wind = jsonObject.getJSONObject("wind")
        val windSpeed = wind.getDouble("speed")
        val sys = jsonObject.getJSONObject("sys")
        val sunrise = sys.getLong("sunrise")
        val sunset = sys.getLong("sunset")

        tvCityName.text = city
        tvUpdatedTime.text = "Updated at: ${getCurrentTime()}"
        tvWeatherCondition.text = weather
        tvTemperature.text = "${temp}°C"
        tvMinMaxTemp.text = "Min Temp: ${tempMin}°C    Max Temp: ${tempMax}°C"
        tvSunrise.text = convertUnixToTime(sunrise)
        tvSunset.text = convertUnixToTime(sunset)
        tvWind.text = "${windSpeed} m/s"
        tvPressure.text = "${pressure} hPa"
        tvHumidity.text = "${humidity}%"
    }

    // Convert Unix timestamp to human-readable time
    private fun convertUnixToTime(time: Long): String {
        val date = Date(time * 1000)
        val format = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
        return format.format(date)
    }

    // Get the current time in a specific format
    private fun getCurrentTime(): String {
        val date = Date()
        val format = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a")
        return format.format(date)
    }

    // Check if network is available
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Show mood picker bottom dialog
    private fun showMoodPickerBottomDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_mood_picker, null)

        val imageViewMood: ImageView = view.findViewById(R.id.imageViewMood)
        val seekBarMood: SeekBar = view.findViewById(R.id.seekBarMood)
        val buttonSendMood: Button = view.findViewById(R.id.buttonSendMood)
        val buttonCancelMood: Button = view.findViewById(R.id.buttonCancelMood)

        // Array of mood images (replace with your actual drawable resources)
        val moodImages = arrayOf(
            R.drawable.mood_1,
            R.drawable.mood_2,
            R.drawable.mood_3,
            R.drawable.mood_4,
            R.drawable.mood_5
        )

        // Set initial mood image
        imageViewMood.setImageResource(moodImages[0])

        // Update mood image based on seek bar position
        seekBarMood.max = moodImages.size - 1
        seekBarMood.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                imageViewMood.setImageResource(moodImages[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }
        })

        buttonSendMood.setOnClickListener {
            val drawable = imageViewMood.drawable as BitmapDrawable
            val moodBitmap = drawable.bitmap

            val receiverId = messageListItem?.userId ?: "user2"
            val senderId = "Yuhan Lu"
            val timestamp = System.currentTimeMillis()

            val message = Message(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                receiverId = receiverId,
                content = "Mood Image",
                timestamp = timestamp,
                type = MessageType.IMAGE,
                imageBitmap = moodBitmap
            )
            messageAdapter.addMessage(message)
            recyclerView.scrollToPosition(messageAdapter.itemCount - 1)

            dialog.dismiss()
        }

        buttonCancelMood.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // Show image picker dialog
    private fun showImagePickerDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)
        val buttonGallery: Button = view.findViewById(R.id.buttonGallery)
        val buttonCamera: Button = view.findViewById(R.id.buttonCamera)
        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)
        val imagePreview: ImageView = view.findViewById(R.id.imagePreview)
        val buttonSend: Button = view.findViewById(R.id.buttonSend)

        buttonGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_REQUEST_CODE)
            } else {
                openGallery()
            }
        }

        buttonCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission if not granted
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            } else {
                // Open the camera if permission is granted
                openCamera()
            }
        }

        buttonCancel.setOnClickListener {
            selectedImageUri = null
            capturedImageBitmap = null
            imagePreview.visibility = View.GONE
            buttonSend.visibility = View.GONE
            buttonGallery.visibility = View.VISIBLE
            buttonCamera.visibility = View.VISIBLE
            buttonCancel.visibility = View.GONE
        }

        buttonSend.setOnClickListener {
            val receiverId = messageListItem?.userId ?: "user2"
            val senderId = "Yuhan Lu"
            val timestamp = System.currentTimeMillis()

            if (capturedImageBitmap != null) {
                // Handle sending bitmap image
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    senderId = senderId,
                    receiverId = receiverId,
                    content = "Bitmap Image",
                    timestamp = timestamp,
                    type = MessageType.IMAGE,
                    imageBitmap = capturedImageBitmap
                )
                messageAdapter.addMessage(message)
                recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                imagePreview.visibility = View.GONE
                imagePreview.setImageBitmap(null)
                capturedImageBitmap = null
            } else if (selectedImageUri != null) {
                // Handle sending URI image
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    senderId = senderId,
                    receiverId = receiverId,
                    content = selectedImageUri.toString(),
                    timestamp = timestamp,
                    type = MessageType.IMAGE,
                    imageUri = selectedImageUri
                )
                messageAdapter.addMessage(message)
                recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                imagePreview.visibility = View.GONE
                imagePreview.setImageURI(null)
                selectedImageUri = null
            }
            dialog.dismiss()
        }

        imagePreviewDialog = dialog

        dialog.setContentView(view)
        dialog.show()
    }

    // Open gallery to pick an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Open camera to take a photo
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }

    // Handle permission results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission was granted, open the camera
            openCamera()
        } else if (requestCode == GALLERY_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission was granted, open the gallery
            openGallery()
        } else if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Handle permission granted for external storage if needed
        } else {
            // Permission was denied, handle the case
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle result from activity (camera or gallery)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        selectedImageUri = uri
                        imagePreviewDialog?.findViewById<ImageView>(R.id.imagePreview)?.apply {
                            setImageURI(uri)
                            visibility = View.VISIBLE
                        }
                        imagePreviewDialog?.findViewById<Button>(R.id.buttonSend)?.visibility = View.VISIBLE
                        imagePreviewDialog?.findViewById<Button>(R.id.buttonCancel)?.visibility = View.VISIBLE
                        imagePreviewDialog?.findViewById<Button>(R.id.buttonGallery)?.visibility = View.GONE
                        imagePreviewDialog?.findViewById<Button>(R.id.buttonCamera)?.visibility = View.GONE
                    }
                }
                TAKE_PHOTO_REQUEST -> {
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    capturedImageBitmap = photo
                    imagePreviewDialog?.findViewById<ImageView>(R.id.imagePreview)?.apply {
                        setImageBitmap(photo)
                        visibility = View.VISIBLE
                    }
                    imagePreviewDialog?.findViewById<Button>(R.id.buttonSend)?.visibility = View.VISIBLE
                    imagePreviewDialog?.findViewById<Button>(R.id.buttonCancel)?.visibility = View.VISIBLE
                    imagePreviewDialog?.findViewById<Button>(R.id.buttonGallery)?.visibility = View.GONE
                    imagePreviewDialog?.findViewById<Button>(R.id.buttonCamera)?.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        // Create a new instance of MessagesFragment with a MessageListItem argument
        @JvmStatic
        fun newInstance(messageListItem: MessageListItem) =
            MessagesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MESSAGE_LIST_ITEM, messageListItem)
                }
            }
    }
}

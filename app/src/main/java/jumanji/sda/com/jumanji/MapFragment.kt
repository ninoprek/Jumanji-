package jumanji.sda.com.jumanji

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresPermission
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), PhotoListener, OnMapReadyCallback {
    companion object {
        private const val LAST_KNOWN_ZOOM = "last_known_zoom"
        private const val LAST_KNOWN_LONGITUDE = "last_known_longitude"
        private const val LAST_KNOWN_LATITUDE = "last_known_latitude"
        private const val CAMERA_PREFERENCE = "camera_preference"
        private const val LOCATION_REQUEST_CODE = 300
        private const val REQUEST_CAMERA_CODE = 100
        private const val SELECT_FILE_CODE = 200
    }

    private lateinit var mapPreference: CameraStateManager

    private lateinit var map: GoogleMap
    private lateinit var locationViewModel: LocationViewModel

    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: LatLng

    var userChoosenTask: String = ""

    private var trashLocationViewModel: TrashLocationViewModel? = null
    private var currentView: LatLngBounds? = null
    private lateinit var mapAdapter: GoogleMapAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapPreference = CameraStateManager()
        locationViewModel = ViewModelProviders.of(this)[LocationViewModel::class.java]

        mapAdapter = GoogleMapAdapter()

        checkUserLocationSetting()

        mapView.getMapAsync(this)

        refreshFab.setOnClickListener {
            if (currentView != null && mapAdapter.map != null) {
                Snackbar.make(it, "loading locations...", Snackbar.LENGTH_SHORT).show()
                trashLocationViewModel?.loadLocations(currentView, true)
                mapAdapter.bindMarkers()
            }
        }

        reportFab.setOnClickListener {
            selectImage()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        addPin.setOnClickListener {
            val pinViewModel: PinViewModel = PinViewModel()
            var user: String

            if (FirebaseAuth.getInstance().currentUser?.displayName != null) {
                user = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            } else {
                user = GoogleSignIn.getLastSignedInAccount(activity)?.displayName.toString()
            }

            pinViewModel.testSavePinData(user)
            Snackbar.make(it, "Pin has been added!", Snackbar.LENGTH_SHORT).show()
        }

        deletePin.setOnClickListener {
            val view = it

            val pinViewModel = ViewModelProviders.of(this)[PinViewModel::class.java]

            pinViewModel.testGetPinData()

            pinViewModel.pinData?.observe(this, Observer {
                Snackbar.make(view, "Here is the pin: $it", Snackbar.LENGTH_SHORT).show()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        if (this::locationCallback.isInitialized) {
            locationViewModel.stopLocationUpdates(locationCallback)
        }
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapPreference.saveMapCameraState()
        mapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val cameraState = mapPreference.getCameraState()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraState))
        enableMyLocationLayer(locationViewModel)

        map.setOnMyLocationButtonClickListener {
            locationViewModel.getLastKnownLocation(map)
            true
        }

        trashLocationViewModel = ViewModelProviders.of(this)[TrashLocationViewModel::class.java]
        trashLocationViewModel?.let { trashLocationViewModel ->
            trashLocationViewModel.map = map
            mapAdapter.map = map

            trashLocationViewModel.trashMarkers.observe(this, Observer {
                it?.let {
                    mapAdapter.trashLocationMarkers = it
                    mapAdapter.bindMarkers()
                    totalNoOfTrashLocationText.text = it.size.toString()
                }
            })

            trashLocationViewModel.trashFreeMarkers.observe(this, Observer {
                it?.let {
                    mapAdapter.trashFreeMarkers = it
                    mapAdapter.bindMarkers()
                    totalNoOfTrashLocationClearedText.text = it.size.toString()
                }
            })
            map.setOnCameraIdleListener {

                currentView = map.projection.visibleRegion.latLngBounds
                trashLocationViewModel.loadLocations(currentView, false)
                mapAdapter.bindMarkers()
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    private fun checkUserLocationSetting() {
        locationViewModel.initiateUserSettingCheck(this@MapFragment.context)
                ?.addOnCompleteListener { task ->
                    try {
                        val result = task.getResult(ApiException::class.java)
                        if (result.locationSettingsStates.isGpsUsable) {
                            locationCallback = object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult?) {
                                    val location = locationResult?.locations?.get(0)
                                    if (location != null) {
                                        currentLocation = LatLng(location.latitude, location.longitude)
                                    }
                                }
                            }
                            locationViewModel.startLocationUpdates(this.context, locationCallback)
                        }
                    } catch (e: ApiException) {
                        this@MapFragment.view?.let { view ->
                            Snackbar.make(view,
                                    "You probably forget to on GPS or are in airplane mode.",
                                    Snackbar.LENGTH_SHORT)
                                    .setDuration(3000)
                                    .show()
                        }
                        Log.d("TAG", "something went wrong in user setting check: ${e.message}")
                    }
                }
    }

    private fun enableMyLocationLayer(viewModel: LocationViewModel) {
        val permission = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.ACCESS_FINE_LOCATION)
        if (ActivityCompat.checkSelfPermission(context!!, permission[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context!!, permission[1]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, LOCATION_REQUEST_CODE)
        } else {
            map.isMyLocationEnabled = true
            viewModel.getLastKnownLocation(map)
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                    enableMyLocationLayer(locationViewModel)
                } else {
                    Toast.makeText(this@MapFragment.context, "Permission is needed.", Toast.LENGTH_SHORT).show()
                }
            }

            Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent()
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent()
                } else {
                    //code for deny
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        fun saveFile() {
            val uri = data?.data
            val mStorageRef: StorageReference = FirebaseStorage.getInstance().getReference("images")
            val riversRef = mStorageRef.child("$uri")
            Log.e("value", "uri Value: $uri")

            if (uri != null) {
                riversRef.putFile(uri)
                        .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                            // Get a URL to the uploaded content
                            val downloadUrl = taskSnapshot.downloadUrl
                            Log.d("SUCCESS", "Able  to upload")
                            val toast = Toast.makeText(activity, "File Uploaded ", Toast.LENGTH_SHORT)
                            toast.show()

                        })
                        .addOnFailureListener { exception ->
                            Toast.makeText(activity, exception.message, Toast.LENGTH_SHORT).show()
                            Log.d("ERROR", "Unable to upload")
                        }
            } else {
                Toast.makeText(activity, "File not found ", Toast.LENGTH_SHORT).show()
            }
        }

        when (requestCode) {
            REQUEST_CAMERA_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    saveFile()

                }
            }

            SELECT_FILE_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    saveFile()
                    val position = getLatLngFromPhoto(data)
                    if (position.latitude == 0.0 && position.longitude == 0.0) {
                        Toast.makeText(this@MapFragment.context,
                                "No position available from photo",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("TAG", "lat lng of photo : $position")
                    }
                }
            }
        }
    }

    override fun selectImage() {
        val items = arrayOf("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(this@MapFragment.context!!)
        builder.setTitle("Add Photo!")
        builder.setItems(items, { dialog, item ->
            val result = Utility.checkPermission(this@MapFragment.context!!)
            if (items[item] == "Take Photo") {
                userChoosenTask = "Take Photo"
                if (result)
                    cameraIntent()
            } else if (items[item] == "Choose from Library") {
                userChoosenTask = "Choose from Library"
                if (result)
                    galleryIntent()
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    private fun cameraIntent() {
        locationViewModel.flushLocations()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA_CODE)
    }

    private fun galleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE_CODE)
    }

    private fun getLatLngFromPhoto(data: Intent): LatLng {
        val uri = data.data
        var cursor = this@MapFragment.context!!.contentResolver.query(
                uri,
                null,
                null,
                null,
                null)
        cursor.moveToFirst()
        val columnIndexOfDisplayName = 2
        val fileName = cursor.getString(columnIndexOfDisplayName)
        cursor.close()
        val selection = "${MediaStore.Images.ImageColumns.DISPLAY_NAME}='$fileName'"
        cursor = this@MapFragment.context!!.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media.LATITUDE,
                        MediaStore.Images.Media.LONGITUDE),
                selection,
                null,
                null)
        cursor.moveToFirst()
        return LatLng(cursor.getDouble(0), cursor.getDouble(1))
    }

    class GoogleMapAdapter {
        var map: GoogleMap? = null
        var trashLocationMarkers: List<Marker> = listOf()
        var trashFreeMarkers: List<Marker> = listOf()

        fun bindMarkers() {
            trashLocationMarkers.filter { getCurrentView().contains(it.position) }
                    .forEach { it.isVisible = true }
            trashLocationMarkers.filterNot { getCurrentView().contains(it.position) }
                    .forEach { it.isVisible = false }

            trashFreeMarkers.filter { getCurrentView().contains(it.position) }
                    .forEach { it.isVisible = true }
            trashFreeMarkers.filterNot { getCurrentView().contains(it.position) }
                    .forEach { it.isVisible = false }
        }

        private fun getCurrentView(): LatLngBounds {
            return map!!.projection.visibleRegion.latLngBounds
        }
    }

    inner class CameraStateManager {
        private val mapCameraPreferences = this@MapFragment.context!!.getSharedPreferences(
                CAMERA_PREFERENCE,
                Context.MODE_PRIVATE)

        fun saveMapCameraState() {
            val cameraPosition = map.cameraPosition
            val latitude = cameraPosition.target.latitude.toFloat()
            val longitude = cameraPosition.target.longitude.toFloat()
            val zoom = cameraPosition.zoom
            val editor = mapCameraPreferences.edit()
            editor.putFloat(LAST_KNOWN_LATITUDE, latitude)
            editor.putFloat(LAST_KNOWN_LONGITUDE, longitude)
            editor.putFloat(LAST_KNOWN_ZOOM, zoom)
            editor.apply()
        }

        fun getCameraState(): CameraPosition {
            val latitude = mapCameraPreferences.getFloat(
                    LAST_KNOWN_LATITUDE,
                    LocationViewModel.DEFAULT_LATITUDE.toFloat())
                    .toDouble()
            val longitude = mapCameraPreferences.getFloat(
                    LAST_KNOWN_LONGITUDE,
                    LocationViewModel.DEFAULT_LONGITUDE.toFloat())
                    .toDouble()
            val zoom = mapCameraPreferences.getFloat(LAST_KNOWN_ZOOM,
                    LocationViewModel.DEFAULT_ZOOM_LEVEL)
            return CameraPosition(LatLng(latitude, longitude), zoom, 0.0f, 0.0f)
        }
    }
}
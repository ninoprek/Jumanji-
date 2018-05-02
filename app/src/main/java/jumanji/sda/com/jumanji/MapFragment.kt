package jumanji.sda.com.jumanji

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), PhotoListener {
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

    var userChoosenTask: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapPreference = CameraStateManager()
        locationViewModel = ViewModelProviders.of(this)[LocationViewModel::class.java]

        mapView.getMapAsync {
            map = it
            val cameraState = mapPreference.getCameraState()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraState))
            enableMyLocationLayer(locationViewModel)

            map.setOnMyLocationButtonClickListener {
                map = it
                locationViewModel.getLastKnownLocation(map)
                true
            }

            val trashLocationViewModel = ViewModelProviders.of(this)[TrashLocationViewModel::class.java]
            val mapAdapter = GoogleMapAdapter()
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

                val currentView = map.projection.visibleRegion.latLngBounds
                trashLocationViewModel.loadLocations(currentView, false)
                mapAdapter.bindMarkers()

                refreshFab.setOnClickListener {
                    Snackbar.make(it, "loading locations...", Snackbar.LENGTH_SHORT).show()
                    trashLocationViewModel.loadLocations(currentView, true)
                    mapAdapter.bindMarkers()
                }
            }
        }

        reportFab.setOnClickListener {
            selectImage()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        reportFab.setOnClickListener {
            selectImage()
        }

      /*  updateGPSFab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser?.displayName
            val profileViewModel = ProfileViewModel()
            profileViewModel.signOut()
            Snackbar.make(it, "${user}, you are signed out", Snackbar.LENGTH_SHORT).show()
        }*/

        addPin.setOnClickListener {
            val pinViewModel: PinViewModel = PinViewModel()
            pinViewModel.testSavePinData()
            Snackbar.make(it, "Pin has been added!", Snackbar.LENGTH_SHORT).show()
        }

        deletePin.setOnClickListener {
            val view = it
            val pinViewModel = ViewModelProviders.of(this)[PinViewModel::class.java]
            //pinViewModel.deletePinData("1")
            //Snackbar.make(it, "Pin has been deleted!",Snackbar.LENGTH_SHORT).show()

            pinViewModel.testGetPinData()

            pinViewModel.pinData?.observe(this, Observer {
                Snackbar.make(view, "Here is the pin: $it", Snackbar.LENGTH_SHORT).show()
            })
        }

        reportFab.setOnClickListener {
            selectImage()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
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
        mapView.onSaveInstanceState(outState)
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

            Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        Log.d("TAG", "in fragment, request code :  $requestCode")
        when (requestCode) {
            REQUEST_CAMERA_CODE -> {
                if (resultCode == PackageManager.PERMISSION_GRANTED) {

                }
            }

            SELECT_FILE_CODE -> {
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    val uri = data?.data

                    val cursor = this@MapFragment.context!!.contentResolver.query(
                            uri,
                            null,
                            null,
                            null,
                            null)
                    Log.d("TAG", "${cursor.getColumnName(2)}")
                }
            }
        }
    }

    override fun selectImage() {
        Log.d("TAG", "select image")
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(this@MapFragment.context!!)
        builder.setTitle("Add Photo!")
        builder.setItems(items, DialogInterface.OnClickListener { dialog, item ->
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
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA_CODE)
    }

    private fun galleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE_CODE)
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
        private val mapCameraPreferences = this@MapFragment.context!!.getSharedPreferences(CAMERA_PREFERENCE, Context.MODE_PRIVATE)

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
            val latitude = mapCameraPreferences.getFloat(LAST_KNOWN_LATITUDE, LocationViewModel.DEFAULT_LATITUDE.toFloat()).toDouble()
            val longitude = mapCameraPreferences.getFloat(LAST_KNOWN_LONGITUDE, LocationViewModel.DEFAULT_LONGITUDE.toFloat()).toDouble()
            val zoom = mapCameraPreferences.getFloat(LAST_KNOWN_ZOOM, LocationViewModel.DEFAULT_ZOOM_LEVEL)
            return CameraPosition(LatLng(latitude, longitude), zoom, 0.0f, 0.0f)
        }
    }
}
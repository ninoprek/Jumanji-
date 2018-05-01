package jumanji.sda.com.jumanji

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), OnLastLocationReadyListener {
    companion object {
        const val DEFAULT_ZOOM_LEVEL = 13.5f
        private const val DEFAULT_LATITUDE = 59.3498065f
        private const val DEFAULT_LONGITUDE = 18.0684759f
        private const val LAST_KNOWN_LOCATION = "last_known_location"
        private const val CAMERA_POSITION = "camera_position"
        private const val LOCATION_REQUEST_CODE = 300
    }

    private lateinit var map: GoogleMap
    private val defaultPosition = LatLng(DEFAULT_LATITUDE.toDouble(), DEFAULT_LONGITUDE.toDouble())
    private lateinit var locationViewModel: LocationViewModel

    private var listener: PhotoListener? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is PhotoListener) listener = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        locationViewModel = ViewModelProviders.of(this)[LocationViewModel::class.java]

        var cameraPosition: CameraPosition? = null
        var currentLocation: LatLng? = null
        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION)
            currentLocation = savedInstanceState.getParcelable(LAST_KNOWN_LOCATION)
        } else {
//            currentLocation =  // TODO: To get from GPS for current user location
        }

        mapView.getMapAsync {
            map = it
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPosition, DEFAULT_ZOOM_LEVEL))

            map.setOnMapLoadedCallback {
                Log.d("TAG", "map loaded")
                enableMyLocationLayer(locationViewModel)
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM_LEVEL))
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
            listener?.selectImage()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        Log.d("TAG", "onPause")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(CAMERA_POSITION, map.cameraPosition)
        outState.putParcelable(LAST_KNOWN_LOCATION, map.cameraPosition.target)
        super.onSaveInstanceState(outState)
    }

    private fun enableMyLocationLayer(viewModel: LocationViewModel) {
        val permission = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.ACCESS_FINE_LOCATION)
        if (ActivityCompat.checkSelfPermission(context!!, permission[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context!!, permission[1]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, LOCATION_REQUEST_CODE)
            Log.d("TAG", "request for permission")

        } else {
            Log.d("TAG", "permission granted before.")
            map.isMyLocationEnabled = true
            viewModel.getLastKnownLocation(this@MapFragment)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            Log.d("TAG", "permission granted.")
            enableMyLocationLayer(locationViewModel)
        } else {
            Log.d("TAG", "permission denied.")
            Toast.makeText(context, "Permission is needed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResultReadyCallBack(location: Location) {
        val position = LatLng(location.latitude, location.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM_LEVEL))
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
}
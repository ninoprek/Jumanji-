package jumanji.sda.com.jumanji

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment() {
    companion object {
        const val DEFAULT_ZOOM_LEVEL = 13.5f
        private const val DEFAULT_LATITUDE = 59.3498065f
        private const val DEFAULT_LONGITUDE = 18.0684759f
        private const val CAMERA_LATITUDE = "camera_latitude"
        private const val CAMERA_LONGITUDE = "camera_longitude"
        private const val CAMERA_ZOOM = "camera_zoom"
        private const val CAMERA_TILT = "camera_tilt"
        private const val CAMERA_BEARING = "camera_bearing"
        private const val CAMERA_PREFERENCE = "camera_preference"
    }

    private lateinit var map: GoogleMap
    private lateinit var mapCameraManager: MapCameraManager

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



        mapCameraManager = MapCameraManager()
        val cameraPosition = mapCameraManager.getCameraState()
        mapView.getMapAsync {
            map = it
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            val currentLocation: LatLng? = LatLng(DEFAULT_LATITUDE.toDouble(), DEFAULT_LONGITUDE.toDouble()) // TODO: To get from GPS for current user location
            if (currentLocation != null) {
                val currentPositionMarker = MarkerOptions()
                currentPositionMarker.title("You are here!")
                currentPositionMarker.position(currentLocation)
                currentPositionMarker.alpha(0.5f)
                Log.d("TAG", "default Location")
                map.addMarker(currentPositionMarker)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM_LEVEL))
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

            button.setOnClickListener {
                trashLocationViewModel.add()
            }

            reportFab.setOnClickListener {
                listener?.selectImage()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (this::mapCameraManager.isInitialized) {
            mapCameraManager.saveMapCameraState()
        }
        mapView.onPause()
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
                    .forEach {
                        it.isVisible = false
                    }
        }

        private fun getCurrentView(): LatLngBounds {
            return map!!.projection.visibleRegion.latLngBounds
        }
    }

    inner class MapCameraManager {
        private val mapCameraPreferences = context!!.getSharedPreferences(CAMERA_PREFERENCE, Context.MODE_PRIVATE)

        fun saveMapCameraState() {
            val cameraPosition = map.cameraPosition
            val latitude = cameraPosition.target.latitude.toFloat()
            val longitude = cameraPosition.target.longitude.toFloat()
            val zoom = cameraPosition.zoom
            val tilt = cameraPosition.tilt
            val bearing = cameraPosition.bearing
            val editor = mapCameraPreferences?.edit()
            editor?.putFloat(CAMERA_LATITUDE, latitude)
            editor?.putFloat(CAMERA_LONGITUDE, longitude)
            editor?.putFloat(CAMERA_ZOOM, zoom)
            editor?.putFloat(CAMERA_TILT, tilt)
            editor?.putFloat(CAMERA_BEARING, bearing)
            editor?.apply()
        }

        fun getCameraState(): CameraPosition {
            val latitude = mapCameraPreferences.getFloat(CAMERA_LATITUDE, DEFAULT_LATITUDE).toDouble()
            val longitude = mapCameraPreferences.getFloat(CAMERA_LONGITUDE, DEFAULT_LONGITUDE).toDouble()
            val zoom = mapCameraPreferences.getFloat(CAMERA_ZOOM, DEFAULT_ZOOM_LEVEL)
            val tilt = mapCameraPreferences.getFloat(CAMERA_TILT, 0.0f)
            val bearing = mapCameraPreferences.getFloat(CAMERA_BEARING, 0.0f)
            return CameraPosition(LatLng(latitude, longitude), zoom, tilt, bearing)
        }
    }
}
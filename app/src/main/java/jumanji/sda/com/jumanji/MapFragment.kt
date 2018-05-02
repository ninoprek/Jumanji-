package jumanji.sda.com.jumanji

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.fragment_map.*
import com.google.firebase.auth.FirebaseAuth


class MapFragment : Fragment() {
    companion object {
        const val DEFAULT_ZOOM_LEVEL = 13.5f
        private const val DEFAULT_LATITUDE = 59.3498065f
        private const val DEFAULT_LONGITUDE = 18.0684759f
        private const val LAST_KNOWN_LOCATION = "last_known_location"
        private const val CAMERA_POSITION = "camera_position"
    }

    private lateinit var map: GoogleMap
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

        var cameraPosition: CameraPosition? = null
        var currentLocation: LatLng? = null
        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION)
            currentLocation = savedInstanceState.getParcelable(LAST_KNOWN_LOCATION)
        } else {
            currentLocation = LatLng(DEFAULT_LATITUDE.toDouble(), DEFAULT_LONGITUDE.toDouble()) // TODO: To get from GPS for current user location
        }

        mapView.getMapAsync {
            map = it

            if (cameraPosition != null) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            } else {
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

            reportFab.setOnClickListener {
                listener?.selectImage()
            }

            updateGPSFab.setOnClickListener {
                val user=FirebaseAuth.getInstance().currentUser?.displayName
                val profileViewModel = ProfileViewModel()
                profileViewModel.signOut()
                Snackbar.make(it, "${user}, you are signed out", Snackbar.LENGTH_SHORT).show()
            }

            addPin.setOnClickListener{
                val pinViewModel: PinViewModel = PinViewModel()
                pinViewModel.testSavePinData()
                Snackbar.make(it, "Pin has been added!", Snackbar.LENGTH_SHORT).show()
            }

            deletePin.setOnClickListener {
                val view  = it
                val pinViewModel = ViewModelProviders.of(this)[PinViewModel::class.java]
                //pinViewModel.deletePinData("1")
                //Snackbar.make(it, "Pin has been deleted!",Snackbar.LENGTH_SHORT).show()

                pinViewModel.testGetPinData()

                pinViewModel.pinData?.observe(this, Observer {
                    Snackbar.make(view, "Here is the pin: $it", Snackbar.LENGTH_SHORT).show()
                })


            }
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(CAMERA_POSITION, map.cameraPosition)
        outState.putParcelable(LAST_KNOWN_LOCATION, map.cameraPosition.target)
        super.onSaveInstanceState(outState)
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
}
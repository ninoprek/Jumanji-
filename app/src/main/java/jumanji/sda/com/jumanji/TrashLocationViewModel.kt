package jumanji.sda.com.jumanji

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlin.math.abs

class TrashLocationViewModel : ViewModel() {
    lateinit var map: GoogleMap
    private val locations: MutableLiveData<List<LatLng>> = MutableLiveData() //TODO: receive location data from database
    private val trashFreeLocations: MutableLiveData<List<LatLng>> = MutableLiveData()//TODO: receive location data from database
    val trashMarkers: LiveData<List<Marker>> = Transformations
            .map(locations) {
                it.map { map.addMarker(MarkerOptions().position(it).visible(false)) }
            }
    val trashFreeMarkers: LiveData<List<Marker>> = Transformations
            .map(trashFreeLocations) {
                it.map {
                    map.addMarker(
                            MarkerOptions()
                                    .position(it)
                                    .icon(BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_GREEN))
                                    .visible(false))
                }
            }

    private val factorToExpandLatLngBoundsForQuery = 0.21
    private lateinit var previousViewForQuery: LatLngBounds
    private var previousCameraZoom: Float = MapFragment.DEFAULT_ZOOM_LEVEL

    private fun getLatLngBoundsForQuery(latLngBounds: LatLngBounds): LatLngBounds {
        val boundsForQuery = latLngBounds.including(LatLng(
                latLngBounds.southwest.latitude - factorToExpandLatLngBoundsForQuery,
                latLngBounds.southwest.longitude - factorToExpandLatLngBoundsForQuery))
        return boundsForQuery.including(LatLng(
                boundsForQuery.northeast.latitude + factorToExpandLatLngBoundsForQuery,
                boundsForQuery.northeast.longitude + factorToExpandLatLngBoundsForQuery))
    }

    fun loadLocations(latLngBounds: LatLngBounds, isRefresh: Boolean) {
        val latLngBoundsForQuery = getLatLngBoundsForQuery(latLngBounds)
        if (isRefresh) {
            map.clear()
            previousCameraZoom = map.cameraPosition.zoom
            previousViewForQuery = latLngBoundsForQuery
            loadTrashLocations(latLngBoundsForQuery)
            loadTrashFreeLocations(latLngBoundsForQuery)
        } else {
            if (!this::previousViewForQuery.isInitialized ||
                    abs(previousViewForQuery.center.latitude -
                            latLngBoundsForQuery.center.latitude) > factorToExpandLatLngBoundsForQuery ||
                    previousCameraZoom > map.cameraPosition.zoom) {
                map.clear()
                previousCameraZoom = map.cameraPosition.zoom
                previousViewForQuery = latLngBoundsForQuery
                loadTrashLocations(latLngBoundsForQuery)
                loadTrashFreeLocations(latLngBoundsForQuery)
            }
        }

    }

    private fun loadTrashLocations(latLngBounds: LatLngBounds) {
        //TODO: call method in repository to update locations
        //locations = repository.loadTrashLocations(latLngBoundsForQuery)
    }

    private fun loadTrashFreeLocations(latLngBounds: LatLngBounds) {
        //TODO: call method in repository to update locations
        //locations = repository.loadTrashFreeLocations
    }
}



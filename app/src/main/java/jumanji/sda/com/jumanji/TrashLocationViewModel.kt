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

    private val repository = Repository()

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
        locations.postValue(repository.loadLocations(latLngBounds))
        //TODO: call method in repository to update locations
        //locations = repository.loadTrashLocations(latLngBoundsForQuery)
    }

    private fun loadTrashFreeLocations(latLngBounds: LatLngBounds) {
        trashFreeLocations.postValue((repository.loadTrashFreeLocations(latLngBounds)))
        //TODO: call method in repository to update locations
        //locations = repository.loadTrashFreeLocations
    }

    fun add() {
        map.clear()
        locations.postValue(listOf(
                LatLng(59.3563219, 18.0735219),
                LatLng(59.3463219, 18.0935219),
                LatLng(59.2563219, 18.0935219),
                LatLng(59.3663219, 18.0535219),
                LatLng(59.3863219, 18.0435219),
                LatLng(59.2863219, 18.0335219),
                LatLng(59.3363219, 18.0235219),
                LatLng(59.3263219, 18.0235219),
                LatLng(59.3163219, 18.0835219)))

        trashFreeLocations.postValue(listOf(
                LatLng(59.3463219, 18.0735219),
                LatLng(59.2563219, 18.0835219),
                LatLng(59.3663219, 18.0935219),
                LatLng(59.3863219, 18.0535219),
                LatLng(59.2863219, 18.0435219),
                LatLng(59.3363219, 18.0335219),
                LatLng(59.3163219, 18.0135219)))
    }
}

class Repository {
    private val locations = listOf(
            LatLng(59.3370304, 18.0687083),
            LatLng(59.3457228, 18.0269944))

    private val trashFreeLocations = listOf(
            LatLng(59.3385767, 18.0530063),
            LatLng(59.3690764, 17.6947272))

    fun loadLocations(latLngBounds: LatLngBounds): List<LatLng> {
        return locations.filter { latLngBounds.contains(it) }
    }

    fun loadTrashFreeLocations(latLngBounds: LatLngBounds): List<LatLng> {
        return trashFreeLocations.filter { latLngBounds.contains(it) }
    }
}


package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs

class PinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PinRepository(application)
    lateinit var map: GoogleMap
    private val factorToExpandLatLngBoundsForQuery = 0.21
    private var previousCameraZoom = LocationViewModel.DEFAULT_ZOOM_LEVEL

    private val pinDataCache: MutableLiveData<List<PinData>> = repository.pinDataAll

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

    private lateinit var previousViewForQuery: LatLngBounds
    val userPinData: MutableLiveData<List<PinData>> = repository.userPinData
    val pinDataAll: MutableLiveData<List<PinData>> = repository.pinDataAll

    fun loadLocations(latLngBounds: LatLngBounds?, isRefresh: Boolean) {
        if (latLngBounds != null) {
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
    }

    private fun getLatLngBoundsForQuery(latLngBounds: LatLngBounds): LatLngBounds {
        val boundsForQuery = latLngBounds.including(LatLng(
                latLngBounds.southwest.latitude - factorToExpandLatLngBoundsForQuery,
                latLngBounds.southwest.longitude - factorToExpandLatLngBoundsForQuery))
        return boundsForQuery.including(LatLng(
                boundsForQuery.northeast.latitude + factorToExpandLatLngBoundsForQuery,
                boundsForQuery.northeast.longitude + factorToExpandLatLngBoundsForQuery))
    }

    private fun loadTrashLocations(latLngBounds: LatLngBounds) {

    }

    private fun loadTrashFreeLocations(latLngBounds: LatLngBounds) {
        //TODO: call method in repository to update locations
        //locations = repository.loadTrashFreeLocations
    }

    fun testSavePinData() {

        Single.fromCallable { repository.storeAllPinsFromFirebaseToRoom() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun testGetPinData() {
        Single.fromCallable { repository.getAllPinsFromRoom() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun getPinData(user: String) {
        return repository.getUserPins(user)
    }

    fun deletePinData(pinId: String) {
        Single.fromCallable { repository.deletePinFromDatabase(pinId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }
}
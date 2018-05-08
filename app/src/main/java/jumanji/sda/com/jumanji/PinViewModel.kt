package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Log
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

    var pinDataCache: LiveData<List<PinData>> = repository.loadPinsWithBounds()

    private val trashFreeLocations: MutableLiveData<List<LatLng>> = MutableLiveData()//TODO: receive location data from database
    val trashMarkers: LiveData<List<Marker>> = Transformations
            .map(pinDataCache) {pinDataList ->
                pinDataList.map {pinData ->
                    val position = LatLng(pinData.latitude.toDouble(), pinData.longitude.toDouble())
                    val marker = map.addMarker(MarkerOptions().position(position).visible(false))
                    marker.tag = pinData.imageURL
                    return@map marker
                }
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
                loadTrashLocations()
                loadTrashFreeLocations(latLngBoundsForQuery)
            } else {
                if (!this::previousViewForQuery.isInitialized ||
                        abs(previousViewForQuery.center.latitude -
                                latLngBoundsForQuery.center.latitude) > factorToExpandLatLngBoundsForQuery ||
                        previousCameraZoom > map.cameraPosition.zoom) {
                    map.clear()
                    previousCameraZoom = map.cameraPosition.zoom
                    previousViewForQuery = latLngBoundsForQuery
                    loadTrashLocations()
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

    private fun loadTrashLocations() : LiveData<List<PinData>> {
        return repository.loadPinsWithBounds()
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
        return repository.getUserPinsFromRoom(user)
    }

    fun deletePinData(pinId: String) {
        Single.fromCallable { repository.deletePinFromFirebase(pinId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }
}
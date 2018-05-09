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

    lateinit var pinDataCache: LiveData<List<PinData>>

    lateinit var trashMarkers: LiveData<List<Marker>>

    lateinit var trashFreeMarkers: LiveData<List<Marker>>
//            Transformations
//            .map(trashFreeLocations) {
//                it.map {
//                    map.addMarker(
//                            MarkerOptions()
//                                    .position(it)
//                                    .icon(BitmapDescriptorFactory.defaultMarker(
//                                            BitmapDescriptorFactory.HUE_GREEN))
//                                    .visible(false))
//                }
//            }

    private lateinit var previousViewForQuery: LatLngBounds
    val userPinData: MutableLiveData<List<PinData>> = repository.userPinData
    val pinDataAll: MutableLiveData<List<PinData>> = repository.pinDataAll

    fun loadTrashLocations() {
        repository.storeAllPinsFromFirebaseToRoom()
        pinDataCache = repository.loadAllPins()

        trashMarkers = Transformations
        .map(pinDataCache) {pinDataList ->
            pinDataList.map {pinData ->
                val position = LatLng(pinData.latitude.toDouble(), pinData.longitude.toDouble())
                val marker = map.addMarker(MarkerOptions().position(position).visible(false).visible(false))
                marker.tag = pinData.imageURL
                return@map marker
            }
        }
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
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

class PinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PinRepository(application)
    lateinit var map: GoogleMap
    private val factorToExpandLatLngBoundsForQuery = 0.21

    private lateinit var trashPinData: LiveData<List<PinData>>
    private lateinit var trashFreePinData: LiveData<List<PinData>>

    lateinit var trashMarkers: LiveData<List<Marker>>
    lateinit var trashFreeMarkers: LiveData<List<Marker>>

    private lateinit var previousViewForQuery: LatLngBounds
    val userPinData: MutableLiveData<List<PinData>> = repository.userPinData
    val pinDataAll: MutableLiveData<List<PinData>> = repository.pinDataAll

    fun loadTrashLocations() {
        repository.storeAllPinsFromFirebaseToRoom()
        trashPinData = repository.loadAllTrashPins(true)
        trashFreePinData = repository.loadAllTrashPins(false)

        trashMarkers = Transformations.map(trashPinData) { pinDataList ->
            pinDataList.map { pinData ->
                val position = LatLng(pinData.latitude.toDouble(), pinData.longitude.toDouble())
                val marker = map.addMarker(MarkerOptions().position(position).visible(false).visible(false))
                marker.tag = pinData.imageURL
                return@map marker
            }
        }

        trashFreeMarkers = Transformations.map(trashFreePinData) { pinDataList ->
            pinDataList.map { pinData ->
                val position = LatLng(pinData.latitude.toDouble(), pinData.longitude.toDouble())
                val marker = map.addMarker(
                        MarkerOptions()
                                .position(position)
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN))
                                .visible(false))
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
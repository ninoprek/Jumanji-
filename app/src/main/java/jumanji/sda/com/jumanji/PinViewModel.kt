package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PinRepository(application)
    lateinit var map: GoogleMap

    private lateinit var trashPinData: LiveData<List<PinData>>
    private lateinit var trashFreePinData: LiveData<List<PinData>>

    lateinit var trashMarkers: LiveData<List<Marker>>
    lateinit var trashFreeMarkers: LiveData<List<Marker>>

    val userPinData: MutableLiveData<List<PinData>> = repository.userPinData
    val pinDataAll: MutableLiveData<List<PinData>> = repository.pinDataAll

    fun loadPinData() {
        repository.storeAllPinsFromFirebaseToRoom()
        transformPinDataToTrashMarker()
        transformPinDataToNonTrashMarker()
    }

    private fun transformPinDataToTrashMarker() {
        trashPinData = repository.loadAllTrashPins(true)
        trashMarkers = Transformations.map(trashPinData) { pinDataList ->
            pinDataList.map { pinData ->
                val position = LatLng(pinData.latitude.toDouble(), pinData.longitude.toDouble())
                val marker = map.addMarker(MarkerOptions().position(position).visible(false).visible(false))
                marker.tag = pinData
                return@map marker
            }
        }
    }

    private fun transformPinDataToNonTrashMarker() {
        trashFreePinData = repository.loadAllTrashPins(false)
        trashFreeMarkers = Transformations.map(trashFreePinData) { pinDataList ->
            pinDataList.map { pinData ->
                val position = LatLng(pinData.latitude.toDouble(), pinData.longitude.toDouble())
                val marker = map.addMarker(
                        MarkerOptions()
                                .position(position)
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN))
                                .visible(false))
                marker.tag = pinData
                return@map marker
            }
        }
    }

    fun queryDataFromFirebaseToRoom() {
        Single.fromCallable { repository.storeAllPinsFromFirebaseToRoom() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun reportPointForTrash(pinDataInfo: PinDataInfo) {
        Single.fromCallable { repository.storePinToFirebase(pinDataInfo) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun reportPointAsClean(pinData: PinData) {
        Single.fromCallable { repository.reportPinAsCleanToFirebase(pinData.pinId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("TAG", "Successfully update firebase")
                }, {
                    Log.d("TAG", "something went wrong when updating firebase, ${it.message}")
                })

        Single.fromCallable { repository.deletePinFromRoom(pinData) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("TAG", "Successfully deleted data in room")
                }, {
                    Log.d("TAG", "something went wrong when deleting data in room, ${it.message}")
                })
    }
}

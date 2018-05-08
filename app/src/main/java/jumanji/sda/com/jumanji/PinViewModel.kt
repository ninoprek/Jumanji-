package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PinViewModel (application: Application) : AndroidViewModel(application) {
    private val repository = PinRepository(application)
    val userPinData: MutableLiveData<List<PinData>> = repository.userPinData
    val pinDataAll: MutableLiveData<List<PinData>> = repository.pinDataAll

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

    fun getPinData(user : String) {

        return repository.getUserPinsFromRoom(user)
    }

    fun deletePinData(pinId: String) {
        Single.fromCallable { repository.deletePinFromDatabase(pinId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }
}
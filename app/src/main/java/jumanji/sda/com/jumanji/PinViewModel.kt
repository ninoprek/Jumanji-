package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PinViewModel (application: Application) : AndroidViewModel(application) {
    private val repository = PinRepository(application)
    val pinData: MutableLiveData<PinDataInfo>? = repository.pinDataTemp
    val pinDataAll: MutableLiveData<List<PinData>> = repository.pinDataAll

    fun testSavePinData() {
        Single.fromCallable { repository.storeFromFirebaseToRoom() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun testGetPinData() {
        Single.fromCallable { repository.testGetPinFromDatabase() }
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe()
    }

    fun getPinData(pinId : String) {

        return repository.getPinFromDatabase(pinId)
    }

    fun deletePinData(pinId: String) {
        Single.fromCallable { repository.deletePinFromDatabase(pinId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }
}
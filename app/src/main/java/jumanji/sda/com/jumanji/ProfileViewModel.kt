package jumanji.sda.com.jumanji

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileViewModel : ViewModel() {

    private val repository = UserRepository()
    private val profile: UserProfile? = getUserProfile()

    val userInfo: MutableLiveData<UserProfile>? = repository.userInfo

    val userName = profile?.userName
    val email = profile?.email
    val uri = profile?.pictureURI

    fun saveUserProfile(profile: UserProfile) {
        Single.fromCallable { repository.createNewUser(profile) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun getUserProfile(context: Context) {
        /*Single.fromCallable { repository.getUserInformation(context) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()*/

        repository.getUserInformation(context)
    }

    fun updateUserProfile(profile: UserProfile) {
        Single.fromCallable { repository.updateUserInformation(profile) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun getUserProfile() : UserProfile? {
        return null //repository.retrieveUserFromDatabase()
    }

    fun signOut(){
        Single.fromCallable { repository.userSignOut() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun deleteUserProfile() : Boolean {
        val result = Single.fromCallable { repository.userDelete() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        Log.d(javaClass.simpleName, "DISPOSABLE result: " + result.isDisposed)
        return result.isDisposed.equals(true)
    }
}

class PinViewModel : ViewModel() {
    private val repository = PinRepository()
    val pinData: MutableLiveData<PinData>? = repository.pinData

    fun testSavePinData(user: String) {
        Single.fromCallable { repository.testPinWriteFunction(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun testGetPinData() {
       /* Single.fromCallable { repository.testGetPinFromDatabase(view) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()*/

        repository.testGetPinFromDatabase()
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

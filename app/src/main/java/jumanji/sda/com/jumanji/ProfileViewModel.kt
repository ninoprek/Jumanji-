package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepository(application)

    val reportedPins: MutableLiveData<String> = repository.reportedPins
    val cleanedPins: MutableLiveData<String> = repository.cleanedPins
    val userInfo: MutableLiveData<UserProfile>? = repository.userInfo

    fun saveUserProfile(profile: UserProfile, callback: OnNewUserRegisteredCallback) {
        Single.fromCallable { repository.createNewUser(profile, callback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun checkIfUserSignedIn(context: Context) {
        Single.fromCallable { repository.signOut(context) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun initializeUserPinNumber(user: String) {
        Single.fromCallable { repository.initializeUserPinNumber(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun updateUserPinNumber(user: String) {
        Single.fromCallable { repository.updateUserPinNumber(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun updateUserCleanedPinNumber(user: String) {
        Single.fromCallable { repository.updateUserCleanedPinNumber(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun updateUserStatistics(user: String) {
        Single.fromCallable { repository.updateUserStatistics(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }
}



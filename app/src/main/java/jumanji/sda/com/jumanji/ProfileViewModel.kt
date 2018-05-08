package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)
    private val profile: UserProfile? = getUserProfile()

    val reportedPins: MutableLiveData<String> = repository.reportedPins
    val cleanedPins: MutableLiveData<String> = repository.cleanedPins
    val userInfo: MutableLiveData<UserProfile>? = repository.userInfo

    val userName = profile?.userName
    val email = profile?.email
    val uri = profile?.photoURL

    fun saveUserProfile(profile: UserProfile) {
        Single.fromCallable { repository.createNewUser(profile) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
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

    fun initializeUserPinNumber (user: String) {
        Single.fromCallable { repository.initializeUserPinNumber(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun updateUserPinNumber (user: String) {
        Single.fromCallable { repository.updateUserPinNumber(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun updateUserCleanedPinNumber (user: String) {
        Single.fromCallable { repository.updateUserCleanedPinNumber(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun updateUserStatistics (user: String) {
        Single.fromCallable { repository.updateUserStatistics(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }
}



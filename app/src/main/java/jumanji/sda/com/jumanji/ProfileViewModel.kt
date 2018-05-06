package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)
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



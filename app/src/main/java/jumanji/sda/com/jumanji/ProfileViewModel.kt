package jumanji.sda.com.jumanji

import android.arch.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileViewModel : ViewModel() {

    private val repository = UserProfileRepository()
    private val profile: UserProfile? = getUserProfile()

    val userName = profile?.userName
    val email = profile?.email
    val uri = profile?.pictureURI

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
        email?.let { return repository.retriveUserProfileFromDatabase(email) }
        return null
    }

    fun deleteUserProfile(profile: UserProfile) {
        //repository.deleteProfile(profile)
    }

    fun editUserProfile(){

    }

    fun signOut(){}

}

package jumanji.sda.com.jumanji

import android.arch.lifecycle.ViewModel
import io.reactivex.Single

class CreateProfileViewModel : ViewModel() {

    private val repository = UserProfileRepository()
    private val profile: UserProfile? = getUserProfile()

    val userName = profile?.userName
    val email = profile?.email
    val uri = profile?.pictureURI

    fun saveUserProfile(profile: UserProfile): Single<Unit>? {
        return Single.fromCallable { repository.storeToDatabase(profile) }
    }

    fun getUserProfile() : UserProfile? {
        return null //repository.retrieveUserFromDatabase()
    }

}

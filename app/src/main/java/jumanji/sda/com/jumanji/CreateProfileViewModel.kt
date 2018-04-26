package jumanji.sda.com.jumanji

import android.arch.lifecycle.ViewModel
import io.reactivex.Single

class CreateProfileViewModel : ViewModel() {

    private val repository = UserProfileRepository()

    fun saveUserProfile(profile: UserProfile)  {
          repository.storeToDatabase(profile)
    }

    fun getUserProfile() : UserProfile? {
        return null //repository.retrieveUserFromDatabase()
    }

}

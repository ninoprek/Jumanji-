package jumanji.sda.com.jumanji

import android.location.Location

data class UserProfile(
    val userName: String,
    val email: String,
    val pictureURI: String,
    val location: Location
    )

class UserProfileRepository {

}
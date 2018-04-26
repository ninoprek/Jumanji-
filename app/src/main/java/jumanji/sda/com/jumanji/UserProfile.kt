package jumanji.sda.com.jumanji

import com.google.firebase.firestore.FirebaseFirestore
import android.support.annotation.NonNull
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.OnSuccessListener

data class UserProfile(
        val userName: String,
        val email: String,
        val pictureURI: String
        )

class UserProfileRepository {
    companion object {
        private const val TAG = "write to database"
    }

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun storeToDatabase(userProfile: UserProfile) {

        var user: HashMap<String, Any> = HashMap()
        user.put("userName", userProfile.userName)
        user.put("email", userProfile.email)
        user.put("pictureURI", userProfile.pictureURI)

        database.collection("userProfiles").document(user.get("userName") as String)
                .set(user)
    }
}
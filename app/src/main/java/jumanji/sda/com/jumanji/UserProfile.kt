package jumanji.sda.com.jumanji

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.OnCompleteListener

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

        database.collection("userProfiles").document(user.get("email") as String)
                .set(user)
    }

    fun retriveUserProfileFromDatabase(email: String) : UserProfile? {

        var userProfile: UserProfile? = null
        val documentReference = database.collection("userProfiles").document(email)

        documentReference.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.data!!)

                     userProfile = UserProfile(document.data!!["userName"].toString()
                            ,document.data!!["email"].toString(),
                            document.data!!["pictureURI"].toString())
                } else {
                    Log.d(TAG, "No such document")
                }
            } else {
                Log.d(TAG, "get failed with ", task.exception)
            }
        })
        return userProfile
    }
}
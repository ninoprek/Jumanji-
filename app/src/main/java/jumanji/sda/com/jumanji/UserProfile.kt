package jumanji.sda.com.jumanji

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

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
    private val imageStorage: FirebaseStorage = FirebaseStorage.getInstance()

    fun storeToDatabase(userProfile: UserProfile) {

        if (userProfile.email.isEmpty()) return
        //TODO notify user that email is obligatory

        val user: HashMap<String, Any> = HashMap()
        user.put("userName", userProfile.userName)
        user.put("email", userProfile.email)
        user.put("pictureURI", userProfile.pictureURI)

        database.collection("userProfiles").document(userProfile.email)
                .set(user)
    }

    fun retriveUserProfileFromDatabase(email: String): UserProfile? {

        var userProfile: UserProfile? = null
        val documentReference = database.collection("userProfiles").document(email)

        documentReference.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.data!!)

                    userProfile = UserProfile(document.data!!["userName"].toString()
                            , document.data!!["email"].toString(),
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

    fun retrivePhotoFromRepository(userName: String) {

        val storageRef: StorageReference = imageStorage.getReference("Images").child(userName)

        storageRef.downloadUrl.addOnSuccessListener {
            // Got the download URL for 'users/me/profile.png'
            uri -> Log.d(javaClass.simpleName, "The image URI is: $uri")

        }.addOnFailureListener {
            // Handle any errors
            e -> Log.d(javaClass.simpleName, "Problem with getting image uri: $e")
        }
    }
}
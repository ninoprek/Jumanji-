package jumanji.sda.com.jumanji

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.UserProfileChangeRequest


data class UserProfile(
        val userName: String,
        val password: String,
        val email: String,
        val pictureURI: String
)

class UserProfileRepository {
    companion object {
        private const val TAG = "write to database"
    }

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userAuthentication: FirebaseAuth = FirebaseAuth.getInstance()


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

                    userProfile = UserProfile(document.data!!["userName"].toString(), document.data!!["password"].toString()
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

    fun createNewUser(userProfile: UserProfile) {
        userAuthentication.createUserWithEmailAndPassword(userProfile.email,userProfile.password)
                .addOnCompleteListener(OnCompleteListener { task ->

                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        updateUserInformation(userProfile)

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    }
                })
    }

    fun updateUserInformation(userProfile: UserProfile) {

        val user = userAuthentication.currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userProfile.userName)
                .setPhotoUri(Uri.parse(userProfile.pictureURI))
                .build()

        user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(javaClass.simpleName, "User profile is updated.")
                    } else {
                        Log.d(javaClass.simpleName, "Problem with updating the profile.")
                    }
                })
    }
}
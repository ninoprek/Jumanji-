package jumanji.sda.com.jumanji

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.*


data class UserProfile(
        val userName: String = "",
        val password: String = "",
        val email: String = "",
        val pictureURI: String = ""
)

class UserRepository (context: Context) {
    companion object {
        private const val TAG = "write to database"
    }

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userAuthentication: FirebaseAuth = FirebaseAuth.getInstance()
    val reportedPins: MutableLiveData<String> = MutableLiveData()
    val cleanedPins: MutableLiveData<String> = MutableLiveData()

    val userInfo: MutableLiveData<UserProfile> = MutableLiveData()

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

    fun getUserInformation(context: Context) {
        if (userAuthentication.currentUser?.displayName != null) {
            userInfo.value = UserProfile(userAuthentication.currentUser?.displayName.toString(),
                    "",
                    userAuthentication.currentUser?.email.toString(),
                    userAuthentication.currentUser?.photoUrl.toString())
        } else {

            val acct = GoogleSignIn.getLastSignedInAccount(context)

            userInfo.value = UserProfile(acct?.givenName.toString(),
                    "",
                    acct?.email.toString(),
                    acct?.photoUrl.toString())
        }
    }

    fun createNewUser(userProfile: UserProfile) {
        userAuthentication.createUserWithEmailAndPassword(userProfile.email,userProfile.password)
                .addOnCompleteListener({ task ->
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
                ?.addOnCompleteListener( { task ->
                    if (task.isSuccessful) {
                        Log.d(javaClass.simpleName, "User profile is updated.")
                    } else {
                        Log.d(javaClass.simpleName, "Problem with updating the profile.")
                    }
                })
    }

    fun userSignOut() {
        userAuthentication.signOut()
    }

    fun userDelete() : Boolean {

        val user = userAuthentication.currentUser
        var deleted = false

        user?.delete()
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        deleted = true
                    }
                }
        return  deleted
    }

    fun initializeUserPinNumber(user: String) {

        val userPins: HashMap<String, Any> = HashMap()
        userPins.put("reportedPins", 0)
        userPins.put("cleanedPins", 0)

        database.collection("userStatistics").document(user).set(userPins)
        updateUserStatistics(user)
    }

    fun updateUserPinNumber (user: String) {

        val documentReference = database.collection("userStatistics").document(user)

        documentReference.get().addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    Log.d(javaClass.simpleName, "User statistics data: " + document.data!!)

                    val reportedPins = document["reportedPins"].toString().toInt() + 1
                    documentReference.update("reportedPins", reportedPins)
                    updateUserStatistics(user)

                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "get failed with " + task.exception)
            }
        })
    }

    fun updateUserCleanedPinNumber (user: String) {

        val documentReference = database.collection("userStatistics").document(user)

        documentReference.get().addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    Log.d(javaClass.simpleName, "User statistics data: " + document.data!!)

                    val reportedPins = document["cleanedPins"].toString().toInt() + 1
                    documentReference.update("cleanedPins", reportedPins)
                    updateUserStatistics(user)

                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "get failed with " + task.exception)
            }
        })
    }

    fun updateUserStatistics(user: String){
        val documentReference = database.collection("userStatistics").document(user)

        documentReference.get().addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {

                    cleanedPins.postValue(document["cleanedPins"].toString())
                    reportedPins.postValue(document["reportedPins"].toString())

                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "Update statistics failed" + task.exception)
            }
        })
    }
}


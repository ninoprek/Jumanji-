package jumanji.sda.com.jumanji

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

data class UserProfile(
        val userName: String = "",
        val password: String = "",
        val email: String = "",
        val photoURL: String = ""
)

class ProfileRepository(context: Context) {
    companion object {
        private const val TAG = "write to database"
        const val PREFERENCE_NAME = "user_data"
        const val KEY_USER_NAME = "user_name"
        const val KEY_PASSWORD = "password"
        const val KEY_EMAIL = "email"
        const val KEY_PHOTO_URL = "photo_url"
    }

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userAuthentication: FirebaseAuth = FirebaseAuth.getInstance()
    val reportedPins: MutableLiveData<String> = MutableLiveData()
    val cleanedPins: MutableLiveData<String> = MutableLiveData()
    val userInfo: MutableLiveData<UserProfile> = MutableLiveData()

    val userSharedPref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    init {
        userSharedPref.registerOnSharedPreferenceChangeListener({ userNameSharedPref, key ->
            if (key == PREFERENCE_NAME) {
                val user = UserProfile(
                        userNameSharedPref.getString(KEY_USER_NAME, ""),
                        userNameSharedPref.getString(KEY_PASSWORD, ""),
                        userNameSharedPref.getString(KEY_EMAIL, ""),
                        userNameSharedPref.getString(KEY_PHOTO_URL, "")
                )
                userInfo.postValue(user)
            }
        })
        getUserInformation(context)
    }

    private fun getUserInformation(context: Context) {
        Log.d("TAG", "get user info")
        val currentUser = userAuthentication.currentUser
        if (currentUser != null) {
            val userProfile = UserProfile(currentUser.displayName.toString(),
                    "",
                    currentUser.email.toString(),
                    currentUser.photoUrl.toString())
            userInfo.value = userProfile
        } else {
            val acct = GoogleSignIn.getLastSignedInAccount(context)
            userInfo.value = UserProfile(acct?.givenName.toString(),
                    "",
                    acct?.email.toString(),
                    acct?.photoUrl.toString())
        }
    }

    fun storeToDatabase(userProfile: UserProfile) {
        if (userProfile.email.isEmpty()) return
        //TODO notify user that email is obligatory

        val user: HashMap<String, Any> = HashMap()
        user.put("userName", userProfile.userName)
        user.put("email", userProfile.email)
        user.put("pictureURI", userProfile.photoURL)

        database.collection("userProfiles").document(userProfile.email)
                .set(user)
    }

    fun createNewUser(userProfile: UserProfile, callback: OnNewUserRegisteredCallback, context: Context) {
        userAuthentication.createUserWithEmailAndPassword(userProfile.email, userProfile.password)
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        updateUserInformation(userProfile, callback)
                        initializeUserPinNumber(userProfile.userName)
                    } else {
                        Toast.makeText(context, "${task.exception?.message}", Toast.LENGTH_SHORT)
                                .show()
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "createUserWithEmail:failure", task.getException());
                    }
                })
    }

    fun updateUserInformation(userProfile: UserProfile, callback: OnNewUserRegisteredCallback) {

        val user = userAuthentication.currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userProfile.userName)
                .setPhotoUri(Uri.parse(userProfile.photoURL))
                .build()

        user?.updateProfile(profileUpdates)
                ?.addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        callback.onProfileSaveToFirebase()
                        Log.d(javaClass.simpleName, "User profile is updated.")
                    } else {
                        Log.d(javaClass.simpleName, "Problem with updating the profile.")
                    }
                })
    }

    fun signOut(context: Context) {

        val user = FirebaseAuth.getInstance().currentUser
        val googleUser = GoogleSignIn.getLastSignedInAccount(context)

        if (user != null) {
            userAuthentication.signOut()

        } else if (googleUser != null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            val client = GoogleSignIn.getClient(context, gso)
            client.signOut()
        }
    }

    fun userDelete(): Boolean {

        val user = userAuthentication.currentUser
        var deleted = false

        user?.delete()
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        deleted = true
                    }
                }
        return deleted
    }

    fun initializeUserPinNumber(user: String) {
        Log.d("TAG", "initialise user data")
        val userPins: HashMap<String, Any> = HashMap()
        userPins.put("reportedPins", 0)
        userPins.put("cleanedPins", 0)
        database.collection("userStatistics").document(user).set(userPins)
        updateUserStatistics(user)
    }

    fun updateUserPinNumber(user: String) {

        val documentReference = database.collection("userStatistics").document(user)

        documentReference.get().addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
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

    fun updateUserCleanedPinNumber(user: String) {

        val documentReference = database.collection("userStatistics").document(user)

        documentReference.get().addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val cleanedPins = document["cleanedPins"].toString().toInt() + 1
                    documentReference.update("cleanedPins", cleanedPins)
                    updateUserStatistics(user)
                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "get failed with " + task.exception)
            }
        })
    }

    fun updateUserStatistics(user: String) {
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
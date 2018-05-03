package jumanji.sda.com.jumanji

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class PinData(val longitude: Double = 0.0, val latitude: Double = 0.0, val imageURL: String = "", val pinId: String = "")

class PinRepository {
    companion object {
        private final val TAG = "Log tag"
    }

    private val database = FirebaseFirestore.getInstance()
    var pinData: MutableLiveData<PinData> = MutableLiveData()

    fun storePinToDatabase(pinData: PinData, user: String) {

        val pinInfo: HashMap<String, Any> = HashMap()
        pinInfo.put("longitude", pinData.longitude)
        pinInfo.put("latitude", pinData.latitude)
        pinInfo.put("imageURL", pinData.imageURL)
        pinInfo.put("pinId", pinData.pinId)

        database.collection(user).document(pinData.pinId)
                .set(pinInfo)
    }

    fun getPinFromDatabase(pinId: String) {

        val userName = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        Log.d(javaClass.simpleName, "The name that is searched: $userName and pinId: $pinId")
        val documentReference = database.collection(userName).document(pinId)

        documentReference.get().addOnCompleteListener( { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    Log.d(javaClass.simpleName, "DocumentSnapshot data: " + document.data!!)

                    pinData.value = PinData(document["longitude"].toString().toDouble(), document["latitude"].toString().toDouble(),
                            document["imageURL"].toString(), document["pinId"].toString())

                    Log.d(javaClass.simpleName, "pinData: ${pinData.value}")

                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "get failed with " + task.exception)
            }
        })
    }

    fun deletePinFromDatabase(pinId: String) {

        val documentReference = database.collection(FirebaseAuth.getInstance().currentUser?.displayName.toString()).document(pinId)
         if (documentReference.delete().isSuccessful) {
             Log.d(javaClass.simpleName, "Pin with id: $pinId has been deleted")
         } else {
             Log.d(javaClass.simpleName, "Pin with id: $pinId was not been deleted")
         }
    }

    fun testPinWriteFunction(user: String) {
        val pin1 = PinData(59.522433, 17.917423, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "1")
        val pin2 = PinData(60.522433, 18.917423, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "2")
        storePinToDatabase(pin1, user)
        storePinToDatabase(pin2, user)

        val pin3 = PinData(61.522433, 19.917423, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "3")
        val pin4 = PinData(62.522433, 20.917423, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "4")
        storePinToDatabase(pin3, user)
        storePinToDatabase(pin4, user)
    }

    fun testGetPinFromDatabase(){

        val testPin = getPinFromDatabase("3")
    }
}
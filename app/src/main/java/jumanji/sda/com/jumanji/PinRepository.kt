package jumanji.sda.com.jumanji

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class PinData(val longitude: Double = 0.0, val latitude: Double = 0.0, val imageURL: String = "", val pinId: String = "")

class PinRepository {
    companion object {
        private final val TAG = ""
    }

    private val database = FirebaseFirestore.getInstance()

    fun storePinToDatabase(pinData: PinData) {

        val pinInfo: HashMap<String, Any> = HashMap()
        pinInfo.put("longitude", pinData.longitude)
        pinInfo.put("latitude", pinData.latitude)
        pinInfo.put("imageURL", pinData.imageURL)
        pinInfo.put("pinId", pinData.pinId)

        database.collection(FirebaseAuth.getInstance().currentUser?.displayName.toString()).document(pinData.pinId)
                .set(pinInfo)
    }

    fun getPinFromDatabase(pinId: String): PinData {

        var pinData: PinData? = null
        val documentReference = database.collection(FirebaseAuth.getInstance().currentUser?.displayName.toString()).document(pinId)

        documentReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    Log.d(javaClass.simpleName, "DocumentSnapshot data: " + document.data!!)

                   pinData = PinData(document["longitude"].toString().toDouble(), document["latitude"].toString().toDouble(),
                           document["imageURL"].toString(), document["pinId"].toString())
                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "get failed with " + task.exception)
            }
        }
        return pinData!!
    }

    fun deletePinFromDatabase(pinId: String) {

        val documentReference = database.collection(FirebaseAuth.getInstance().currentUser?.displayName.toString()).document(pinId)
         if (documentReference.delete().isSuccessful) {
             Log.d(javaClass.simpleName, "Pin with id: $pinId has been deleted")
         } else {
             Log.d(javaClass.simpleName, "Pin with id: $pinId was not been deleted")
         }
    }

    fun testPinWriteFunction() {
        val pin1 = PinData(59.522433, 17.917423, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "1")
        val pin2 = PinData(59.522433, 17.917423, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "2")
        storePinToDatabase(pin1)
        storePinToDatabase(pin2)
    }
}
package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


data class PinDataInfo(val longitude: Float = 0.0f, val latitude: Float = 0.0f, val imageURL: String = "", val pinId: String = "")

class PinRepository(application: Application) {

    private var roomPinDb = Room.databaseBuilder(application,
            AppDatabase::class.java, "database-name")
            .fallbackToDestructiveMigration()
            .build()

    private val database = FirebaseFirestore.getInstance()
    var userPinData: MutableLiveData<List<PinData>> = MutableLiveData()
    var pinDataAll: MutableLiveData<List<PinData>> = MutableLiveData()

    fun storePinToFirebase(pinData: PinDataInfo, user: String) {

        val pin: HashMap<String, Any> = HashMap()
        pin.put("longitude", pinData.longitude)
        pin.put("latitude", pinData.latitude)
        pin.put("imageURL", pinData.imageURL)
        pin.put("username", user)

        database.collection("allPins").document().set(pin)
    }

//    fun getPinFromDatabase(pinId: String) {
//
//        val userName = FirebaseAuth.getInstance().currentUser?.displayName.toString()
//        Log.d(javaClass.simpleName, "The name that is searched: $userName and pinId: $pinId")
//        val documentReference = database.collection(userName).document(pinId)
//
//        documentReference.get().addOnCompleteListener({ task ->
//            if (task.isSuccessful) {
//                val document = task.result
//                if (document.exists()) {
//                    Log.d(javaClass.simpleName, "DocumentSnapshot data: " + document.data!!)
//
//                    pinDataTemp.value = PinDataInfo(document["longitude"].toString().toFloat(), document["latitude"].toString().toFloat(),
//                            document["imageURL"].toString(), document["pinId"].toString())
//
//                    Log.d(javaClass.simpleName, "pinData: ${pinDataTemp.value}")
//
//                } else {
//                    Log.d(javaClass.simpleName, "No such document")
//                }
//            } else {
//                Log.d(javaClass.simpleName, "get failed with " + task.exception)
//            }
//        })
//    }

    fun storeAllPinsFromFirebaseToRoom() {
        val firebaseDb = FirebaseFirestore.getInstance()

        firebaseDb.collection("allPins").get()
                .addOnCompleteListener ({ task ->
                    if(task.isSuccessful) {
                        val pins = task.result

                        for (document in pins) {

                            val pin = PinData(document.id,
                                    document["longitude"].toString().toFloat(),
                                    document["latitude"].toString().toFloat(),
                                    document["username"].toString(),
                                    document["imageURL"].toString()
                                    )
                            Single.fromCallable { roomPinDb.userDao().insert(pin) }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe()
                        }

                    } else {
                        Log.d(javaClass.simpleName, "Error in reading from Firebase: " + task.exception)
                    }
                })
    }

    fun deletePinFromFirebase(pinId: String) {

        val documentReference = database.collection(FirebaseAuth.getInstance().currentUser?.displayName.toString()).document(pinId)
        if (documentReference.delete().isSuccessful) {
            Log.d(javaClass.simpleName, "Pin with id: $pinId has been deleted")
        } else {
            Log.d(javaClass.simpleName, "Pin with id: $pinId was not been deleted")
        }
    }

    fun testPinWriteFunction() {
        //val pin1 = PinData(1, 59.522433.toFloat(), 17.917423.toFloat(), "ninoprek", "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700")
        //val pin2 = PinData(2, 60.522433.toFloat(), 18.917423.toFloat(), "onino", "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700")
        //storePinToDatabase(pin1, user)
        //storePinToDatabase(pin2, user)

        //roomPinDb.userDao().insert(pin1)
        //roomPinDb.userDao().insert(pin2)

        val user = "nino"

        val pin3 = PinDataInfo(61.522433f, 19.917423f, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "3")
        val pin4 = PinDataInfo(62.522433f, 20.917423f   , "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "4")
        storePinToFirebase(pin3, user)
        storePinToFirebase(pin4, user)
    }

    fun getAllPinsFromRoom() {

        val returnRoomValue = roomPinDb.userDao().getAll()
        pinDataAll.postValue(returnRoomValue)

    }

    fun getUserPinsFromRoom(user: String) {

        val returnRoomValue = roomPinDb.userDao().findTaskById(user)
        userPinData.postValue(returnRoomValue)
    }

    fun deletePinFromRoom(pinData: PinData) {
        roomPinDb.userDao().deletePinData(pinData)
    }
}

@Entity(tableName = "pinData")
data class PinData(@PrimaryKey var pinId: String,
                   @ColumnInfo(name = "longitude") var longitude: Float,
                   @ColumnInfo(name = "latitude") var latitude: Float,
                   @ColumnInfo(name = "username") var userName: String,
                   @ColumnInfo(name = "imageURL") var imageURL: String
)

@Dao
interface PinDataDao {

    @Query("SELECT * from pinData")
    fun getAll(): List<PinData>

    @Query("select * from pinData where username LIKE :userName")
    fun findTaskById(userName: String): List<PinData>

    @Insert(onConflict = REPLACE)
    fun insert(pinData: PinData)

    @Delete
    fun deletePinData(pinData: PinData)
}

@Database(entities = [PinData::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): PinDataDao
}
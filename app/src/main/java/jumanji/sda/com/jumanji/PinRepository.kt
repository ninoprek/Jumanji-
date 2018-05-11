package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


data class PinDataInfo(val longitude: Float = 0.0f,
                       val latitude: Float = 0.0f,
                       val imageURL: String = "",
                       val username: String = "",
                       val isTrash: Boolean = true)

class PinRepository(application: Application) {

    private var roomPinDb = Room.databaseBuilder(application,
            AppDatabase::class.java, "database-name")
            .fallbackToDestructiveMigration()
            .build()

    private val database = FirebaseFirestore.getInstance()
    var userPinData: MutableLiveData<List<PinData>> = MutableLiveData()
    var pinDataAll: MutableLiveData<List<PinData>> = MutableLiveData()

    fun storePinToFirebase(pinData: PinDataInfo) {

        val pin: HashMap<String, Any> = HashMap()
        pin.put("longitude", pinData.longitude)
        pin.put("latitude", pinData.latitude)
        pin.put("imageURL", pinData.imageURL)
        pin.put("username", pinData.username)
        pin["isTrash"] = pinData.isTrash

        database.collection("allPins").document().set(pin)
    }

    fun reportPinAsCleanToFirebase(pinId: String) {
        val pin: HashMap<String, Any> = HashMap()
        pin["isTrash"] = false
        database.collection("allPins").document(pinId).update(pin)
    }

    fun storeAllPinsFromFirebaseToRoom() {
        val firebaseDb = FirebaseFirestore.getInstance()

        firebaseDb.collection("allPins").get()
                .addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        val pins = task.result

                        for (document in pins) {

                            val pin = PinData(document.id,
                                    document["longitude"].toString().toFloat(),
                                    document["latitude"].toString().toFloat(),
                                    document["username"].toString(),
                                    document["imageURL"].toString(),
                                    document["isTrash"] as Boolean
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

//        val user = "nino"
//
//        val pin3 = PinDataInfo(61.522433f, 19.917423f, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "3")
//        val pin4 = PinDataInfo(62.522433f, 20.917423f, "https://www.digiplex.com/resources/locations/DS1-high.jpg-2/basic700", "4")
//        storePinToFirebase(pin3, user)
//        storePinToFirebase(pin4, user)
    }

    fun loadAllTrashPins(indicator: Boolean): LiveData<List<PinData>> {
        return roomPinDb.userDao().loadAllPins(indicator)
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
                   @ColumnInfo(name = "imageURL") var imageURL: String,
                   @ColumnInfo(name = "isTrash") var isTrash: Boolean
)

@Dao
interface PinDataDao {
    @Query("SELECT * FROM pinData WHERE isTrash == :indicator")
    fun loadAllPins(indicator: Boolean): LiveData<List<PinData>>

    @Query("select * from pinData where username LIKE :userName")
    fun findTaskById(userName: String): List<PinData>

    @Insert(onConflict = REPLACE)
    fun insert(pinData: PinData)

    @Delete
    fun deletePinData(pinData: PinData)
}

@Database(entities = [PinData::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): PinDataDao
}
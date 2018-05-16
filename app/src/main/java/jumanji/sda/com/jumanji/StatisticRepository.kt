package jumanji.sda.com.jumanji

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class StatisticRepository {
    companion object {
        const val TOTAL_REPORTED_PINS = "totalReportedPins"
        const val TOTAL_CLEANED_PINS = "totalCleanPins"
        const val TOTAL_USERS = "totalUsers"
    }

    private val database = FirebaseFirestore.getInstance()
    private val communityStatisticsReference =
            database.collection("communityStatistics")
                    .document("allUsers")

    val communityTotalReportedPins: MutableLiveData<Int> = MutableLiveData()
    val communityTotalCleanPins: MutableLiveData<Int> = MutableLiveData()
    val totalUsers: MutableLiveData<Int> = MutableLiveData()

    fun updateStatistics(documentReference: DocumentReference = communityStatisticsReference
                         , referenceName: String) {
        Single.fromCallable { incrementNumberByOne(documentReference, referenceName) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun getStatisticFromFirebase(documentReference: DocumentReference = communityStatisticsReference) {
        documentReference.get().addOnSuccessListener { document ->
            if (document.exists()) {
                communityTotalReportedPins.value = document[TOTAL_REPORTED_PINS].toString().toInt()
                communityTotalCleanPins.value = document[TOTAL_CLEANED_PINS].toString().toInt()
                totalUsers.value = document[TOTAL_USERS].toString().toInt()
            }
        }
    }

    private fun incrementNumberByOne(documentReference: DocumentReference, referenceName: String) {
        documentReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val newValue = document[referenceName].toString().toInt() + 1
                    documentReference.update(referenceName, newValue)
                            .addOnSuccessListener {
                                when (referenceName) {
                                    TOTAL_REPORTED_PINS -> {
                                        communityTotalReportedPins.postValue(newValue)
                                    }

                                    TOTAL_CLEANED_PINS -> {
                                        communityTotalCleanPins.postValue(newValue)
                                    }

                                    TOTAL_USERS -> {
                                        totalUsers.postValue(newValue)
                                    }
                                }
                            }
                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "get failed with " + task.exception)
            }
        }
    }

    fun decreaseNumberByOne(documentReference: DocumentReference = communityStatisticsReference
                            , referenceName: String) {
        documentReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val newValue = document[referenceName].toString().toInt() - 1
                    documentReference.update(referenceName, newValue)
                            .addOnSuccessListener {
                                when (referenceName) {
                                    TOTAL_REPORTED_PINS -> {
                                        communityTotalReportedPins.postValue(newValue)
                                    }

                                    TOTAL_CLEANED_PINS -> {
                                        communityTotalCleanPins.postValue(newValue)
                                    }

                                    TOTAL_USERS -> {
                                        totalUsers.postValue(newValue)
                                    }
                                }
                            }
                } else {
                    Log.d(javaClass.simpleName, "No such document")
                }
            } else {
                Log.d(javaClass.simpleName, "get failed with " + task.exception)
            }
        }
    }
}
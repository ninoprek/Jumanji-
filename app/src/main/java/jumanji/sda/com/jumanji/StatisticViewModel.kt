package jumanji.sda.com.jumanji

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class StatisticViewModel : ViewModel() {
    private val repository = StatisticRepository()
    private val communityTotalReportedPins: LiveData<Int> = repository.communityTotalReportedPins
    private val communityTotalCleanedPins: LiveData<Int> = repository.communityTotalCleanPins
    private val totalUsers: LiveData<Int> = repository.totalUsers
    val averageUserReportedPins: MediatorLiveData<Int> = MediatorLiveData()
    val averageUserCleanedPins: MediatorLiveData<Int> = MediatorLiveData()

    init {
        getUpdateFromFirebase()
        averageUserReportedPins.addSource(communityTotalReportedPins, { totalReportedPins ->
            if (totalUsers.value != null && totalReportedPins != null) {
                averageUserReportedPins.value = totalReportedPins / totalUsers.value!!
            }
        })

        averageUserReportedPins.addSource(totalUsers, { totalUsers ->
            if (totalUsers != null) {
                averageUserReportedPins.value = communityTotalReportedPins.value!! / totalUsers
            }
        })

        averageUserCleanedPins.addSource(communityTotalCleanedPins, { totalReportedPins ->
            if (totalUsers.value != null && totalReportedPins != null) {
                averageUserCleanedPins.value = totalReportedPins / totalUsers.value!!
            }
        })

        averageUserCleanedPins.addSource(totalUsers, { totalUsers ->
            if (totalUsers != null) {
                averageUserCleanedPins.value = communityTotalCleanedPins.value!! / totalUsers
            }
        })
    }

    fun getUpdateFromFirebase() {
        Single.fromCallable { repository.getStatisticFromFirebase() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun updateCommunityStatistics(referenceName: String) {
        repository.updateStatistics(referenceName = referenceName)
    }
}
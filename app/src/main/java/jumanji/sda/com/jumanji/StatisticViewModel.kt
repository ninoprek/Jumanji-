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
        val value = totalUsers.value
        averageUserReportedPins.addSource(communityTotalReportedPins, { totalReportedPins ->
            if (value != null && value > 0 && totalReportedPins != null) {
                averageUserReportedPins.value = totalReportedPins / value
            }
        })

        averageUserReportedPins.addSource(totalUsers, { totalUsers ->
            if (totalUsers != null && totalUsers > 0) {
                averageUserReportedPins.value = communityTotalReportedPins.value!! / totalUsers
            }
        })

        averageUserCleanedPins.addSource(communityTotalCleanedPins, { totalReportedPins ->
            if (value != null && totalReportedPins != null) {
                averageUserCleanedPins.value = totalReportedPins / value
            }
        })

        averageUserCleanedPins.addSource(totalUsers, { totalUsers ->
            if (totalUsers != null && totalUsers > 0) {
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

    fun updateUsersNumberWhenDeleteProfile(referenceName: String) {
        Single.fromCallable { repository.decreaseNumberByOne(referenceName = referenceName) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

}
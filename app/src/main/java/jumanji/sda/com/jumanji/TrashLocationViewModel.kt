package jumanji.sda.com.jumanji

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class TrashLocationViewModel(application: Application) : AndroidViewModel(application) {
    val trashLocationsCache: LiveData<List<LatLng>> = MutableLiveData() //TODO: receive location data from database

    init {
        trashLocationsCache as MutableLiveData
        trashLocationsCache.postValue(listOf(LatLng(59.3563219, 18.0935219),
                LatLng(59.3463219, 18.0735219),
                LatLng(59.3563219, 18.0835219),
                LatLng(59.3663219, 18.0935219),
                LatLng(59.3763219, 18.0635219),
                LatLng(59.3863219, 18.0535219),
                LatLng(59.3863219, 18.0435219),
                LatLng(59.3363219, 18.0335219),
                LatLng(59.3263219, 18.0235219),
                LatLng(59.3163219, 18.0135219)))
    }

    fun loadTrashLocations(latLngBounds: LatLngBounds) {
        //TODO: call method in repository to update trashLocationsCache
        //trashLocationsCache = repository.loadTrashLocations
    }

}
package jumanji.sda.com.jumanji

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            application.applicationContext)

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): LatLng? {
        var lastKnownLocation: LatLng? = null
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            lastKnownLocation = LatLng(it.result.latitude, it.result.longitude)
        }
        return lastKnownLocation
    }
}


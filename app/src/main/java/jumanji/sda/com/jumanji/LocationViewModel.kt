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
    fun getLastLocations(): LatLng? {
        var lastKnownLocation: LatLng? = null
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            it?.let {
                lastKnownLocation = LatLng(it.latitude, it.longitude)
            }
        }
        return lastKnownLocation
    }
}
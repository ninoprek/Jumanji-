package jumanji.sda.com.jumanji

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.location.Location
import android.support.annotation.RequiresPermission
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

interface OnLastLocationWatcher {
    fun onLastLocationReadyCallBack(location: Location)
}

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val DEFAULT_ZOOM_LEVEL = 13.5f
        const val DEFAULT_LATITUDE = 59.3498065
        const val DEFAULT_LONGITUDE = 18.0684759
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            application.applicationContext)

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(listener: OnLastLocationWatcher): Location? {
        var lastKnownLocation: Location? = null
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    it?.let { listener.onLastLocationReadyCallBack(it) }
                    Log.d("TAG", "last location: ${it.latitude}, ${it.longitude}.")
                }
                .addOnFailureListener {
                    Log.d("ERROR", "something went wrong: ${it.message}.")
                }
        return lastKnownLocation
    }


}


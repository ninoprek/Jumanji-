package jumanji.sda.com.jumanji

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.location.Location
import android.support.annotation.RequiresPermission
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

interface OnLastLocationReadyListener {
    fun onResultReadyCallBack(location: Location)
}

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            application.applicationContext)

    fun createLocationRequest(): LocationRequest {
        return LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(listener: OnLastLocationReadyListener): Location? {
        var lastKnownLocation: Location? = null
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    it?.let { listener.onResultReadyCallBack(it) }
                    Log.d("TAG", "last location: ${it.latitude}, ${it.longitude}.")
                }
                .addOnFailureListener {
                    Log.d("ERROR", "something went wrong: ${it.message}.")
                }
        return lastKnownLocation
    }


}


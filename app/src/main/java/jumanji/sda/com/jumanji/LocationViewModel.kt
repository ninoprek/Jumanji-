package jumanji.sda.com.jumanji

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.support.annotation.RequiresPermission
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val DEFAULT_ZOOM_LEVEL = 13.5f
        const val DEFAULT_LATITUDE = 59.3498065
        const val DEFAULT_LONGITUDE = 18.0684759
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(application.applicationContext)
    lateinit var locationRequest: LocationRequest

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(map: GoogleMap) {
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    it?.let { location ->
                        val position = LatLng(location.latitude, location.longitude)
                        map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                        position,
                                        LocationViewModel.DEFAULT_ZOOM_LEVEL))
                    }
                }
                .addOnFailureListener {
                    Log.d("ERROR", "something went wrong: ${it.message}.")
                }
    }

    private fun createLocationSettingRequest() {
        locationRequest = LocationRequest().apply {
            interval = 600000
            fastestInterval = 60000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    fun initiateUserSettingCheck(context: Context?): Task<LocationSettingsResponse>? {
        return if (context != null) {
            val settingsClient = LocationServices.getSettingsClient(context)
            createLocationSettingRequest()
            val locationSettingsRequest = LocationSettingsRequest
                    .Builder()
                    .addLocationRequest(locationRequest)
                    .build()
            settingsClient.checkLocationSettings(locationSettingsRequest)
        } else {
            null
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context?, locationCallBack: LocationCallback) {
        if (context != null) {
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallBack,
                    null)
        }
    }

    fun stopLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    fun flushLocations() {
        fusedLocationProviderClient.flushLocations()
    }
}


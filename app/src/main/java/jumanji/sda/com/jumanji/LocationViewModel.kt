package jumanji.sda.com.jumanji

import android.Manifest
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
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

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            application.applicationContext)
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var currentLocation: LiveData<LatLng> = MutableLiveData()

    private fun createLocationSettingRequest() {
        locationRequest = LocationRequest().apply {
            interval = 3000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun initiateUserSettingCheck(context: Context): Task<LocationSettingsResponse> {
        val settingsClient = LocationServices.getSettingsClient(context)
        createLocationSettingRequest()
        val locationSettingsRequest = LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)
                .build()
        return settingsClient.checkLocationSettings(locationSettingsRequest)
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let { locationResult ->
                    val lastIndex = locationResult.locations.lastIndex
                    val location = locationResult.locations[lastIndex]
                    Log.d("TAG", "location from GPS: ${location.latitude}, ${location.longitude}")
                    (currentLocation as MutableLiveData).postValue(
                            LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    fun startLocationUpdates(context: Context) {
        createLocationCallback()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            Toast.makeText(context,
                    "Please enable permission to access your device location.",
                    Toast.LENGTH_LONG)
                    .show()
        }
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    fun moveToLastKnowLocation(map: GoogleMap, zoomLevel: Float = DEFAULT_ZOOM_LEVEL) {
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    val location = LatLng(it.latitude, it.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel),
                            500, null)
                }
            }
        }
    }
}


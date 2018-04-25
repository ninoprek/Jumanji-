package jumanji.sda.com.jumanji

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_map.*

class MapFragment : Fragment() {
    companion object {
        private const val DEFAULT_ZOOM_LEVEL = 12.0f
        private const val DEFAULT_LATITUDE = 59.3498065f
        private const val DEFAULT_LONGITUDE = 18.0684759f
        private const val CAMERA_LATITUDE = "camera_latitude"
        private const val CAMERA_LONGITUDE = "camera_longitude"
        private const val CAMERA_ZOOM = "camera_zoom"
        private const val CAMERA_TILT = "camera_tilt"
        private const val CAMERA_BEARING = "camera_bearing"
        private const val CAMERA_PREFERENCE = "camera_preference"
        private val PICK_IMAGE_REQUEST = 71
    }

    private lateinit var map: GoogleMap
    private lateinit var mapCameraManager: MapCameraManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapCameraManager = MapCameraManager()
        val cameraPosition = mapCameraManager.getCameraState()
        mapView.getMapAsync {
            map = it
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            val currentLocation: LatLng? = null // To get from GPS for current user location
            if (currentLocation != null) {
                val currentPositionMarker = MarkerOptions()
                currentPositionMarker.title("You are here!")
                currentPositionMarker.position(currentLocation)
                currentPositionMarker.alpha(0.5f)
                map.addMarker(currentPositionMarker)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM_LEVEL))
            }
        }

        reportFab.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK
                && data != null && data.data != null) {

            val uri: Uri = data.data
            val mStorageRef: StorageReference = FirebaseStorage.getInstance().getReference()
            val filepath: StorageReference = mStorageRef.child("Images").child(uri.lastPathSegment)

            filepath.putFile(uri)
                    .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        // Get a URL to the uploaded content
                        val downloadUrl = taskSnapshot.downloadUrl
                    })
                    .addOnFailureListener(OnFailureListener {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("ERROR", "Unable to upload")
                    })

        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (this::mapCameraManager.isInitialized) {
            mapCameraManager.saveMapCameraState()
        }
        mapView.onPause()
    }

    inner class MapCameraManager {
        private val mapCameraPreferences = context!!.getSharedPreferences(CAMERA_PREFERENCE, Context.MODE_PRIVATE)

        fun saveMapCameraState() {
            val cameraPosition = map.cameraPosition
            val latitude = cameraPosition.target.latitude.toFloat()
            val longitude = cameraPosition.target.longitude.toFloat()
            val zoom = cameraPosition.zoom
            val tilt = cameraPosition.tilt
            val bearing = cameraPosition.bearing
            val editor = mapCameraPreferences?.edit()
            editor?.putFloat(CAMERA_LATITUDE, latitude)
            editor?.putFloat(CAMERA_LONGITUDE, longitude)
            editor?.putFloat(CAMERA_ZOOM, zoom)
            editor?.putFloat(CAMERA_TILT, tilt)
            editor?.putFloat(CAMERA_BEARING, bearing)
            editor?.apply()
        }

        fun getCameraState(): CameraPosition {
            val latitude = mapCameraPreferences.getFloat(CAMERA_LATITUDE, DEFAULT_LATITUDE).toDouble()
            val longitude = mapCameraPreferences.getFloat(CAMERA_LONGITUDE, DEFAULT_LONGITUDE).toDouble()
            val zoom = mapCameraPreferences.getFloat(CAMERA_ZOOM, DEFAULT_ZOOM_LEVEL)
            val tilt = mapCameraPreferences.getFloat(CAMERA_TILT, 0.0f)
            val bearing = mapCameraPreferences.getFloat(CAMERA_BEARING, 0.0f)
            return CameraPosition(LatLng(latitude, longitude), zoom, tilt, bearing)
        }
    }
}
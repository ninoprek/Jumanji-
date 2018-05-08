package jumanji.sda.com.jumanji

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.lang.Thread.sleep
import android.content.DialogInterface
import android.support.v7.app.AlertDialog


class ProfileFragment : Fragment(), OnMapReadyCallback {
    lateinit var map: GoogleMap

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userActivityMapView.onCreate(savedInstanceState)

        userActivityMapView.onCreate(savedInstanceState)
        userActivityMapView.getMapAsync(this)

        val profileViewModel = ViewModelProviders.of(this)[ProfileViewModel::class.java]
        var username: String? = ""

        profileViewModel.userInfo?.observe(this, Observer {
            username = it?.userName
            usernameText.text = username
            Picasso.get().load(it?.photoURL).into(profilePhotoView);
        })

        signOutButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val googleUser = GoogleSignIn.getLastSignedInAccount(context)

            if (user != null) {
               // Snackbar.make(it, "${username}, you are signed out", Snackbar.LENGTH_SHORT).show()
                profileViewModel.signOut()
                goToSignIn()

            } else if (googleUser != null) {
                //Snackbar.make(it, "${username}, you are signed out", Snackbar.LENGTH_SHORT).show()
                profileViewModel.googleSignOut(this.requireContext())
                goToSignIn()
            } else {
            Snackbar.make(it, "No user is currently singed in.", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    fun goToSignIn() {
        val intent = Intent(context, SignInActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        userActivityMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        userActivityMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        userActivityMapView.onPause()
    }

    override fun onDestroy() {
        userActivityMapView?.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        userActivityMapView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        userActivityMapView?.onLowMemory()
        super.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        map.isIndoorEnabled = false
        val zoomLevelForProfile = 14.5f
        val viewModel = ViewModelProviders.of(activity!!)[LocationViewModel::class.java]
        viewModel.moveToLastKnowLocation(map, zoomLevelForProfile)
        val context = this@ProfileFragment.context?: return
        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false
            map.uiSettings.isZoomGesturesEnabled = false
        } else {
            Toast.makeText(context,
                    "Please enable permission to access your device location.",
                    Toast.LENGTH_LONG)
                    .show()
        }
    }
}

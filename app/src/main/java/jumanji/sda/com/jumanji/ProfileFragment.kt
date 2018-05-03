package jumanji.sda.com.jumanji

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userActivityMapView.onCreate(savedInstanceState)

        var profileViewModel = ViewModelProviders.of(this)[ProfileViewModel::class.java]

        profileViewModel.getUserProfile(this.context!!)

        profileViewModel.userInfo?.observe(this, Observer {

            usernameText.text = it?.userName
            Picasso.get().load(it?.pictureURI).into(profilePhotoView);
        })

        signOutButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser?.displayName
            val profileViewModel = ViewModelProviders.of(this)[ProfileViewModel::class.java]
            profileViewModel.signOut()

            if (user != null) {
                Snackbar.make(it, "${user}, you are signed out", Snackbar.LENGTH_SHORT).show()
            } else {
                val userName = GoogleSignIn.getLastSignedInAccount(activity)?.displayName
                Snackbar.make(it, "${userName}, you are signed out", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
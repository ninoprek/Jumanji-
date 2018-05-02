package jumanji.sda.com.jumanji

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*

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

        signOutButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser?.displayName
            val profileViewModel = ProfileViewModel()
            profileViewModel.signOut()
            Snackbar.make(it, "${user}, you are signed out", Snackbar.LENGTH_SHORT).show()
        }
    }
}
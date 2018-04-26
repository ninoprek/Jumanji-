package jumanji.sda.com.jumanji

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {

    val profileViewModel = CreateProfileViewModel()
    var userName = ""
    var email = ""
    var uriString = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        profileSignUpButton.setOnClickListener {
            val createProfileIntent = Intent(this, CreateProfileActivity::class.java)
            startActivity(createProfileIntent)
            this.finish()
        }

        signInButton.setOnClickListener({
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("signing in...")
                    .show()
        })

        googleSignInButton.setOnClickListener({

            //Sign in with google
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build()


            // Build a GoogleSignInClient with the options specified by options.

            val client = GoogleSignIn.getClient(this, options)

            val signIn = client.silentSignIn()

            if (signIn.isSuccessful) {
                getInfo(signIn)

            } else {
                startActivityForResult(client.signInIntent, 10)
            }


        })


    }

     /*override fun onStart() {
         super.onStart()
         // Check for existing Google Sign In account, if the user is already signed in
         // the GoogleSignInAccount will be non-null.
         val account = GoogleSignIn.getLastSignedInAccount(this)
         updateUI(account)
     }*/

    /* private fun updateUI(account: GoogleSignInAccount?) {
         if (account != null) {

             val intent = Intent(this, ProgramActivity::class.java)

         } else {

             //no last log in
             val intent = Intent(this, SignInActivity::class.java)
         }
         startActivity(intent)
     }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            val signedInAccountFromIntent = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (signedInAccountFromIntent.isSuccessful) {
                getInfo(signedInAccountFromIntent)
            }
        }
    }

    private fun getInfo(info: Task<GoogleSignInAccount>) {
        val result = info.result
        Toast.makeText(this, "Welcome  " + result.displayName, Toast.LENGTH_LONG).show()

        userName = result.givenName!!
        email = result.email!!
        uriString = result.photoUrl.toString()

        val profile = UserProfile(userName, email, uriString)
        profileViewModel.saveUserProfile(profile)
    }
}

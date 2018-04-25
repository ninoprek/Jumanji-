package jumanji.sda.com.jumanji

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        //findViewById(R.id.sign_in_button).setOnClickListener(this)


        //Sign in with google
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build()


        // Build a GoogleSignInClient with the options specified by gso.
        val   client = GoogleSignIn.getClient(this, options)


        signInButton.setOnClickListener {
            val signIn = client.silentSignIn()
            if (signIn.isSuccessful) {
                getInfo(signIn)
            } else {
                startActivityForResult(client.signInIntent, 10)
            }

        }

        /* signOutButton.setOnClickListener{
            client.signOut()
            firstNameText.text = ""
            emailText.text = ""
        } */

    }

    fun createProfile(view: View) {
        val profileIntent = Intent(this, ProfileActivity::class.java)
        startActivity(profileIntent)
    }


    override  fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if(account!=null)
        {
            //go to the home page
           // val intent=Intent(this, "activity_main"::class.java)
            //or do silent sign in

        }else{

            //no last log in
        }
    }


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
        Toast.makeText(this,"Welcome  "+result.displayName,Toast.LENGTH_LONG).show()
        //firstNameText.text = result.givenName
        //emailText.text = result.email
    }
}

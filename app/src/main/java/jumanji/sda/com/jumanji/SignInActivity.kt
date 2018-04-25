package jumanji.sda.com.jumanji

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


    }

    fun createProfile(view: View){
        val profileIntent = Intent(this, ProfileActivity::class.java)
        startActivity(profileIntent)
    }
}

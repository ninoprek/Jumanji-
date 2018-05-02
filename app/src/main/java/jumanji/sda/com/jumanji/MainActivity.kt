package jumanji.sda.com.jumanji

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    var authenticator = FirebaseAuth.getInstance()
    var user = authenticator.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Check if the user is sign in (go to the map) or he/she should redirected to the sign in activity
        if (user != null) {
            val intent = Intent(this, ProgramActivity::class.java)
            startActivity(intent)
            this.finish()

        } else {
            Single.fromCallable { startSignInActivity() }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({  }
                            , { error ->
                        Toast.makeText(this,
                                "something went wrong: $error",
                                Toast.LENGTH_SHORT).show()
                    })
        }
    }

    private fun startSignInActivity() {
        sleep (1500)
        val signInIntent = Intent(this, SignInActivity::class.java)
        startActivity(signInIntent)
        this.finish()
    }
}
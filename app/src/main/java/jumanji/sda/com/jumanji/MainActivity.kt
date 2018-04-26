package jumanji.sda.com.jumanji


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val testUserProfile: UserProfile = UserProfile("Jumanji", "jumanji@emai.com", "www.picture.com")
        val userProfileRepository: UserProfileRepository = UserProfileRepository()
        userProfileRepository.storeToDatabase(testUserProfile)*/

        startButton.setOnClickListener({
            val signInIntent = Intent(this, SignInActivity::class.java )
            startActivity(signInIntent)
        this.finish()})
    }
}
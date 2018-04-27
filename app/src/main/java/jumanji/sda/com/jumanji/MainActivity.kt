package jumanji.sda.com.jumanji

import kotlinx.android.synthetic.main.activity_main.*

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener({
            val signInIntent = Intent(this, SignInActivity::class.java)
            startActivity(signInIntent)

            val userProfilerepository : UserProfileRepository = UserProfileRepository()

            userProfilerepository.retrivePhotoFromRepository("299")
        })
    }
}


package jumanji.sda.com.jumanji

import android.app.Activity
import android.content.ContextWrapper

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
import jumanji.sda.com.jumanji.R.id.imageView
import android.provider.MediaStore
import android.graphics.Bitmap
import jumanji.sda.com.jumanji.R.id.startButton
import java.io.IOException

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.startButton

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*val testUserProfile: UserProfile = UserProfile("Jumanji", "jumanji@emai.com", "www.picture.com")

        val userProfileRepository: UserProfileRepository = UserProfileRepository()
        userProfileRepository.storeToDatabase(testUserProfile)*/
        startButton.setOnClickListener({
            val signInIntent = Intent(this, SignInActivity::class.java )
            startActivity(signInIntent)})

/*
        val mStorageRef: StorageReference = FirebaseStorage.getInstance().getReference("photo")

        val file = Uri.fromFile(File("file:///Users/tmp-sda-1164/Downloads/jumanji.jpg"))
        val riversRef = mStorageRef.child("images/$file")

        riversRef.putFile(file)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    // Get a URL to the uploaded content
                    val downloadUrl = taskSnapshot.downloadUrl
                })
                .addOnFailureListener(OnFailureListener {
                    // Handle unsuccessful uploads
                    // ...
                    Log.d("ERROR", "Unable to upload")
                })

*/
    }
}
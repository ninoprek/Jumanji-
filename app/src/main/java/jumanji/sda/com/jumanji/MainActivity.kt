package jumanji.sda.com.jumanji

<<<<<<< HEAD

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
=======
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
import java.io.IOException

>>>>>>> b3a885df724b1f9609d376655498fbfb0be5bf93

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 71

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)



        /*val testUserProfile: UserProfile = UserProfile("Jumanji", "jumanji@emai.com", "www.picture.com")
        val userProfileRepository: UserProfileRepository = UserProfileRepository()
        userProfileRepository.storeToDatabase(testUserProfile)*/
<<<<<<< HEAD

        startButton.setOnClickListener({
            val signInIntent = Intent(this, SignInActivity::class.java )
            startActivity(signInIntent)})
    }
}
=======
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.data != null) {

            val uri:Uri = data.data
            val mStorageRef: StorageReference = FirebaseStorage.getInstance().getReference()
            val filepath:StorageReference = mStorageRef.child("Images").child(uri.lastPathSegment)

            filepath.putFile(uri)
                    .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        // Get a URL to the uploaded content
                        val downloadUrl = taskSnapshot.downloadUrl
                    })
                    .addOnFailureListener(OnFailureListener {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d("ERROR", "Unable to upload")
                    })

        }
    }


    fun login(view: View){
        val signInIntent = Intent(this, SignInActivity::class.java )
        startActivity(signInIntent)
    }

}
>>>>>>> b3a885df724b1f9609d376655498fbfb0be5bf93

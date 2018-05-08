package jumanji.sda.com.jumanji

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.NonNull
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class PhotoRepository (email: String?) {

    val email = email?.toLowerCase()

    fun storePhotoToDatabase(uri: Uri?, activity: FragmentActivity?) {

        //var uri = data?.data
        val mStorageRef: StorageReference = FirebaseStorage.getInstance().getReference("$email/images")
        val imageRef = mStorageRef.child("$uri")
        Log.e("value", "uri Value: $uri")

        //if (uri == null) {
        //    uri = data!!.extras[MediaStore.EXTRA_OUTPUT] as Uri
        //}

        if (uri != null) {
            imageRef.putFile(uri)
                    .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                        // Get a URL to the uploaded content
                        val downloadUrl = taskSnapshot.downloadUrl
                        Log.d("SUCCESS", "Able  to upload")
                        val toast = Toast.makeText(activity, "File Uploaded ", Toast.LENGTH_SHORT)
                        toast.show()

                    })
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, exception.message, Toast.LENGTH_SHORT).show()
                        Log.d("ERROR", "Unable to upload")
                    }
        } else {
            Toast.makeText(activity, "File not found ", Toast.LENGTH_SHORT).show()
        }
    }

}
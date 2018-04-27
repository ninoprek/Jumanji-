package jumanji.sda.com.jumanji

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap

import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.*

class CreateProfileActivity : AppCompatActivity(), PhotoListener {

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val SELECT_FILE = 200
    }
    var userChoosenTask: String = ""

    private var uriString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val viewModel: ProfileViewModel = ViewModelProviders.of(this)[ProfileViewModel::class.java]

        profilePhoto.setOnClickListener {
            selectImage()
        }

        saveButton.setOnClickListener({

          //  if(            )
            val userName = userNameField.text.toString()
            val email = emailField.text.toString()

            val profile = UserProfile(userName, email, uriString)
            viewModel.saveUserProfile(profile)

            val intent = Intent(this, ProgramActivity::class.java )
            startActivity(intent)})

        cancelButton.setOnClickListener({
            val intent = Intent(this, ProgramActivity::class.java )
            startActivity(intent)
        this.finish()})
    }

    override fun selectImage()  {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(this@CreateProfileActivity)
        builder.setTitle("Add Photo!")
        builder.setItems(items, DialogInterface.OnClickListener { dialog, item ->
            val result = Utility.checkPermission(this@CreateProfileActivity)
            if (items[item] == "Take Photo") {
                userChoosenTask = "Take Photo"
                if (result)
                    cameraIntent()
            } else if (items[item] == "Choose from Library") {
                userChoosenTask = "Choose from Library"
                if (result)
                    galleryIntent()
            } else if (items[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    private fun cameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun galleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (userChoosenTask.equals("Take Photo"))
                    cameraIntent()
                else if (userChoosenTask.equals("Choose from Library"))
                    galleryIntent()
            } else {
                //code for deny
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data)
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data)
        }
    }

    private fun onSelectFromGalleryResult(data: Intent?) {

        var bm: Bitmap? = null
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, data.data)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        profilePhoto.setImageBitmap(bm)
    }

    private fun onCaptureImageResult(data: Intent) {
        val thumbnail = data.extras!!.get("data") as Bitmap
        val bytes = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val destination = File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis().toString() + ".jpg")
        val fo: FileOutputStream
        try {
            destination.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        profilePhoto.setImageBitmap(thumbnail)
    }
}
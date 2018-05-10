package jumanji.sda.com.jumanji

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri

import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_profile.*
import java.io.*

class CreateProfileActivity : AppCompatActivity(), TextWatcher, PhotoListener, OnUrlAvailableListener {
    override fun storeDataToFirebase(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val SELECT_FILE = 200
    }

    var userChoosenTask: String = ""

    private var uriString: Uri = Uri.parse("android.resource://jumanji.sda.com.jumanji/" + R.drawable.download)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        saveButton.isEnabled = false
        userNameField.addTextChangedListener(this)
        passwordField.addTextChangedListener(this)
        confirmPasswordField.addTextChangedListener(this)
        emailField.addTextChangedListener(this)
        val viewModel: ProfileViewModel = ViewModelProviders.of(this)[ProfileViewModel::class.java]

        profilePhoto.setOnClickListener {
            selectImage()
        }

        saveButton.setOnClickListener {
            if (passwordField.text.length >= 6) {
                val userName = userNameField.text.toString()
                val email = emailField.text.toString()
                val password = passwordField.text.toString()
                val photoRepository = PhotoRepository(email)

                val profile = UserProfile(userName, password, email, uriString?.toString())
                viewModel.saveUserProfile(profile)
                viewModel.initializeUserPinNumber(userName)

                photoRepository.storePhotoToDatabase(uriString, this, this)
                val intent = Intent(this, ProgramActivity::class.java)
                startActivity(intent)
                this.finish()
            } else {
                Toast.makeText(this@CreateProfileActivity,
                        "Password is too short.",
                        Toast.LENGTH_SHORT)
                        .show()
            }
        }

        cancelButton.setOnClickListener({
            val intent = Intent(this, ProgramActivity::class.java)
            startActivity(intent)
        })
    }

    override fun selectImage() {
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
                uriString = data?.data
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        profilePhoto.setImageBitmap(bm)
        Log.d(javaClass.simpleName, "Setting image to profile.")
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
        uriString = Uri.fromFile(destination.absoluteFile)
    }

    override fun afterTextChanged(s: Editable?) {
        if (userNameField.text.isNotEmpty() &&
                passwordField.text.isNotEmpty() &&
                confirmPasswordField.text.isNotEmpty() &&
                passwordField.text.toString() == confirmPasswordField.text.toString() &&
                emailField.text.contains("@")) {
            saveButton.isEnabled = true
        } else {
            if (passwordField.text.length == confirmPasswordField.text.length &&
                    passwordField.text.toString() != confirmPasswordField.text.toString()) {
                Toast.makeText(this,
                        "Password doesn't match.",
                        Toast.LENGTH_SHORT)
                        .show()
            }
            saveButton.isEnabled = false
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

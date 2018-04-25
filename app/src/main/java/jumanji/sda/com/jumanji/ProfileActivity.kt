package jumanji.sda.com.jumanji

import android.content.Intent

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import jumanji.sda.com.jumanji.R.id.profilePhoto
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    val repository = UserProfileRepository()
    var userName = ""
    var email = ""
    var uriString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePhoto.setOnClickListener {
            val intentPickImage = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentPickImage, 0)
        }

        saveButton.setOnClickListener({
            userName = userNameField.text.toString()
            email = emailField.text.toString()

            val profile = UserProfile(userName, email, uriString)
            repository.storeToDatabase(profile)

            val intent = Intent(this, ProgramActivity::class.java )
            startActivity(intent)})

        cancelButton.setOnClickListener({
            val intent = Intent(this, ProgramActivity::class.java )
            startActivity(intent)})
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        Picasso.get().load(uri).into(profilePhoto)
        uriString = uri.toString()
    }
}
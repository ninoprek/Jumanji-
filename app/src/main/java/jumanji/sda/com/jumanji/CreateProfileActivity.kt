package jumanji.sda.com.jumanji

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class CreateProfileActivity : AppCompatActivity() {

    private var uriString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val viewModel: CreateProfileViewModel = ViewModelProviders.of(this)[CreateProfileViewModel::class.java]

        profilePhoto.setOnClickListener {
            val intentPickImage = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentPickImage, 0)
        }

        saveButton.setOnClickListener({
            val userName = userNameField.text.toString()
            val email = emailField.text.toString()
            val profile = UserProfile(userName, email, uriString)

            viewModel.saveUserProfile(profile)

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
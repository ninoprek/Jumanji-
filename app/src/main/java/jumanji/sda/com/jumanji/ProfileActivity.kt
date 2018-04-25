package jumanji.sda.com.jumanji

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profilePhoto.setOnClickListener {
            val intentPickImage = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentPickImage, 0)
        }

        saveButton.setOnClickListener({
            val userName = userNameField.text
            val email = emailField.text
            val password = passwordField.text

            val dialog = AlertDialog.Builder(this)
            dialog.setMessage("saving").show()
        })

        cancelButton.setOnClickListener({

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        Picasso.get().load(uri).into(profilePhoto)
    }
}
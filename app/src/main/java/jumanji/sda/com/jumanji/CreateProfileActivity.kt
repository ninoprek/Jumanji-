package jumanji.sda.com.jumanji

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class CreateProfileActivity : AppCompatActivity(), TextWatcher {
    private var uriString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        saveButton.isEnabled = false
        userNameField.addTextChangedListener(this)
        passwordField.addTextChangedListener(this)
        confirmPasswordField.addTextChangedListener(this)
        emailField.addTextChangedListener(this)
        val viewModel: ProfileViewModel = ViewModelProviders.of(this)[ProfileViewModel::class.java]

        profilePhoto.setOnClickListener {
            val intentPickImage = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentPickImage, 0)
        }

        saveButton.setOnClickListener({

            //  if(            )
            val userName = userNameField.text.toString()
            val email = emailField.text.toString()

            val profile = UserProfile(userName, email, uriString)
            viewModel.saveUserProfile(profile)

            val intent = Intent(this, ProgramActivity::class.java)
            startActivity(intent)
        })

        cancelButton.setOnClickListener({
            val intent = Intent(this, ProgramActivity::class.java)
            startActivity(intent)
            this.finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        Picasso.get().load(uri).into(profilePhoto)
        uriString = uri.toString()
    }

    override fun afterTextChanged(s: Editable?) {
        if (userNameField.text.isNotEmpty() &&
                passwordField.text.isNotEmpty() &&
                confirmPasswordField.text.isNotEmpty() &&
                passwordField.text.toString() == confirmPasswordField.text.toString() &&
                emailField.text.isNotEmpty()) {
            saveButton.isEnabled = true
        } else {
            if (passwordField.text.toString() != confirmPasswordField.text.toString() &&
                    passwordField.text.isNotEmpty() &&
                    confirmPasswordField.text.isNotEmpty()) {
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

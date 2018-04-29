package jumanji.sda.com.jumanji

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Single.fromCallable { startActivity() }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ }
                        , { error ->
                    Toast.makeText(this,
                            "something went wrong: $error",
                            Toast.LENGTH_SHORT).show()
                })
    }

    private fun startActivity() {
        sleep(1500)
        val signInIntent = Intent(this, SignInActivity::class.java)
        startActivity(signInIntent)
        this.finish()
    }
}


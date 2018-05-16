package jumanji.sda.com.jumanji

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home_page.*

class ProgramActivity : AppCompatActivity() {
    private var flagToQuitApp = false
    private lateinit var quitToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home_page)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        val adapter = PagerAdapter(supportFragmentManager)
        container.adapter = adapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        val pinViewModel = ViewModelProviders.of(this)[PinViewModel::class.java]
        pinViewModel.queryDataFromFirebaseToRoom()

    }

    class PagerAdapter(fragmentManger: FragmentManager) : FragmentPagerAdapter(fragmentManger) {
        companion object {
            private const val NO_OF_TABS = 3
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {

                1 -> Fragment()
                2 -> ProfileFragment()
                else -> MapFragment()
            }
        }

        override fun getCount(): Int = NO_OF_TABS
    }

    private fun resetQuitFlag() {
        Thread.sleep(2000)
        flagToQuitApp = false
    }

    override fun onBackPressed() {
        if (flagToQuitApp) {
            quitToast.cancel()
            super.onBackPressed()
        } else {
            quitToast = Toast.makeText(this,
                    "Please press back on more time to quit.",
                    Toast.LENGTH_SHORT)
            quitToast.duration
            quitToast.show()
            Single.fromCallable { resetQuitFlag() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            flagToQuitApp = true
        }
    }
}

package jumanji.sda.com.jumanji

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import jumanji.sda.com.jumanji.R.menu.options_menu
import kotlinx.android.synthetic.main.fragment_home_page.*

class ProgramActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home_page)

        val adapter = PagerAdapter(supportFragmentManager)
        container.adapter = adapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        val pinViewModel = ViewModelProviders.of(this)[PinViewModel::class.java]
        pinViewModel.queryDataFromFirebaseToRoom()

        val profileViewModel = ViewModelProviders.of(this)[ProfileViewModel::class.java]

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(itemSelected: MenuItem?) = when(itemSelected?.itemId){
        R.id.editProfileItem -> consume { editProfile()}
        R.id.signOutItem -> consume { signOut() }
        R.id.deleteProfileItem -> consume { deleteProfile() }

        else -> super.onOptionsItemSelected(itemSelected)
    }

    inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }

    fun editProfile() {
        val intent = Intent(this, CreateProfileActivity::class.java)
        startActivity(intent)
    }

    fun signOut(){
    }

    fun deleteProfile() {

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


}

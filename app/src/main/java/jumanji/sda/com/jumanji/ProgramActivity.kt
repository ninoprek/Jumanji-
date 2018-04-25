package jumanji.sda.com.jumanji

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_page.*

class ProgramActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home_page)

        val adapter = PagerAdapter(supportFragmentManager)
        container.adapter = adapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
    }

    class PagerAdapter(fragmentManger: FragmentManager) : FragmentPagerAdapter(fragmentManger) {
        companion object {
            private const val NO_OF_TABS = 3
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> CommunityFragment()
                2 -> ProfileFragment()
                else -> MapFragment()
            }
        }

        override fun getCount(): Int = NO_OF_TABS
    }

    class CommunityFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_community, container, false)
        }
    }

    class ProfileFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_profile, container, false)
        }
    }
}

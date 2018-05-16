package jumanji.sda.com.jumanji

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.constraint.ConstraintSet
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.*
import android.view.animation.OvershootInterpolator
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    val profileViewModel by lazy {
        ViewModelProviders.of(activity!!)[ProfileViewModel::class.java]
    }

    private var username: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        profileViewModel.userInfo?.observe(this, Observer {
            username = it?.userName
            if (username != null) usernameText.text = username
            Picasso.get().load(it?.photoURL).into(profilePhotoView)
        })

        profileViewModel.reportedPins.observe(this, Observer {
            userReportedText.text = it
            levelCheck()
        })

        profileViewModel.cleanedPins.observe(this, Observer {
            userClearedText.text = it
            levelCheck()
        })

        val statisticViewModel = ViewModelProviders.of(activity!!)[StatisticViewModel::class.java]
        statisticViewModel.getUpdateFromFirebase()
        statisticViewModel.averageUserReportedPins.observe(this, Observer {
            averageReportedText.text = it.toString()
            levelCheck()
        })

        statisticViewModel.averageUserCleanedPins.observe(this, Observer {
            averageClearedText.text = it.toString()
            levelCheck()
        })

        badgeView.setOnClickListener {
            updateConstraints(R.layout.fragment_profile_badge)
        }

        root.setOnClickListener {
            updateConstraints(R.layout.fragment_profile)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val statisticViewModel = ViewModelProviders.of(activity!!)[StatisticViewModel::class.java]
        when (item?.itemId) {
            R.id.signOutItem -> {
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle(R.string.app_name)
                builder.setMessage("Do you want to sign out?")
                builder.setPositiveButton("Yes") { dialog, id ->
                    dialog.dismiss()
                    profileViewModel.signOut()
                    goToSignIn()
                }
                builder.setNegativeButton("No") { dialog, id -> dialog.dismiss() }
                val alert = builder.create()
                alert.show()

            }
            R.id.editProfileItem -> {
                val intent = Intent(context, CreateProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.deleteProfileItem -> {
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle(R.string.app_name)
                builder.setMessage("Are you sure? Deleting your profile will cause loosing all your data!")
                builder.setPositiveButton("Yes") { dialog, id ->
                    dialog.dismiss()
                    profileViewModel.deleteUserProfile(username, context)
                    statisticViewModel.updateUsersNumberWhenDeleteProfile(StatisticRepository.TOTAL_USERS)
                    goToSignIn()
                }
                builder.setNegativeButton("No") { dialog, id -> dialog.dismiss() }
                val alert = builder.create()
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun levelCheck() {
        val userReport = userReportedText.text.toString().toInt()
        val userClean = userClearedText.text.toString().toInt()
        val averageReport = averageReportedText.text.toString().toInt()
        val averageClear = averageClearedText.text.toString().toInt()
        val sumOfAverage = averageReport + averageClear
        if (averageReport > 0 && averageClear > 0) {
            val clearScore = userClean * 1.5
            val userScore = userReport + clearScore
            when {
                userScore >= (sumOfAverage * 1.3) -> badgeView.setImageResource(R.drawable.tree)
                userScore < (sumOfAverage * 0.7) -> badgeView.setImageResource(R.drawable.logo1)
                else -> badgeView.setImageResource(R.drawable.branch)
            }
        }
    }

    private fun updateConstraints(@LayoutRes id: Int) {
        ConstraintSet().run {
            clone(context, id)
            applyTo(root)
        }
        val transition = ChangeBounds()
        transition.interpolator = OvershootInterpolator()
        TransitionManager.beginDelayedTransition(root, transition)
    }

    fun goToSignIn() {
        val intent = Intent(context, SignInActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}

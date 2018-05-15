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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileViewModel = ViewModelProviders.of(activity!!)[ProfileViewModel::class.java]

        profileViewModel.userInfo?.observe(this, Observer {
            usernameText.text = it?.userName
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

        signOutButton.setOnClickListener {
            val builder = AlertDialog.Builder(this.requireContext())
            builder.setTitle(R.string.app_name)
            builder.setMessage("Do you want to sign out?")
            builder.setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                profileViewModel.checkIfUserSignedIn(this.requireContext())
                goToSignIn()
            }
            builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            val alert = builder.create()
            alert.show()
        }

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

    private fun levelCheck() {
        val userReport = userReportedText.text.toString().toInt()
        val userClean = userClearedText.text.toString().toInt()
        val averageReport = averageReportedText.text.toString().toInt()
        val averageClear = averageClearedText.text.toString().toInt()
        if (averageReport > 0 && averageClear > 0) {
            val reportScore = (userReport - averageReport) / averageReport.toFloat() * 100
            val clearScore = (userClean - averageClear) / averageClear.toFloat() * 100
            val averageScore = (reportScore + clearScore) / 2
            when {
                averageScore >= 30 -> badgeView.setImageResource(R.drawable.tree)
                averageScore < -30 -> badgeView.setImageResource(R.drawable.logo1)
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

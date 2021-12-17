package com.onirun.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.onirun.R
import com.onirun.screens.settings.SettingsActivity
import com.onirun.views.AvatarView

/**
 * Created by Aurelien Lubecki
 * on 19/03/2018.
 */
abstract class BaseMenuFragment : BaseLoadingFragment() {


    private lateinit var avatarView: AvatarView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatarView = view.findViewById(R.id.buttonAvatar)

        avatarView.setOnClickListener {

            if (context != null) {
                SettingsActivity.start(requireContext())
            }
        }
    }

    final override fun onResume() {
        super.onResume()

        if (!isHidden) {
            //the resume() method will be called after in onHiddenChanged(hidden: Boolean)
            resume()

            //the letter in avatar view may have changed
            avatarView.updateUI()
        }
    }

    final override fun onPause() {
        super.onPause()

        pause()
    }

    final override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            resume()
        } else {
            pause()
        }
    }

    open fun resume() {

    }

    open fun pause() {

    }

    override fun startActivity(intent: Intent?) {

        val a = activity as? BaseAnimatedActivity

        if (a == null) {
            //start default activity
            super.startActivity(intent)
        } else {
            //play animations of the AnimatedActivity
            activity?.startActivity(intent)
        }
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {

        val a = activity as? BaseAnimatedActivity

        if (a == null) {
            //start default activity
            super.startActivityForResult(intent, requestCode)
        } else {
            //play animations of the AnimatedActivity
            activity?.startActivityForResult(intent, requestCode)
        }
    }

}
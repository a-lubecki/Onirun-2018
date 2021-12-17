package com.onirun.test

import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import com.onirun.BuildConfig
import com.onirun.api.APIManager
import com.onirun.utils.setVisible


class APIDebugButtons(private val contentView: ViewGroup) {


    private lateinit var buttonAPITestMode: Button

    private lateinit var buttonAPILogAlert: Button


    fun init() {

        val context = contentView.context

        val paramsAPITestMode = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        paramsAPITestMode.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        paramsAPITestMode.bottomMargin = 500

        buttonAPITestMode = Button(context)
        buttonAPITestMode.layoutParams = paramsAPITestMode

        val paramsAPILogAlert = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        paramsAPILogAlert.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        paramsAPILogAlert.bottomMargin = 300

        buttonAPILogAlert = Button(context)
        buttonAPILogAlert.layoutParams = paramsAPILogAlert

        val layout = RelativeLayout(context)
        layout.addView(buttonAPITestMode)
        layout.addView(buttonAPILogAlert)
        layout.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)

        contentView.addView(layout)


        updateDebugButtons()

        buttonAPITestMode.setOnClickListener {

            APIManager.isAPITestModeEnabled = !APIManager.isAPITestModeEnabled
            updateDebugButtons()
        }

        buttonAPILogAlert.setOnClickListener {

            APIManager.isAPILogAlertEnabled = !APIManager.isAPILogAlertEnabled
            updateDebugButtons()
        }

    }

    private fun updateDebugButtons() {

        buttonAPITestMode.setVisible(BuildConfig.CAN_DEBUG_API)
        buttonAPITestMode.text = if (APIManager.isAPITestModeEnabled) "Repasser l'API en mode serveur" else "Passer l'API en mode local"

        buttonAPILogAlert.setVisible(BuildConfig.CAN_DEBUG_API)
        buttonAPILogAlert.text = if (APIManager.isAPILogAlertEnabled) "Désactiver les diffs de JSON" else "Afficher les diffs de JSON"
    }


}
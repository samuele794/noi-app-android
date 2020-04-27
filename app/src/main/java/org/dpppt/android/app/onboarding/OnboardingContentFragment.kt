/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.onboarding

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_content.view.*
import org.dpppt.android.app.R

class OnboardingContentFragment : Fragment(R.layout.fragment_onboarding_content) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        if (args.containsKey(ARG_RES_TITLE)) {
            view.onboarding_title.setText(args.getInt(ARG_RES_TITLE))
        }
        if (args.containsKey(ARG_RES_DESCRIPTION)) {
            view.onboarding_description.setText(args.getInt(ARG_RES_DESCRIPTION))
        }
        if (args.containsKey(ARG_RES_ILLUSTRATION)) {
            view.onboarding_illustration.setImageResource(args.getInt(ARG_RES_ILLUSTRATION))
        }
    }

    companion object {
        private const val ARG_RES_TITLE = "RES_TITLE"
        private const val ARG_RES_DESCRIPTION = "RES_DESCRIPTION"
        private const val ARG_RES_ILLUSTRATION = "RES_ILLUSTRATION"
        fun newInstance(@StringRes title: Int,
                        @StringRes description: Int,
                        @DrawableRes illustration: Int): OnboardingContentFragment {
            return OnboardingContentFragment().run {
                arguments = Bundle().apply {
                    putInt(ARG_RES_TITLE, title)
                    putInt(ARG_RES_DESCRIPTION, description)
                    putInt(ARG_RES_ILLUSTRATION, illustration)
                }
                this
            }
        }
    }
}
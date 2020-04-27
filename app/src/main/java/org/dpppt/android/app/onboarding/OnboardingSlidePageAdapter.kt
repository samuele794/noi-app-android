/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.dpppt.android.app.R

class OnboardingSlidePageAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return OnboardingContentFragment.newInstance(R.string.onboarding_title_1, R.string.onboarding_desc_1, R.drawable.ill_isolation)
            1 -> return OnboardingContentFragment.newInstance(R.string.onboarding_title_2, R.string.onboarding_desc_2, R.drawable.ill_privacy)
            2 -> return OnboardingContentFragment.newInstance(R.string.onboarding_title_3, R.string.onboarding_desc_3, R.drawable.ill_distancing)
            SCREEN_INDEX_PERMISSIONS -> return OnboardingPermissionFragment.newInstance()
            4 -> return OnboardingContentFragment.newInstance(R.string.onboarding_title_5, R.string.onboarding_desc_5, R.drawable.ill_distancing)
        }
        throw IllegalArgumentException("There is no fragment for view pager position $position")
    }

    override fun getItemCount(): Int {
        return 5
    }

    companion object {
        const val SCREEN_INDEX_PERMISSIONS = 3
    }
}
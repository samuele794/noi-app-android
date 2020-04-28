/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.dpppt.android.app.R
import org.dpppt.android.app.onboarding.OnboardingActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val preferences = getSharedPreferences(PREFS_COVID, Context.MODE_PRIVATE)
            val onboardingCompleted = preferences.getBoolean(PREF_KEY_ONBOARDING_COMPLETED, false)
            if (onboardingCompleted) {
                showMainFragment()
            } else {
                startActivityForResult(Intent(this, OnboardingActivity::class.java), REQ_ONBOARDING)
            }
        }
    }

    private fun showMainFragment() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, MainFragment.newInstance())
                .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONBOARDING) {
            if (resultCode == Activity.RESULT_OK) {
                val preferences = getSharedPreferences(PREFS_COVID, Context.MODE_PRIVATE)
                preferences.edit().putBoolean(PREF_KEY_ONBOARDING_COMPLETED, true).apply()
                showMainFragment()
            } else {
                finish()
            }
        }
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        for (frag in fm.fragments) {
            if (frag.isVisible) {
                val childFm = frag.childFragmentManager
                if (childFm.backStackEntryCount > 0) {
                    childFm.popBackStack()
                    return
                }
            }
        }
        super.onBackPressed()
    }

    companion object {
        private const val PREFS_COVID = "PREFS_COVID"
        private const val PREF_KEY_ONBOARDING_COMPLETED = "PREF_KEY_ONBOARDING_COMPLETED"
        private const val REQ_ONBOARDING = 123
    }
}
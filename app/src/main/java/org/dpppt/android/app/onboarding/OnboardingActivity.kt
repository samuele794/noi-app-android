/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.onboarding

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.dpppt.android.app.R
import org.dpppt.android.sdk.DP3T

class OnboardingActivity : FragmentActivity() {

    private lateinit var pagerAdapter: FragmentStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        pagerAdapter = OnboardingSlidePageAdapter(this)

        pager.adapter = pagerAdapter
        pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateStepButton()
            }
        })

        TabLayoutMediator(onboarding_tab_dots, pager, TabConfigurationStrategy { tab: TabLayout.Tab, _ -> tab.select() }).attach()

        onboarding_fab.setOnClickListener(View.OnClickListener {
            val currentItem = pager.currentItem
            if (currentItem < pagerAdapter.itemCount - 1) {
                pager.setCurrentItem(currentItem + 1, true)
            } else {
                DP3T.start(this)
                setResult(Activity.RESULT_OK)
                finish()
            }
        })
    }

    fun updateStepButton() {
        onboarding_fab.isEnabled = (checkPermissionsReady()
                || pager.currentItem < OnboardingSlidePageAdapter.SCREEN_INDEX_PERMISSIONS)
    }

    private fun checkPermissionsReady(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothEnabled = bluetoothAdapter != null && bluetoothAdapter.isEnabled
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryOptDeact = powerManager.isIgnoringBatteryOptimizations(this.packageName)
        val locationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        return bluetoothEnabled && batteryOptDeact && locationGranted
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            pager.currentItem = pager.currentItem - 1
        }
    }
}
/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.onboarding

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_permission.*
import org.dpppt.android.app.R
import org.dpppt.android.app.util.DeviceFeatureHelper
import org.dpppt.android.app.util.InfoDialog

class OnboardingPermissionFragment : Fragment(R.layout.fragment_onboarding_permission) {

    private val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

    private val bluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                updateButtonStatus()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        onboarding_location_permission_button.setOnClickListener(View.OnClickListener { v: View? ->
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION)
        })

        onboarding_location_permission_info.setOnClickListener { v: View? ->
            InfoDialog.newInstance(R.string.onboarding_android_location_permission_info)
                    .show(childFragmentManager, InfoDialog::class.qualifiedName)
        }

        onboarding_battery_button.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + requireContext().packageName)))
        })

        onboarding_battery_info.setOnClickListener { v: View? ->
            InfoDialog.newInstance(R.string.onboarding_android_battery_saving_info)
                    .show(childFragmentManager, InfoDialog::class.qualifiedName)
        }

        onboarding_bluetooth_button.setOnClickListener(View.OnClickListener { v: View ->
            Toast.makeText(v.context, getString(R.string.activate_bluetooth_button) + " ...", Toast.LENGTH_SHORT).show()
            BluetoothAdapter.getDefaultAdapter()?.enable()
        })
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(bluetoothStateReceiver, filter)
        updateButtonStatus()
    }

    private fun updateButtonStatus() {
        if (DeviceFeatureHelper.isLocationPermissionGranted(requireContext())) {
            setButtonOk(onboarding_location_permission_button, R.string.button_permission_location_granted_android)
        } else {
            setButtonDefault(onboarding_location_permission_button, R.string.button_permission_location_android)
        }

        if (DeviceFeatureHelper.isBatteryOptimizationDeactivated(requireContext())) {
            setButtonOk(onboarding_battery_button, R.string.button_battery_optimization_deactivated)
        } else {
            setButtonDefault(onboarding_battery_button, R.string.button_battery_optimization)
        }

        if (DeviceFeatureHelper.isBluetoothEnabled()) {
            setButtonOk(onboarding_bluetooth_button, R.string.bluetooth_activated_label)
        } else {
            setButtonDefault(onboarding_bluetooth_button, R.string.activate_bluetooth_button)
        }

        (requireActivity() as OnboardingActivity).updateStepButton()
    }

    private fun setButtonDefault(button: Button, @StringRes buttonLabel: Int) {
        button.apply {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            setTextColor(Color.WHITE)
            setText(buttonLabel)
            isClickable = true
            elevation = resources.getDimensionPixelSize(R.dimen.button_elevation).toFloat()
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue_main)
        }

    }

    private fun setButtonOk(button: Button, @StringRes grantedLabel: Int) {
        button.apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0)
            setTextColor(resources.getColor(R.color.green_main, requireActivity().theme))
            setText(grantedLabel)
            isClickable = false
            elevation = 0f
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().unregisterReceiver(bluetoothStateReceiver)
    }

    companion object {
        private const val REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION = 123
        fun newInstance(): OnboardingPermissionFragment {
            return OnboardingPermissionFragment()
        }
    }
}
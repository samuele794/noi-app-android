/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.contacts

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_contacts.*
import org.dpppt.android.app.R
import org.dpppt.android.app.main.TracingViewModel
import org.dpppt.android.app.util.DeviceFeatureHelper.isBatteryOptimizationDeactivated
import org.dpppt.android.app.util.DeviceFeatureHelper.isLocationPermissionGranted
import org.dpppt.android.app.util.TracingStatusHelper
import org.dpppt.android.app.util.TracingStatusHelper.updateStatusView
import org.dpppt.android.sdk.TracingStatus
import org.dpppt.android.sdk.TracingStatus.ErrorState

class ContactsFragment : Fragment(R.layout.fragment_contacts) {

    private lateinit var tracingViewModel: TracingViewModel
    private var requestedSomething = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracingViewModel = ViewModelProvider(requireActivity()).get(TracingViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        contacts_toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        contacts_tracking_switch.setOnClickListener {
            tracingViewModel.setTracingEnabled(contacts_tracking_switch.isChecked)
        }
        tracingViewModel.errorsLiveData.observe(viewLifecycleOwner, Observer { errorStates: List<ErrorState?> ->
            contacts_tracking_switch.isEnabled = errorStates.isEmpty()
        })
        tracingViewModel.tracingStatusLiveData.observe(viewLifecycleOwner, Observer { status: TracingStatus ->
            val running = status.isAdvertising && status.isReceiving
            contacts_tracking_switch.isChecked = running
            val state =
                    if (status.errors.size > 0 || !running)
                        TracingStatusHelper.State.WARNING
                    else
                        TracingStatusHelper.State.OK
            val titleRes =
                    if (state === TracingStatusHelper.State.OK)
                        R.string.tracing_active_title
                    else
                        R.string.tracing_error_title
            val textRes =
                    if (state === TracingStatusHelper.State.OK)
                        R.string.tracing_active_text
                    else
                        R.string.tracing_error_text
            updateStatusView(contacts_status, state, titleRes, textRes)
            invalidateErrorResolverButtons()
        })

        contact_location_permission_button.setOnClickListener(View.OnClickListener { v: View? ->
            requestedSomething = true
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissions, 1)
        })

        contact_battery_button.setOnClickListener(View.OnClickListener { v: View? ->
            requestedSomething = true
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + requireContext().packageName)))
        })

        contact_bluetooth_button.setOnClickListener(View.OnClickListener { v: View ->
            Toast.makeText(v.context, getString(R.string.activate_bluetooth_button) + " ...", Toast.LENGTH_SHORT).show()
            BluetoothAdapter.getDefaultAdapter().enable()
        })
        tracingViewModel.bluetoothEnabledLiveData.observe(viewLifecycleOwner, Observer { bluetoothEnabled: Boolean ->
            contact_bluetooth_button.visibility = if (bluetoothEnabled) View.GONE else View.VISIBLE
        })
        invalidateErrorResolverButtons()
    }

    override fun onResume() {
        super.onResume()
        if (requestedSomething) {
            tracingViewModel.invalidateService()
            invalidateErrorResolverButtons()
            requestedSomething = false
        }
    }

    private fun invalidateErrorResolverButtons() {
        val locationPermissionGranted = isLocationPermissionGranted(requireContext())
        contact_location_permission_button.visibility =
                if (locationPermissionGranted)
                    View.GONE
                else
                    View.VISIBLE
        val batteryOptDeactivated = isBatteryOptimizationDeactivated(requireContext())
        contact_battery_button.visibility = if (batteryOptDeactivated) View.GONE else View.VISIBLE
    }

    companion object {
        fun newInstance(): ContactsFragment {
            return ContactsFragment()
        }
    }
}
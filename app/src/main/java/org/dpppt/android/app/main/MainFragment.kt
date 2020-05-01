/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.card_contacts.*
import kotlinx.android.synthetic.main.card_notifications.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.dpppt.android.app.R
import org.dpppt.android.app.contacts.ContactsFragment
import org.dpppt.android.app.debug.DebugFragment
import org.dpppt.android.app.main.model.AppState
import org.dpppt.android.app.main.views.HeaderView
import org.dpppt.android.app.notifications.NotificationsFragment
import org.dpppt.android.app.trigger.TriggerFragment
import org.dpppt.android.app.util.DebugUtils
import org.dpppt.android.app.util.TracingStatusHelper
import org.dpppt.android.app.util.TracingStatusHelper.updateStatusView


class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var tracingViewModel: TracingViewModel
    private lateinit var headerView: HeaderView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracingViewModel = ViewModelProvider(requireActivity()).get(TracingViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupHeader(view)
        setupCards(view)
        setupDebugButton(view)
    }

    override fun onStart() {
        super.onStart()
        tracingViewModel.invalidateTracingStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        headerView.stopArcAnimation()
    }

    private fun setupHeader(view: View) {
        headerView = view.findViewById(R.id.main_header_container)
        tracingViewModel.appStateLiveData
                .observe(viewLifecycleOwner, Observer { appState: AppState -> headerView.setState(appState) })
    }

    private fun setupCards(view: View) {
        card_contacts.setOnClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, ContactsFragment.newInstance())
                    .addToBackStack(ContactsFragment::class.java.canonicalName)
                    .commit()
        }

        tracingViewModel.tracingEnabledLiveData.observe(viewLifecycleOwner,
                Observer { isTracing: Boolean ->
                    tracingViewModel.errorsLiveData.value?.let { errors ->
                        val state = if (errors.isNotEmpty() || !isTracing)
                            TracingStatusHelper.State.WARNING
                        else
                            TracingStatusHelper.State.OK
                        val titleRes = if (state === TracingStatusHelper.State.OK)
                            R.string.tracing_active_title
                        else
                            R.string.tracing_error_title
                        val textRes = if (state === TracingStatusHelper.State.OK)
                            R.string.tracing_active_text
                        else
                            R.string.tracing_error_text

                        updateStatusView(contacts_status, state, titleRes, textRes)
                    }

                })

        card_notifications.setOnClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, NotificationsFragment.newInstance())
                    .addToBackStack(NotificationsFragment::class.java.canonicalName)
                    .commit()
        }

        val notificationStatusView = notifications_status_bubble.findViewById<View>(R.id.notification_status)
        val buttonInform = view.findViewById<View>(R.id.main_button_inform)

        buttonInform.setOnClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, TriggerFragment.newInstance())
                    .addToBackStack(TriggerFragment::class.java.canonicalName)
                    .commit()
        }

        tracingViewModel.selfOrContactExposedLiveData.observe(viewLifecycleOwner,
                Observer { selfOrContactExposed: Pair<Boolean, Boolean> ->
                    val isExposed = selfOrContactExposed.first || selfOrContactExposed.second
                    buttonInform.visibility = if (!selfOrContactExposed.first) View.VISIBLE else View.GONE
                    val state = if (!isExposed) TracingStatusHelper.State.OK else TracingStatusHelper.State.INFO
                    val title = if (isExposed) {
                        if (selfOrContactExposed.first)
                            R.string.meldungen_infected_title
                        else
                            R.string.meldungen_meldung_title
                    } else
                        R.string.meldungen_no_meldungen_title
                    val text = if (isExposed) {
                        if (selfOrContactExposed.first)
                            R.string.meldungen_infected_text
                        else
                            R.string.meldungen_meldung_text
                    } else
                        R.string.meldungen_no_meldungen_text

                    context?.let {
                        notifications_status_bubble.backgroundTintList = ColorStateList
                                .valueOf(it.getColor(if (isExposed) R.color.status_blue else R.color.status_green_bg))
                    }

                    updateStatusView(notificationStatusView, state, title, text)
                })
    }

    private fun setupDebugButton(view: View) {
        if (DebugUtils.isDev) {
            main_button_debug.visibility = View.VISIBLE;
            main_button_debug.setOnClickListener {
                parentFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, DebugFragment.newInstance())
                        .addToBackStack(DebugFragment::class.java.canonicalName)
                        .commit()
            }
        } else {
            main_button_debug.visibility = View.GONE;
        }


    }

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}
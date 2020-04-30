/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.notifications

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.dpppt.android.app.R
import org.dpppt.android.app.main.TracingViewModel
import org.dpppt.android.app.util.PhoneUtil.callHelpline
import org.dpppt.android.app.util.TracingStatusHelper
import org.dpppt.android.app.util.TracingStatusHelper.updateStatusView

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {
    private lateinit var tracingViewModel: TracingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracingViewModel = ViewModelProvider(requireActivity()).get(TracingViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        notifications_toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        notifications_call_hotline_button.setOnClickListener {
            callHelpline(it.context)
        }

        tracingViewModel.selfOrContactExposedLiveData.observe(viewLifecycleOwner,
                Observer { selfOrContactExposed: Pair<Boolean, Boolean> ->
                    val isExposed = selfOrContactExposed.first || selfOrContactExposed.second
                    val state = if (!isExposed)
                        TracingStatusHelper.State.OK
                    else
                        TracingStatusHelper.State.INFO

                    val title =
                            if (isExposed) {
                                if (selfOrContactExposed.first)
                                    R.string.meldungen_infected_title
                                else
                                    R.string.meldungen_meldung_title
                            } else
                                R.string.meldungen_no_meldungen_title

                    val text =
                            if (isExposed) {
                                if (selfOrContactExposed.first)
                                    R.string.meldungen_infected_text
                                else
                                    R.string.meldungen_meldung_text
                            } else
                                R.string.meldungen_no_meldungen_text

                    val bubbleColor = ColorStateList.valueOf(requireContext().getColor(
                            if (isExposed)
                                R.color.status_blue
                            else
                                R.color.status_green_bg)
                    )

                    updateStatusView(notifications_status_view, state, title, text)

                    notifications_bubble.backgroundTintList = bubbleColor
                    notifications_bubble_triangle.imageTintList = bubbleColor
                    notifications_status_exposed_layout.background =
                            if (isExposed)
                                resources.getDrawable(R.drawable.bg_status_bubble_stroke_grey, null)
                            else
                                null

                    notifications_exposed_information_group.visibility =
                            if (isExposed)
                                VISIBLE
                            else
                                GONE


                    if (isExposed) {
                        notifications_info_text_specific.setText(
                                if (selfOrContactExposed.first)
                                    R.string.meldungen_hinweis_info_text1_infected
                                else
                                    R.string.meldungen_hinweis_info_text1)
                    }
                })
    }

    companion object {

        fun newInstance(): NotificationsFragment {
            return NotificationsFragment()
        }
    }
}
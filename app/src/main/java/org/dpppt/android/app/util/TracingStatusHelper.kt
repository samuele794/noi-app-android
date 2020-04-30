/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.util

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.dpppt.android.app.R

object TracingStatusHelper {

    fun updateStatusView(statusView: View, state: State, @StringRes title: Int, @StringRes text: Int) {
        val context = statusView.context
        statusView.findViewById<ImageView>(R.id.status_icon).apply {
            setImageResource(state.iconResource)
            imageTintList = ColorStateList.valueOf(context.getColor(state.titleColor))
        }

        statusView.findViewById<TextView>(R.id.status_title).apply {
            setText(title)
            setTextColor(context.getColor(state.titleColor))
        }
        statusView.findViewById<TextView>(R.id.status_text).apply {
            setText(text)
            setTextColor(context.getColor(state.textColor))
        }

    }

    enum class State(@param:ColorRes internal val titleColor: Int,
                     @param:ColorRes internal val textColor: Int,
                     @param:DrawableRes internal val iconResource: Int) {
        OK(R.color.green_main, R.color.dark_main, R.drawable.ic_check),
        INFO(R.color.white, R.color.white, R.drawable.ic_info),
        WARNING(R.color.status_red, R.color.status_red, R.drawable.ic_warning);

    }
}
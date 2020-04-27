/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.dpppt.android.app.R

object PhoneUtil {
    @JvmStatic
    fun callHelpline(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:" + context.getString(R.string.tel_hotline))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}
package org.dpppt.android.app.util

import org.dpppt.android.app.BuildConfig

object DebugUtils {
    val isDev: Boolean
        get() = BuildConfig.IS_DEV
}
package org.dpppt.android.app.main.model

import org.dpppt.android.app.debug.model.DebugAppState


interface TracingStatusInterface {
    fun isReportedAsExposed(): Boolean

    fun wasContactExposed(): Boolean

    fun setDebugAppState(debugAppState: DebugAppState)

    fun getDebugAppState(): DebugAppState?

    fun getAppState(): AppState
}
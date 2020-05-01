package org.dpppt.android.app.debug

import org.dpppt.android.app.debug.model.DebugAppState
import org.dpppt.android.app.main.model.AppState
import org.dpppt.android.app.main.model.TracingStatusInterface
import org.dpppt.android.sdk.TracingStatus

class TracingStatusWrapper(debugAppState: DebugAppState) : TracingStatusInterface {

    private var debugAppState = DebugAppState.NONE
    private var status: TracingStatus? = null

    fun setStatus(status: TracingStatus?) {
        this.status = status
    }

    override fun isReportedAsExposed(): Boolean {
        return status?.isReportedAsExposed ?: false || debugAppState === DebugAppState.REPORTED_EXPOSED
    }

    override fun wasContactExposed(): Boolean {
        return status?.wasContactExposed() ?: false || debugAppState === DebugAppState.CONTACT_EXPOSED
    }

    override fun setDebugAppState(debugAppState: DebugAppState) {
        this.debugAppState = debugAppState
    }

    override fun getDebugAppState(): DebugAppState {
        return debugAppState
    }

    override fun getAppState(): AppState {
        status?.let { status ->
            val hasError = status.errors.size > 0 || !(status.isAdvertising || status.isReceiving)
            return when (debugAppState) {
                DebugAppState.NONE -> {
                    when {
                        status.isReportedAsExposed || status.wasContactExposed() -> {
                            if (hasError) AppState.EXPOSED_ERROR else AppState.EXPOSED
                        }
                        hasError -> AppState.ERROR
                        else -> AppState.TRACING
                    }
                }
                DebugAppState.HEALTHY -> if (hasError) AppState.ERROR else AppState.TRACING
                DebugAppState.REPORTED_EXPOSED, DebugAppState.CONTACT_EXPOSED -> if (hasError) AppState.EXPOSED_ERROR else AppState.EXPOSED
            }
        }
        return AppState.ERROR
    }

    init {
        this.debugAppState = debugAppState
    }
}
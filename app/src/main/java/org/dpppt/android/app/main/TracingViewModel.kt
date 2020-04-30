/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.dpppt.android.app.main.model.AppState
import org.dpppt.android.app.util.DeviceFeatureHelper.isBluetoothEnabled
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.TracingStatus
import org.dpppt.android.sdk.TracingStatus.ErrorState

class TracingViewModel(application: Application) : AndroidViewModel(application) {
    internal val tracingStatusLiveData = MutableLiveData<TracingStatus>()
    private val tracingStatusBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            invalidateTracingStatus()
        }
    }
    internal val tracingEnabledLiveData = MutableLiveData<Boolean>()
    private val exposedLiveData = MutableLiveData<Pair<Boolean, Boolean>>()
    private val numberOfHandshakesLiveData = MutableLiveData(0)
    internal val errorsLiveData = MutableLiveData<List<ErrorState>>(emptyList())
    internal val appStateLiveData = MutableLiveData<AppState>()
    internal val bluetoothEnabledLiveData = MutableLiveData<Boolean>()
    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
                invalidateBluetoothState()
                invalidateTracingStatus()
            }
        }
    }

    fun invalidateTracingStatus() {
        val status = DP3T.getStatus(getApplication())
        tracingStatusLiveData.value = status
    }

    fun getTracingEnabledLiveData(): LiveData<Boolean> {
        return tracingEnabledLiveData
    }

    val selfOrContactExposedLiveData: LiveData<Pair<Boolean, Boolean>>
        get() = exposedLiveData

    fun getErrorsLiveData(): LiveData<List<ErrorState>> {
        return errorsLiveData
    }

    fun getAppStateLiveData(): LiveData<AppState> {
        return appStateLiveData
    }

    fun getTracingStatusLiveData(): LiveData<TracingStatus> {
        return tracingStatusLiveData
    }

    fun getBluetoothEnabledLiveData(): LiveData<Boolean> {
        return bluetoothEnabledLiveData
    }

    fun setTracingEnabled(enabled: Boolean) {
        if (enabled) {
            DP3T.start(getApplication())
        } else {
            DP3T.stop(getApplication())
        }
    }

    fun invalidateService() {
        tracingEnabledLiveData.value?.let {
            if (it) {
                DP3T.stop(getApplication())
                DP3T.start(getApplication())
            }
        }
    }

    private fun invalidateBluetoothState() {
        bluetoothEnabledLiveData.value = isBluetoothEnabled
    }

    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(tracingStatusBroadcastReceiver)
        getApplication<Application>().unregisterReceiver(bluetoothReceiver)
    }

    init {
        tracingStatusLiveData.observeForever { status: TracingStatus ->
            tracingEnabledLiveData.value = status.isAdvertising && status.isReceiving
            numberOfHandshakesLiveData.value = status.numberOfHandshakes
            exposedLiveData.value = Pair(status.isReportedAsExposed, status.wasContactExposed())
            errorsLiveData.value = status.errors
            val hasError = status.errors.size > 0 || !(status.isAdvertising || status.isReceiving)
            if (status.isReportedAsExposed || status.wasContactExposed()) {
                appStateLiveData.setValue(if (hasError) AppState.EXPOSED_ERROR else AppState.EXPOSED)
            } else if (hasError) {
                appStateLiveData.setValue(AppState.ERROR)
            } else {
                appStateLiveData.setValue(AppState.TRACING)
            }
        }
        invalidateBluetoothState()
        invalidateTracingStatus()
        application.registerReceiver(tracingStatusBroadcastReceiver, DP3T.getUpdateIntentFilter())
        application.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }
}
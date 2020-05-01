package org.dpppt.android.app.debug

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_debug.*
import org.dpppt.android.app.R
import org.dpppt.android.app.debug.model.DebugAppState
import org.dpppt.android.app.main.TracingViewModel
import org.dpppt.android.app.util.InfoDialog
import org.dpppt.android.sdk.TracingStatus
import java.text.SimpleDateFormat
import java.util.*

class DebugFragment : Fragment(R.layout.fragment_debug) {

    private lateinit var tracingViewModel: TracingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracingViewModel = ViewModelProvider(requireActivity()).get(TracingViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contacts_toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        setupSdkViews(view)
        setupStateOptions(view)
    }

    private fun setupSdkViews(view: View) {
        tracingViewModel.getTracingStatusLiveData().observe(viewLifecycleOwner, Observer { status: TracingStatus ->
            debug_sdk_state_text.text = formatStatusString(status)
            val isTracing = (status.isAdvertising || status.isReceiving) && status.errors.size == 0
            debug_sdk_state_text.backgroundTintList = ColorStateList.valueOf(
                    if (isTracing) resources.getColor(R.color.status_green_bg, null) else resources.getColor(R.color.status_red_bg, null))
        })
        debug_button_reset.setOnClickListener {
            val progressDialog = AlertDialog.Builder(context)
                    .setView(R.layout.dialog_loading)
                    .show()
            tracingViewModel.resetSdk(Runnable {
                progressDialog.dismiss()
                InfoDialog.newInstance(R.string.debug_sdk_reset_text)
                        .show(childFragmentManager, InfoDialog::class.simpleName)
                updateRadioGroup(debug_state_options_group)
            })
        }
    }

    private fun setupStateOptions(view: View) {
        val optionsGroup = view.findViewById<RadioGroup>(R.id.debug_state_options_group)
        optionsGroup.setOnCheckedChangeListener { _, checkedId: Int ->
            when (checkedId) {
                R.id.debug_state_option_none -> tracingViewModel.debugAppState = DebugAppState.NONE
                R.id.debug_state_option_healthy -> tracingViewModel.debugAppState = DebugAppState.HEALTHY
                R.id.debug_state_option_exposed -> tracingViewModel.debugAppState = DebugAppState.CONTACT_EXPOSED
                R.id.debug_state_option_infected -> tracingViewModel.debugAppState = DebugAppState.REPORTED_EXPOSED
            }
        }
        updateRadioGroup(optionsGroup)
    }

    private fun updateRadioGroup(optionsGroup: RadioGroup) {

        when (tracingViewModel.debugAppState) {
            DebugAppState.NONE -> R.id.debug_state_option_none
            DebugAppState.HEALTHY -> R.id.debug_state_option_healthy
            DebugAppState.CONTACT_EXPOSED -> R.id.debug_state_option_exposed
            DebugAppState.REPORTED_EXPOSED -> R.id.debug_state_option_infected
        }.let { preSetId ->
            optionsGroup.check(preSetId)
        }
    }

    private fun formatStatusString(status: TracingStatus): SpannableString {
        return SpannableStringBuilder().run {

            val isTracing = (status.isAdvertising || status.isReceiving) && status.errors.size == 0
            append(getString(if (isTracing) R.string.tracing_active_title else R.string.tracing_error_title)).append("\n")
                    .setSpan(StyleSpan(Typeface.BOLD), 0, length - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            val lastSyncDateUTC = status.lastSyncDate
            val lastSyncDateString = if (lastSyncDateUTC > 0) DATE_FORMAT_SYNC.format(Date(lastSyncDateUTC)) else "n/a"

            append(getString(R.string.debug_sdk_state_last_synced))
            append(lastSyncDateString).append("\n")
            append(getString(R.string.debug_sdk_state_self_exposed))
            append(getBooleanDebugString(status.isReportedAsExposed)).append("\n")
            append(getString(R.string.debug_sdk_state_contact_exposed))
            append(getBooleanDebugString(status.wasContactExposed())).append("\n")
            append(getString(R.string.debug_sdk_state_number_handshakes))
            append(status.numberOfHandshakes.toString())

            val errors = status.errors
            if (errors != null && errors.size > 0) {
                val start = length
                append("\n")
                for (error in errors) {
                    append("\n").append(error.toString())
                }
                setSpan(ForegroundColorSpan(resources.getColor(R.color.red_main, null)),
                        start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            SpannableString(this)
        }
    }

    private fun getBooleanDebugString(value: Boolean): String {
        return getString(if (value) R.string.debug_sdk_state_boolean_true else R.string.debug_sdk_state_boolean_false)
    }

    companion object {
        private val DATE_FORMAT_SYNC = SimpleDateFormat.getDateTimeInstance()
        fun startDebugFragment(parentFragmentManager: FragmentManager) {
            parentFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, newInstance())
                    .addToBackStack(DebugFragment::class.java.canonicalName)
                    .commit()
        }

        fun newInstance(): DebugFragment {
            return DebugFragment()
        }
    }
}
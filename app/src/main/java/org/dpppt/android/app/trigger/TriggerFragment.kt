/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.trigger

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_trigger.*
import org.dpppt.android.app.R
import org.dpppt.android.app.trigger.NoCodeFragment
import org.dpppt.android.app.trigger.ThankYouFragment
import org.dpppt.android.app.trigger.views.ChainedEditText.ChainedEditTextListener
import org.dpppt.android.app.util.InfoDialog
import org.dpppt.android.app.util.InfoDialog.Companion.newInstance
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.internal.backend.CallbackListener
import org.dpppt.android.sdk.internal.backend.ResponseException
import org.dpppt.android.sdk.internal.backend.models.ExposeeAuthData
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

class TriggerFragment : Fragment(R.layout.fragment_trigger) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trigger_fragment_input.addTextChangedListener(object : ChainedEditTextListener {
            override fun onTextChanged(input: String) {
                trigger_fragment_button_trigger.isEnabled = input.matches(Regex(REGEX_CODE_PATTERN))
            }

            override fun onEditorSendAction() {
                if (trigger_fragment_button_trigger.isEnabled) trigger_fragment_button_trigger.callOnClick()
            }
        })
        trigger_fragment_button_trigger.setOnClickListener { v: View ->
            // TODO: HARDCODED ONSET DATE - NOT FINAL
            val calendarNow: Calendar = GregorianCalendar()
            calendarNow.add(Calendar.DAY_OF_YEAR, -14)
            val progressDialog = AlertDialog.Builder(v.context)
                    .setView(R.layout.dialog_loading)
                    .show()
            val inputBase64 = String(Base64.encode(trigger_fragment_input.text.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP),
                    StandardCharsets.UTF_8)

            DP3T.sendIWasExposed(v.context,
                    Date(calendarNow.timeInMillis),
                    ExposeeAuthData(inputBase64), object : CallbackListener<Void?> {
                override fun onSuccess(response: Void?) {
                    progressDialog.dismiss()
                    parentFragmentManager.beginTransaction()
                            .replace(R.id.main_fragment_container, ThankYouFragment.newInstance())
                            .addToBackStack(ThankYouFragment::class.qualifiedName)
                            .commit()
                }

                override fun onError(throwable: Throwable) {
                    progressDialog.dismiss()

                    val errorTextException = when (throwable) {
                        is ResponseException -> throwable.message ?: ""
                        is IOException -> throwable.localizedMessage
                        else -> ""
                    }

                    val error = getString(R.string.unexpected_error_title, errorTextException)
                    newInstance(error)
                            .show(childFragmentManager, InfoDialog::class.qualifiedName)
                    Log.e(TriggerFragment::class.simpleName, "Send Exposed Message Error: ", throwable)
                }
            })
        }
        cancel_button.setOnClickListener { activity?.onBackPressed() }
        trigger_fragment_no_code_button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_container, NoCodeFragment.newInstance())
                    .addToBackStack(NoCodeFragment::class.qualifiedName)
                    .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        trigger_fragment_input.requestFocus()
    }

    companion object {
        private const val REGEX_CODE_PATTERN = "\\d{6}"


        fun newInstance(): TriggerFragment {
            return TriggerFragment()
        }
    }
}
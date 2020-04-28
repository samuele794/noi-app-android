/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_info.*
import org.dpppt.android.app.R

class InfoDialog : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments
        args?.let {
            if (it.containsKey(ARG_TEXT_ID)) {
                dialog_info_text.setText(it.getInt(ARG_TEXT_ID))
            } else {
                dialog_info_text.text = it.getString(ARG_TEXT_STRING)
            }
        }

        dialog_info_button.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        private const val ARG_TEXT_ID = "arg_text_id"
        private const val ARG_TEXT_STRING = "arg_text_string"

        fun newInstance(@StringRes text: Int): InfoDialog {
            return InfoDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TEXT_ID, text)
                }
            }
        }


        fun newInstance(text: String): InfoDialog {
            return InfoDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TEXT_STRING, text)
                }
            }
        }
    }
}
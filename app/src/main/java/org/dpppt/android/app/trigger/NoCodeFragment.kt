/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.trigger

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_no_code.*
import org.dpppt.android.app.R
import org.dpppt.android.app.util.PhoneUtil.callHelpline

class NoCodeFragment : Fragment(R.layout.fragment_no_code) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        no_code_fragment_cancel.setOnClickListener { activity?.onBackPressed() }
        no_code_fragment_support_tel.setOnClickListener { v: View -> callHelpline(v.context) }
    }

    companion object {

        fun newInstance(): NoCodeFragment {
            return NoCodeFragment()
        }
    }
}
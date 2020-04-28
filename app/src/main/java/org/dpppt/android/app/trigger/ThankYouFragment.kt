/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.trigger

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.fragment_thank_you.*
import org.dpppt.android.app.R

class ThankYouFragment : Fragment(R.layout.fragment_thank_you) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thank_you_fragment_button_done.setOnClickListener {
            parentFragmentManager.popBackStack(TriggerFragment::class.java.canonicalName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ThankYouFragment {
            return ThankYouFragment()
        }
    }
}
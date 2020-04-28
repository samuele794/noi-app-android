/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.trigger.views

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.dpppt.android.app.R
import java.util.*
import kotlin.math.max

class ChainedEditText : ConstraintLayout {
    private lateinit var shadowEditText: EditText
    private lateinit var textViewGroup: View
    private val textViews = arrayOfNulls<TextView>(6)
    private val chainedEditTextListeners: MutableSet<ChainedEditTextListener> = HashSet()

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        textViewGroup = LayoutInflater.from(context).inflate(R.layout.view_chained_edit_text, this, true)
        for (i in 0 until NUM_CHARACTERS) {
            textViews[i] = textViewGroup
                    .findViewById(resources.getIdentifier(ID_TEXT_FIELD + (i + 1), "id", context.packageName))
        }
        shadowEditText = EditText(context).apply {
            height = 1
            width = 1
            setBackgroundColor(Color.TRANSPARENT)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            imeOptions = EditorInfo.IME_ACTION_SEND
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.length > NUM_CHARACTERS) {
                        s.delete(NUM_CHARACTERS, s.length)
                    }
                    updateTextViews()
                    val input = s.toString()
                    for (listener in chainedEditTextListeners) {
                        listener.onTextChanged(input)
                    }
                }
            })
            onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                updateTextViews()
                setKeyboardVisible(hasFocus)
            }

            setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_SEND && chainedEditTextListeners.size > 0) {
                    for (listener in chainedEditTextListeners) {
                        listener.onEditorSendAction()
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        addView(shadowEditText)
        textViewGroup.setOnClickListener { _: View? ->
            focusEditText()
            setKeyboardVisible(true)
        }
        onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                focusEditText()
            }
        }
    }

    private fun focusEditText() {
        shadowEditText.requestFocus()
        shadowEditText.setSelection(shadowEditText.text.length)
    }

    private fun updateTextViews() {
        val input = shadowEditText.text.toString()
        val hasFocus = shadowEditText.hasFocus()
        for (i in 0 until NUM_CHARACTERS) {
            textViews[i]?.let { textView ->
                if (i < input.length) {
                    textView.text = input[i].toString()
                } else {
                    textView.text = ""
                }
                textView.isSelected = hasFocus && i == max(0, input.length - 1)
            }

        }
    }

    private fun setKeyboardVisible(visible: Boolean) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (visible) {
            inputMethodManager.showSoftInput(shadowEditText, InputMethodManager.SHOW_IMPLICIT)
        } else {
            inputMethodManager.hideSoftInputFromWindow(shadowEditText.windowToken, 0)
        }
    }

    val text: String
        get() = shadowEditText.text.toString()

    fun addTextChangedListener(chainedEditTextListener: ChainedEditTextListener) {
        chainedEditTextListeners.add(chainedEditTextListener)
    }

    fun removeTextChangedListener(chainedEditTextListener: ChainedEditTextListener) {
        chainedEditTextListeners.remove(chainedEditTextListener)
    }

    public interface ChainedEditTextListener {
        fun onTextChanged(input: String)
        fun onEditorSendAction()
    }

    companion object {
        private const val NUM_CHARACTERS = 6
        private const val ID_TEXT_FIELD = "chained_edit_text_view_"
    }
}
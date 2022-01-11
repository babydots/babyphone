package com.serwylo.babyphone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.serwylo.babyphone.databinding.DialPadBinding

/**
 * A fragment representing a dial pad (number 1 - 9, 0, *, and #).
 */
class DialPadFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialPadBinding.inflate(inflater, container, false)

        val tonePlayer = { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                buttonPressListener?.invoke();
            }
            true
        }
        binding.btn0.setOnTouchListener(tonePlayer)
        binding.btn1.setOnTouchListener(tonePlayer)
        binding.btn2.setOnTouchListener(tonePlayer)
        binding.btn3.setOnTouchListener(tonePlayer)
        binding.btn4.setOnTouchListener(tonePlayer)
        binding.btn5.setOnTouchListener(tonePlayer)
        binding.btn6.setOnTouchListener(tonePlayer)
        binding.btn7.setOnTouchListener(tonePlayer)
        binding.btn8.setOnTouchListener(tonePlayer)
        binding.btn9.setOnTouchListener(tonePlayer)
        binding.btnStar.setOnTouchListener(tonePlayer)
        binding.btnHash.setOnTouchListener(tonePlayer)

        return binding.root
    }

    private var buttonPressListener: (() -> Unit)? = null

    fun onButtonPressed(listener: (() -> Unit)?) {
        buttonPressListener = listener
    }

}
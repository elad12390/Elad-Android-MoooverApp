package com.elad.hit.mooover

import android.content.Context
import android.text.Editable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object Extensions {
    fun AppCompatEditText.toFloat() = this.text.toString().toFloat()
    fun AppCompatEditText.toFloatOrNull() = this.text.toString().toFloatOrNull()

    fun AppCompatEditText.toInt() = this.text.toString().toInt()
    fun AppCompatEditText.toIntOrNull() = this.text.toString().toIntOrNull()

    fun Editable?.toInt() = this.toString().toInt()
    fun Editable?.toIntOrNull() = this.toString().toIntOrNull()
    fun Editable?.toFloat() = this.toString().toFloat()
    fun Editable?.toFloatOrNull() = this.toString().toFloatOrNull()

    fun Fragment.getInt(resourceId: Int) = this.getString(resourceId).toInt()
    fun Fragment.getFloat(resourceId: Int) = this.getString(resourceId).toFloat()
    fun Fragment.openErrorSnackbar(errorText: Int) {
        val snackbar = Snackbar.make(requireView(), errorText, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.error_snackbar_txt))
        snackbar.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_snackbar))
        snackbar.show()
    }
    fun Fragment.openErrorSnackbar(errorText: String) {
        val snackbar = Snackbar.make(requireView(), errorText, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.error_snackbar_txt))
        snackbar.view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_snackbar))
        snackbar.show()
    }
    fun Fragment.openSnackbar(text: Int) {
        Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
    }
    fun Fragment.openSnackbar(text: String) {
        Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
    }
    fun Fragment.openTopSnackbar(text: String) {
        val snackbar = Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT)
        val layoutParams = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.gravity = Gravity.CENTER
        snackbar.view.layoutParams = layoutParams
        snackbar.show()
    }
}
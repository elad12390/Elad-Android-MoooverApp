package com.elad.hit.mooover

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(val type: ItemType, val width: Float, val height: Float) : Parcelable {
    val id = counter++
    val price get() = (type.pricePerCubicMeter * (width / 100) * (height / 100))
    companion object CartItem {
        var counter = 0
    }
}

package com.elad.hit.mooover

enum class ItemType(val itemIdx: Int, val drawable: Int, val pricePerCubicMeter: Float) {
    Chair(0, R.drawable.ic_chair_black_24dp, 10f),
    Sofa(1, R.drawable.ic_couch_sofa,30f),
    Bed(2, R.drawable.bed_icon,40f);
    companion object {
        fun fromIdx(idx: Int): ItemType {
            when (idx) {
                0 -> return Chair
                1 -> return Sofa
                2 -> return Bed

                else -> return Chair
            }
        }
    }
}
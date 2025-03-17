package com.wc.fatclip

import android.content.res.Resources
import android.util.TypedValue

fun Float.dp2px() = dp22px().toInt()
fun Float.dp22px() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    Resources.getSystem().displayMetrics
)

fun Float.sp2px() = sp22px().toInt()
fun Float.sp22px() = this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f

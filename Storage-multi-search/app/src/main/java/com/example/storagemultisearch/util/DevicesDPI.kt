package com.example.storagemultisearch.util

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.getDeviceDpi(): String {
    return when(resources.displayMetrics.densityDpi) {
        in 0 .. 160 -> "mdpi"
        in 161 .. 240 -> "hdpi"
        in 241 .. 320 -> "xhdpi"
        in 321 .. 480 -> "xxhdpi"
        else -> "xxxhdpi"
    }
}
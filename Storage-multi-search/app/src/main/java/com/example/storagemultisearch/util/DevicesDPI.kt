package com.example.storagemultisearch.util

import androidx.appcompat.app.AppCompatActivity

/**
 * getDeviceDpi():
 * 디바이스의 Dpi를 계산할 수 있습니다.
 * 디바이스의 해상도에 따라 분리된 Storage폴더에 접근할 수 있습니다.
 */

fun AppCompatActivity.getDeviceDpi(): String {
    return when(resources.displayMetrics.densityDpi) {
        in 0 .. 160 -> "mdpi"
        in 161 .. 240 -> "hdpi"
        in 241 .. 320 -> "xhdpi"
        in 321 .. 480 -> "xxhdpi"
        else -> "xxxhdpi"
    }
}
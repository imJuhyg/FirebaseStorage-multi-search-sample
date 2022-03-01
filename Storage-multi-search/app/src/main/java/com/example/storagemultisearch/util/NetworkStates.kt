package com.example.storagemultisearch.util

import android.net.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/* 순간적인 네트워크 상태 가져오기 */
fun AppCompatActivity.getNetworkState(connectivityManager: ConnectivityManager): Boolean {
    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected ?: false

    } else {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        if(activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true
        else if(activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true
    }
    return false
}

/* 네트워크 이벤트 수신 대기 */
fun AppCompatActivity.registerNetworkCallback(
    connectivityManager: ConnectivityManager,
    networkCallback: ConnectivityManager.NetworkCallback) {

    val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()
    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
}

fun AppCompatActivity.unregisterNetworkCallback(
    connectivityManager: ConnectivityManager,
    networkCallback: ConnectivityManager.NetworkCallback) {

    connectivityManager.unregisterNetworkCallback(networkCallback)
}
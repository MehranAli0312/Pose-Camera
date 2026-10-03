package com.aipose.camera.posematch.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import com.aipose.camera.posematch.domain.models.NetworkIssue
import com.aipose.camera.posematch.domain.models.NetworkStatus
import com.aipose.camera.posematch.domain.models.NetworkTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class NetworkConnectivityChecker(
    private val context: Context,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    suspend fun hasActiveInternet(): Boolean = withContext(Dispatchers.IO) {
        isCurrentlyConnected()
    }

    fun currentStatus(): NetworkStatus {
        val manager = connectivityManager
        val network = manager.activeNetwork
        val capabilities = network?.let(manager::getNetworkCapabilities)

        return capabilities.toStatus()
    }

    fun observeActiveInternet(): Flow<Boolean> = callbackFlow {
        fun emitCurrent() {
            trySend(isCurrentlyConnected())
        }

        emitCurrent()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                emitCurrent()
            }

            override fun onLost(network: Network) {
                emitCurrent()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                emitCurrent()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    private fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun NetworkCapabilities?.toStatus(): NetworkStatus {
        if (this == null) {
            return NetworkStatus.Unavailable(
                if (isAirplaneModeOn()) NetworkIssue.AIRPLANE_MODE else NetworkIssue.NO_NETWORK,
            )
        }

        if (!hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return NetworkStatus.Unavailable(NetworkIssue.NO_NETWORK)
        }

        if (hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) {
            return NetworkStatus.Unavailable(NetworkIssue.CAPTIVE_PORTAL)
        }

        if (!hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return NetworkStatus.Unavailable(NetworkIssue.NO_INTERNET)
        }

        return NetworkStatus.Connected(
            transport = transport(),
            isMetered = !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
        )
    }

    private fun NetworkCapabilities.transport(): NetworkTransport = when {
        hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkTransport.VPN
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkTransport.WIFI
        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkTransport.CELLULAR
        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkTransport.ETHERNET
        else -> NetworkTransport.OTHER
    }

    private fun isAirplaneModeOn(): Boolean = runCatching {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON) != 0
    }.getOrDefault(false)
}

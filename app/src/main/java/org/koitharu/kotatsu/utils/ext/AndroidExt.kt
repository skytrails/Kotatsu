package org.koitharu.kotatsu.utils.ext

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun ConnectivityManager.waitForNetwork(): Network {
	val request = NetworkRequest.Builder().build()
	return suspendCancellableCoroutine<Network> { cont ->
		val callback = object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				cont.resume(network)
			}
		}
		registerNetworkCallback(request, callback)
		cont.invokeOnCancellation {
			unregisterNetworkCallback(callback)
		}
	}
}
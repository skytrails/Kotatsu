package org.koitharu.kotatsu.utils.ext

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.koitharu.kotatsu.core.network.CommonHeaders
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Call.await() = suspendCancellableCoroutine<Response> { cont ->
	this.enqueue(object : Callback {
		override fun onFailure(call: Call, e: IOException) {
			if (cont.isActive) {
				cont.resumeWithException(e)
			}
		}

		override fun onResponse(call: Call, response: Response) {
			if (cont.isActive) {
				cont.resume(response)
			}
		}
	})
	cont.invokeOnCancellation {
		this.cancel()
	}
}

val Response.mimeType: String?
	get() = body?.contentType()?.run { "$type/$subtype" }

val Response.contentDisposition: String?
	get() = header(CommonHeaders.CONTENT_DISPOSITION)
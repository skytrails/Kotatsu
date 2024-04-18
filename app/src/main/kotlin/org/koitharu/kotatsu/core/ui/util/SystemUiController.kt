package org.koitharu.kotatsu.core.ui.util

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi

sealed class SystemUiController(
	protected val window: Window,
) {

	abstract fun setSystemUiVisible(value: Boolean)

	@RequiresApi(Build.VERSION_CODES.S)
	private class Api30Impl(window: Window) : SystemUiController(window) {

		private val insetsController = checkNotNull(window.decorView.windowInsetsController)

		override fun setSystemUiVisible(value: Boolean) {
			if (value) {
				insetsController.show(WindowInsets.Type.systemBars())
				insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
			} else {
				insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
				insetsController.hide(WindowInsets.Type.systemBars())
			}
		}
	}

	@Suppress("DEPRECATION")
	private class LegacyImpl(window: Window) : SystemUiController(window) {

		override fun setSystemUiVisible(value: Boolean) {
			window.decorView.systemUiVisibility = if (value) {
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
						View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			} else {
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
						View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
						View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
						View.SYSTEM_UI_FLAG_FULLSCREEN or
						View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			}
		}
	}

	companion object {

		operator fun invoke(window: Window): SystemUiController =
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				Api30Impl(window)
			} else {
				LegacyImpl(window)
			}
	}
}

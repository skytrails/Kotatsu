package org.koitharu.kotatsu.scrobbling.common.ui.config

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.ui.BaseViewModel
import org.koitharu.kotatsu.list.ui.model.EmptyState
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.scrobbling.common.domain.Scrobbler
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblerService
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblerUser
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblingInfo
import org.koitharu.kotatsu.scrobbling.common.domain.model.ScrobblingStatus
import org.koitharu.kotatsu.utils.SingleLiveEvent
import org.koitharu.kotatsu.utils.asFlowLiveData
import org.koitharu.kotatsu.utils.ext.onFirst

class ScrobblerConfigViewModel @AssistedInject constructor(
	@Assisted scrobblerService: ScrobblerService,
	scrobblers: Set<@JvmSuppressWildcards Scrobbler>,
) : BaseViewModel() {

	private val scrobbler = scrobblers.first { it.scrobblerService == scrobblerService }

	val titleResId = scrobbler.scrobblerService.titleResId

	val user = MutableLiveData<ScrobblerUser?>(null)
	val onLoggedOut = SingleLiveEvent<Unit>()

	val content = scrobbler.observeAllScrobblingInfo()
		.onStart { loadingCounter.increment() }
		.onFirst { loadingCounter.decrement() }
		.catch { errorEvent.postCall(it) }
		.map { buildContentList(it) }
		.asFlowLiveData(viewModelScope.coroutineContext + Dispatchers.Default, emptyList())

	init {
		scrobbler.user
			.onEach { user.postValue(it) }
			.launchIn(viewModelScope + Dispatchers.Default)
	}

	fun onAuthCodeReceived(authCode: String) {
		launchLoadingJob(Dispatchers.Default) {
			val newUser = scrobbler.authorize(authCode)
			user.postValue(newUser)
		}
	}

	fun logout() {
		launchLoadingJob(Dispatchers.Default) {
			scrobbler.logout()
			user.postValue(null)
			onLoggedOut.postCall(Unit)
		}
	}

	private fun buildContentList(list: List<ScrobblingInfo>): List<ListModel> {
		if (list.isEmpty()) {
			return listOf(
				EmptyState(
					icon = R.drawable.ic_empty_history,
					textPrimary = R.string.nothing_here,
					textSecondary = R.string.scrobbling_empty_hint,
					actionStringRes = 0,
				),
			)
		}
		val grouped = list.groupBy { it.status }
		val statuses = enumValues<ScrobblingStatus>()
		val result = ArrayList<ListModel>(list.size + statuses.size)
		for (st in statuses) {
			val subList = grouped[st]
			if (subList.isNullOrEmpty()) {
				continue
			}
			result.add(st)
			result.addAll(subList)
		}
		return result
	}

	@AssistedFactory
	interface Factory {

		fun create(service: ScrobblerService): ScrobblerConfigViewModel
	}
}

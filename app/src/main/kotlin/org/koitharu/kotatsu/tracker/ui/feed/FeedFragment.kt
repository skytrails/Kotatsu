package org.koitharu.kotatsu.tracker.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.ImageLoader
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.exceptions.resolve.SnackbarErrorObserver
import org.koitharu.kotatsu.core.ui.BaseFragment
import org.koitharu.kotatsu.core.ui.list.PaginationScrollListener
import org.koitharu.kotatsu.core.util.ext.addMenuProvider
import org.koitharu.kotatsu.core.util.ext.observe
import org.koitharu.kotatsu.core.util.ext.observeEvent
import org.koitharu.kotatsu.databinding.FragmentFeedBinding
import org.koitharu.kotatsu.details.ui.DetailsActivity
import org.koitharu.kotatsu.list.ui.adapter.MangaListListener
import org.koitharu.kotatsu.list.ui.adapter.TypedListSpacingDecoration
import org.koitharu.kotatsu.list.ui.model.ListHeader
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.size.StaticItemSizeResolver
import org.koitharu.kotatsu.main.ui.owners.BottomNavOwner
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaTag
import org.koitharu.kotatsu.tracker.ui.feed.adapter.FeedAdapter
import org.koitharu.kotatsu.tracker.ui.updates.UpdatesActivity
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment :
	BaseFragment<FragmentFeedBinding>(),
	PaginationScrollListener.Callback,
	MangaListListener, SwipeRefreshLayout.OnRefreshListener {

	@Inject
	lateinit var coil: ImageLoader

	private val viewModel by viewModels<FeedViewModel>()

	private var feedAdapter: FeedAdapter? = null

	override fun onCreateViewBinding(
		inflater: LayoutInflater,
		container: ViewGroup?,
	) = FragmentFeedBinding.inflate(inflater, container, false)

	override fun onViewBindingCreated(binding: FragmentFeedBinding, savedInstanceState: Bundle?) {
		super.onViewBindingCreated(binding, savedInstanceState)
		val sizeResolver = StaticItemSizeResolver(resources.getDimensionPixelSize(R.dimen.smaller_grid_width))
		feedAdapter = FeedAdapter(coil, viewLifecycleOwner, this, sizeResolver)
		with(binding.recyclerView) {
			adapter = feedAdapter
			setHasFixedSize(true)
			addOnScrollListener(PaginationScrollListener(4, this@FeedFragment))
			addItemDecoration(TypedListSpacingDecoration(context, true))
		}
		binding.swipeRefreshLayout.setOnRefreshListener(this)
		addMenuProvider(
			FeedMenuProvider(
				binding.recyclerView,
				viewModel,
			),
		)

		viewModel.content.observe(viewLifecycleOwner, this::onListChanged)
		viewModel.onError.observeEvent(viewLifecycleOwner, SnackbarErrorObserver(binding.recyclerView, this))
		viewModel.onFeedCleared.observeEvent(viewLifecycleOwner) {
			onFeedCleared()
		}
		viewModel.isRunning.observe(viewLifecycleOwner, this::onIsTrackerRunningChanged)
	}

	override fun onDestroyView() {
		feedAdapter = null
		super.onDestroyView()
	}

	override fun onWindowInsetsChanged(insets: Insets) {
		val rv = requireViewBinding().recyclerView
		rv.updatePadding(
			bottom = insets.bottom + rv.paddingTop,
		)
	}

	override fun onRefresh() {
		viewModel.update()
	}

	override fun onRetryClick(error: Throwable) = Unit

	override fun onUpdateFilter(tags: Set<MangaTag>) = Unit

	override fun onFilterClick(view: View?) = Unit

	override fun onEmptyActionClick() = Unit

	override fun onListHeaderClick(item: ListHeader, view: View) {
		val context = view.context
		context.startActivity(UpdatesActivity.newIntent(context))
	}

	private fun onListChanged(list: List<ListModel>) {
		feedAdapter?.items = list
	}

	private fun onFeedCleared() {
		val snackbar = Snackbar.make(
			requireViewBinding().recyclerView,
			R.string.updates_feed_cleared,
			Snackbar.LENGTH_LONG,
		)
		snackbar.anchorView = (activity as? BottomNavOwner)?.bottomNav
		snackbar.show()
	}

	private fun onIsTrackerRunningChanged(isRunning: Boolean) {
		requireViewBinding().swipeRefreshLayout.isRefreshing = isRunning
	}

	override fun onScrolledToEnd() {
		viewModel.requestMoreItems()
	}

	override fun onItemClick(item: Manga, view: View) {
		startActivity(DetailsActivity.newIntent(context ?: return, item))
	}

	override fun onReadClick(manga: Manga, view: View) = Unit

	override fun onTagClick(manga: Manga, tag: MangaTag, view: View) = Unit
}

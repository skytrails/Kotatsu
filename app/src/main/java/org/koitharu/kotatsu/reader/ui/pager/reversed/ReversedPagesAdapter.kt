package org.koitharu.kotatsu.reader.ui.pager.reversed

import android.view.LayoutInflater
import android.view.ViewGroup
import org.koitharu.kotatsu.core.exceptions.resolve.ExceptionResolver
import org.koitharu.kotatsu.databinding.ItemPageBinding
import org.koitharu.kotatsu.reader.domain.PageLoader
import org.koitharu.kotatsu.reader.ui.config.ReaderSettings
import org.koitharu.kotatsu.reader.ui.pager.BaseReaderAdapter

class ReversedPagesAdapter(
	loader: PageLoader,
	settings: ReaderSettings,
	exceptionResolver: ExceptionResolver,
) : BaseReaderAdapter<ReversedPageHolder>(loader, settings, exceptionResolver) {

	override fun onCreateViewHolder(
		parent: ViewGroup,
		loader: PageLoader,
		settings: ReaderSettings,
		exceptionResolver: ExceptionResolver,
	) = ReversedPageHolder(
		binding = ItemPageBinding.inflate(LayoutInflater.from(parent.context), parent, false),
		loader = loader,
		settings = settings,
		exceptionResolver = exceptionResolver,
	)
}

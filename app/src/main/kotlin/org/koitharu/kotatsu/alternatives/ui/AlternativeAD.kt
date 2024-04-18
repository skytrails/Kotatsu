package org.koitharu.kotatsu.alternatives.ui

import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.parser.favicon.faviconUri
import org.koitharu.kotatsu.core.ui.image.ChipIconTarget
import org.koitharu.kotatsu.core.ui.image.CoverSizeResolver
import org.koitharu.kotatsu.core.ui.image.TrimTransformation
import org.koitharu.kotatsu.core.ui.list.AdapterDelegateClickListenerAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.util.ext.enqueueWith
import org.koitharu.kotatsu.core.util.ext.newImageRequest
import org.koitharu.kotatsu.core.util.ext.source
import org.koitharu.kotatsu.databinding.ItemMangaAlternativeBinding
import org.koitharu.kotatsu.list.ui.ListModelDiffCallback
import org.koitharu.kotatsu.list.ui.model.ListModel
import kotlin.math.sign
import com.google.android.material.R as materialR

fun alternativeAD(
	coil: ImageLoader,
	lifecycleOwner: LifecycleOwner,
	listener: OnListItemClickListener<MangaAlternativeModel>,
) = adapterDelegateViewBinding<MangaAlternativeModel, ListModel, ItemMangaAlternativeBinding>(
	{ inflater, parent -> ItemMangaAlternativeBinding.inflate(inflater, parent, false) },
) {

	val colorGreen = ContextCompat.getColor(context, R.color.common_green)
	val colorRed = ContextCompat.getColor(context, R.color.common_red)
	val clickListener = AdapterDelegateClickListenerAdapter(this, listener)
	itemView.setOnClickListener(clickListener)
	binding.buttonMigrate.setOnClickListener(clickListener)
	binding.chipSource.setOnClickListener(clickListener)

	bind { payloads ->
		binding.textViewTitle.text = item.manga.title
		binding.textViewSubtitle.text = buildSpannedString {
			if (item.chaptersCount > 0) {
				append(context.resources.getQuantityString(R.plurals.chapters, item.chaptersCount, item.chaptersCount))
			} else {
				append(context.getString(R.string.no_chapters))
			}
			when (item.chaptersDiff.sign) {
				-1 -> inSpans(ForegroundColorSpan(colorRed)) {
					append("  ▼ ")
					append(item.chaptersDiff.toString())
				}

				1 -> inSpans(ForegroundColorSpan(colorGreen)) {
					append("  ▲ +")
					append(item.chaptersDiff.toString())
				}
			}
		}
		binding.progressView.setPercent(item.progress, ListModelDiffCallback.PAYLOAD_PROGRESS_CHANGED in payloads)
		binding.chipSource.also { chip ->
			chip.text = item.manga.source.title
			ImageRequest.Builder(context)
				.data(item.manga.source.faviconUri())
				.lifecycle(lifecycleOwner)
				.crossfade(false)
				.size(context.resources.getDimensionPixelSize(materialR.dimen.m3_chip_icon_size))
				.target(ChipIconTarget(chip))
				.placeholder(R.drawable.ic_web)
				.fallback(R.drawable.ic_web)
				.error(R.drawable.ic_web)
				.source(item.manga.source)
				.transformations(CircleCropTransformation())
				.allowRgb565(true)
				.enqueueWith(coil)
		}
		binding.imageViewCover.newImageRequest(lifecycleOwner, item.manga.coverUrl)?.run {
			size(CoverSizeResolver(binding.imageViewCover))
			placeholder(R.drawable.ic_placeholder)
			fallback(R.drawable.ic_placeholder)
			error(R.drawable.ic_error_placeholder)
			transformations(TrimTransformation())
			allowRgb565(true)
			tag(item.manga)
			source(item.manga.source)
			enqueueWith(coil)
		}
	}
}

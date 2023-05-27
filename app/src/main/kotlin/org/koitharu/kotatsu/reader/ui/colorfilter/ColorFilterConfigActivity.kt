package org.koitharu.kotatsu.reader.ui.colorfilter

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.graphics.Insets
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.ViewSizeResolver
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.parcelable.ParcelableManga
import org.koitharu.kotatsu.core.model.parcelable.ParcelableMangaPages
import org.koitharu.kotatsu.core.ui.BaseActivity
import org.koitharu.kotatsu.core.util.ext.decodeRegion
import org.koitharu.kotatsu.core.util.ext.enqueueWith
import org.koitharu.kotatsu.core.util.ext.indicator
import org.koitharu.kotatsu.core.util.ext.observe
import org.koitharu.kotatsu.core.util.ext.observeEvent
import org.koitharu.kotatsu.core.util.ext.setValueRounded
import org.koitharu.kotatsu.databinding.ActivityColorFilterBinding
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.util.format
import org.koitharu.kotatsu.reader.domain.ReaderColorFilter
import javax.inject.Inject
import com.google.android.material.R as materialR

@AndroidEntryPoint
class ColorFilterConfigActivity :
	BaseActivity<ActivityColorFilterBinding>(),
	Slider.OnChangeListener,
	View.OnClickListener {

	@Inject
	lateinit var coil: ImageLoader

	private val viewModel: ColorFilterConfigViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(ActivityColorFilterBinding.inflate(layoutInflater))
		supportActionBar?.run {
			setDisplayHomeAsUpEnabled(true)
			setHomeAsUpIndicator(materialR.drawable.abc_ic_clear_material)
		}
		viewBinding.sliderBrightness.addOnChangeListener(this)
		viewBinding.sliderContrast.addOnChangeListener(this)
		val formatter = PercentLabelFormatter(resources)
		viewBinding.sliderContrast.setLabelFormatter(formatter)
		viewBinding.sliderBrightness.setLabelFormatter(formatter)
		viewBinding.buttonDone.setOnClickListener(this)
		viewBinding.buttonReset.setOnClickListener(this)

		onBackPressedDispatcher.addCallback(ColorFilterConfigBackPressedDispatcher(this, viewModel))

		viewModel.colorFilter.observe(this, this::onColorFilterChanged)
		viewModel.isLoading.observe(this, this::onLoadingChanged)
		viewModel.preview.observe(this, this::onPreviewChanged)
		viewModel.onDismiss.observeEvent(this) {
			finishAfterTransition()
		}
	}

	override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
		if (fromUser) {
			when (slider.id) {
				R.id.slider_brightness -> viewModel.setBrightness(value)
				R.id.slider_contrast -> viewModel.setContrast(value)
			}
		}
	}

	override fun onClick(v: View) {
		when (v.id) {
			R.id.button_done -> viewModel.save()
			R.id.button_reset -> viewModel.reset()
		}
	}

	override fun onWindowInsetsChanged(insets: Insets) {
		viewBinding.root.updatePadding(
			left = insets.left,
			right = insets.right,
		)
		viewBinding.scrollView.updatePadding(
			bottom = insets.bottom,
		)
		viewBinding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
			topMargin = insets.top
		}
	}

	private fun onColorFilterChanged(readerColorFilter: ReaderColorFilter?) {
		viewBinding.sliderBrightness.setValueRounded(readerColorFilter?.brightness ?: 0f)
		viewBinding.sliderContrast.setValueRounded(readerColorFilter?.contrast ?: 0f)
		viewBinding.imageViewAfter.colorFilter = readerColorFilter?.toColorFilter()
	}

	private fun onPreviewChanged(preview: MangaPage?) {
		if (preview == null) return
		ImageRequest.Builder(this@ColorFilterConfigActivity)
			.data(preview.url)
			.scale(Scale.FILL)
			.decodeRegion()
			.tag(preview.source)
			.indicator(listOf(viewBinding.progressBefore, viewBinding.progressAfter))
			.error(R.drawable.ic_error_placeholder)
			.size(ViewSizeResolver(viewBinding.imageViewBefore))
			.allowRgb565(false)
			.target(ShadowViewTarget(viewBinding.imageViewBefore, viewBinding.imageViewAfter))
			.enqueueWith(coil)
	}

	private fun onLoadingChanged(isLoading: Boolean) {
		viewBinding.sliderContrast.isEnabled = !isLoading
		viewBinding.sliderBrightness.isEnabled = !isLoading
		viewBinding.buttonDone.isEnabled = !isLoading
	}

	private class PercentLabelFormatter(resources: Resources) : LabelFormatter {

		private val pattern = resources.getString(R.string.percent_string_pattern)

		override fun getFormattedValue(value: Float): String {
			val percent = ((value + 1f) * 100).format(0)
			return pattern.format(percent)
		}
	}

	companion object {

		const val EXTRA_PAGES = "pages"
		const val EXTRA_MANGA = "manga_id"

		fun newIntent(context: Context, manga: Manga, page: MangaPage) =
			Intent(context, ColorFilterConfigActivity::class.java)
				.putExtra(EXTRA_MANGA, ParcelableManga(manga, false))
				.putExtra(EXTRA_PAGES, ParcelableMangaPages(listOf(page)))
	}
}

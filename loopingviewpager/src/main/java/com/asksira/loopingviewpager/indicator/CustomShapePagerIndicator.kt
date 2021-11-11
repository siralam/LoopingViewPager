package com.asksira.loopingviewpager.indicator

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.R
import com.asksira.loopingviewpager.databinding.WidgetCustomShapePagerIndicatorBinding

class CustomShapePagerIndicator : FrameLayout {

    private var indicatorSize: Int = 0
    private var normalIcon: Drawable? = null
    private var selectedIcon: Drawable? = null
    private lateinit var indicatorAdapter: IndicatorAdapter
    private lateinit var binding: WidgetCustomShapePagerIndicatorBinding
    private var _indicatorSpacing: Int = 0
    var indicatorSpacing: Int
        get() = _indicatorSpacing
        set(value) {
            _indicatorSpacing = value
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomShapePagerIndicator, 0, 0
        )
        try {
            indicatorSpacing =
                a.getDimensionPixelSize(R.styleable.CustomShapePagerIndicator_indicator_spacing, 0)
            indicatorSize =
                a.getDimensionPixelSize(R.styleable.CustomShapePagerIndicator_indicator_size, 0)

            selectedIcon =
                a.getDrawable(R.styleable.CustomShapePagerIndicator_drawable_selected)
            normalIcon =
                a.getDrawable(R.styleable.CustomShapePagerIndicator_drawable_normal)
        } finally {
            a.recycle()
        }

        binding =
            WidgetCustomShapePagerIndicatorBinding.inflate(LayoutInflater.from(context), this, true)

        attachRecyclerView()

    }

    private fun attachRecyclerView() {
        binding.rvSelector.addItemDecoration(
            SpacesItemDecoration(indicatorSpacing)
        )
        indicatorAdapter = IndicatorAdapter(0, 0)
        indicatorAdapter.indicatorSize = indicatorSize
        indicatorAdapter.iconSelected = selectedIcon
        indicatorAdapter.iconNormal = normalIcon
        binding.rvSelector.adapter = indicatorAdapter
        binding.rvSelector.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return true
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    fun updateIndicatorCounts(count: Int) {
        indicatorAdapter.indicatorsCount = count
        indicatorAdapter.selectedPosition = 0
        indicatorAdapter.notifyDataSetChanged()
        binding.rvSelector.scrollToPosition(0)
    }

    fun onPageScrolled(position: Int, positionOffset: Float) {
        indicatorAdapter.selectedPosition = position
        indicatorAdapter.notifyDataSetChanged()
        binding.rvSelector.smoothScrollToPosition(position)
    }

    fun onPageSelected(position: Int) {
        indicatorAdapter.selectedPosition = position
        indicatorAdapter.notifyDataSetChanged()
        binding.rvSelector.smoothScrollToPosition(position)
    }
}


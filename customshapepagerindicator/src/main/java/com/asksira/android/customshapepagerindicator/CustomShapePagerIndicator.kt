package com.asksira.android.customshapepagerindicator

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout

class CustomShapePagerIndicator: FrameLayout {

    private lateinit var llUnselectedIndicators: LinearLayout
    private lateinit var flSelectedIndicatorContainer: FrameLayout

    private var _indicatorSpacing: Int = 0
    var indicatorSpacing: Int
        get() = _indicatorSpacing
        set(value) {
            _indicatorSpacing = value
        }

    var highlighterViewDelegate: ((container: FrameLayout) -> View)? = null
    var unselectedViewDelegate: ((container: LinearLayout) -> View)? = null
    private var currentSelectedPosition: Int = 0
    private var highlighterView: View? = null

    constructor(context: Context): super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomShapePagerIndicator, 0, 0)
        try {
            indicatorSpacing = a.getDimensionPixelSize(R.styleable.CustomShapePagerIndicator_indicator_spacing, 0)
        } finally {
            a.recycle()
        }
        LayoutInflater.from(context).inflate(R.layout.widget_custom_shape_pager_indicator, this, true)
        llUnselectedIndicators = findViewById(R.id.llUnselectedIndicators)
        flSelectedIndicatorContainer = findViewById(R.id.flSelectedIndicatorContainer)
    }

    fun updateIndicatorCounts(count: Int) {
        llUnselectedIndicators.removeAllViews()
        flSelectedIndicatorContainer.removeAllViews()
        repeat(count) { i ->
            val view = unselectedViewDelegate?.invoke(llUnselectedIndicators)
            view?.let { llUnselectedIndicators.addView(it) }
            if (i != 0) {
                view?.layoutParams = (view?.layoutParams as? MarginLayoutParams).apply {
                    this?.setMargins(indicatorSpacing, 0, 0, 0)
                }
            }
        }
        highlighterView = highlighterViewDelegate?.invoke(flSelectedIndicatorContainer)
        highlighterView?.let { flSelectedIndicatorContainer.addView(it) }
        highlighterView?.afterMeasured {
            x = llUnselectedIndicators.getChildAt(currentSelectedPosition).x
        }
        llUnselectedIndicators.afterMeasured {
            flSelectedIndicatorContainer.layoutParams = flSelectedIndicatorContainer.layoutParams.apply {
                width = llUnselectedIndicators.width
                height = llUnselectedIndicators.height
            }
            flSelectedIndicatorContainer.requestLayout()
        }
    }

    fun onPageScrolled(position: Int, positionOffset: Float) {
        if (positionOffset == 0f) { //Which means it reaches the end, special case
            llUnselectedIndicators.getChildAt(position)?.x?.let { highlighterView?.x = it }
        } else { //Still scrolling
            val lhs = llUnselectedIndicators.getChildAt(position)
            val rhs = llUnselectedIndicators.getChildAt(position+1)
            when {
                lhs == null -> { //Assume it is the first one
                    val toX = llUnselectedIndicators.getChildAt(1).x
                    highlighterView?.x = toX * positionOffset
                }
                rhs == null -> { //Which means lhs is already the end. This case we will flip from end to the beginning
                    highlighterView?.x = lhs.x * (1 - positionOffset)
                }
                else -> {
                    highlighterView?.x = lhs.x + (rhs.x - lhs.x) * positionOffset
                }
            }
        }
    }

    fun onPageSelected(position: Int) {
        currentSelectedPosition = position
    }

    private inline fun <T : View> T.afterMeasured(crossinline onMeasured: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    onMeasured()
                }
            }
        })
    }

}
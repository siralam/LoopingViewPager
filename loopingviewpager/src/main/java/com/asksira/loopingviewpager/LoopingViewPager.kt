package com.asksira.loopingviewpager

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * A ViewPager that auto-scrolls, and supports infinite scroll.
 * For infinite Scroll, you may use LoopingPagerAdapter.
 */
class LoopingViewPager : ViewPager {
    protected var isInfinite = true
    protected var isAutoScroll = false
    protected var wrapContent = true
    protected var aspectRatio = 0f
    protected var itemAspectRatio = 0f

    //AutoScroll
    private var interval = 5000
    private var currentPagePosition = 0
    private var isAutoScrollResumed = false
    private val autoScrollHandler = Handler()
    private val autoScrollRunnable = Runnable {
        if (adapter == null || !isAutoScroll || adapter?.count ?: 0 < 2) return@Runnable
        if (!isInfinite && adapter?.count ?: 0 - 1 == currentPagePosition) {
            currentPagePosition = 0
        } else {
            currentPagePosition++
        }
        setCurrentItem(currentPagePosition, true)
    }

    //For Indicator
    var onIndicatorProgress: ((selectingPosition: Int, progress: Float) -> Unit)? = null
    private var previousScrollState = SCROLL_STATE_IDLE
    private var scrollState = SCROLL_STATE_IDLE

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.LoopingViewPager, 0, 0)
        try {
            isInfinite = a.getBoolean(R.styleable.LoopingViewPager_isInfinite, false)
            isAutoScroll = a.getBoolean(R.styleable.LoopingViewPager_autoScroll, false)
            wrapContent = a.getBoolean(R.styleable.LoopingViewPager_wrap_content, true)
            interval = a.getInt(R.styleable.LoopingViewPager_scrollInterval, 5000)
            aspectRatio = a.getFloat(R.styleable.LoopingViewPager_viewpagerAspectRatio, 0f)
            itemAspectRatio = a.getFloat(R.styleable.LoopingViewPager_itemAspectRatio, 0f)
            isAutoScrollResumed = isAutoScroll
        } finally {
            a.recycle()
        }
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        val width = MeasureSpec.getSize(widthMeasureSpec)
        if (aspectRatio > 0) {
            val height =
                (MeasureSpec.getSize(widthMeasureSpec).toFloat() / aspectRatio).roundToInt()
            val finalWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            val finalHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)

            /*
             * If child items can scale, fit inside their parent by increasing left/right padding.
             * https://github.com/siralam/LoopingViewPager/issues/17
             */if (itemAspectRatio > 0 && itemAspectRatio != aspectRatio) {
                // super has to be called in the beginning so the child views can be initialized.
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                // Remove padding from width
                var childWidthSize = width - paddingLeft - paddingRight
                // Make child width MeasureSpec
                var childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY)
                var i = 0
                while (i < childCount) {
                    val child = getChildAt(i)
                    child.measure(
                        childWidthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    )
                    val w = child.measuredWidth
                    val h = child.measuredHeight
                    if (h > 0 && h > height) {
                        val ratio = w.toFloat() / h
                        // Round down largest width that fits.
                        val optimalWidth =
                            floor(height * ratio.toDouble())
                        // Round up new padding size.
                        val newPadding =
                            ((width - optimalWidth) / 2).roundToInt()
                        // Set new padding values
                        setPadding(newPadding, paddingTop, newPadding, paddingBottom)
                        // Remove padding from width
                        childWidthSize = width - paddingLeft - paddingRight
                        // Make child width MeasureSpec
                        childWidthMeasureSpec =
                            MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY)
                        child.measure(
                            childWidthMeasureSpec,
                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                        )
                    } else {
                        i++
                    }
                }
            }
            super.onMeasure(finalWidthMeasureSpec, finalHeightMeasureSpec)
        } else {
            //https://stackoverflow.com/a/24666987/7870874
            if (wrapContent) {
                val mode = MeasureSpec.getMode(heightMeasureSpec)
                // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
                // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
                if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
                    // super has to be called in the beginning so the child views can be initialized.
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                    var height = 0
                    // Remove padding from width
                    val childWidthSize = width - paddingLeft - paddingRight
                    // Make child width MeasureSpec
                    val childWidthMeasureSpec =
                        MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY)
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)
                        child.measure(
                            childWidthMeasureSpec,
                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                        )
                        val h = child.measuredHeight
                        if (h > height) {
                            height = h
                        }
                    }
                    // Add padding back to child height
                    height += paddingTop + paddingBottom
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                }
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isAutoScroll)
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> pauseAutoScroll()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> resumeAutoScroll()
            }

        return super.onTouchEvent(ev)
    }

    protected fun init() {
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (onIndicatorProgress == null) return
                onIndicatorProgress?.invoke(
                    getRealPosition(position),
                    positionOffset
                )
            }

            override fun onPageSelected(position: Int) {
                currentPagePosition = position
                if (isAutoScrollResumed) {
                    autoScrollHandler.removeCallbacks(autoScrollRunnable)
                    autoScrollHandler.postDelayed(autoScrollRunnable, interval.toLong())
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                previousScrollState = scrollState
                scrollState = state
                if (state == SCROLL_STATE_IDLE) {
                    //Below are code to achieve infinite scroll.
                    //We silently and immediately flip the item to the first / last.
                    if (isInfinite) {
                        if (adapter == null) return
                        val itemCount = adapter?.count ?: 0
                        if (itemCount < 2) {
                            return
                        }
                        val index = currentItem
                        if (index == 0) {
                            setCurrentItem(itemCount - 2, false) //Real last item
                        } else if (index == itemCount - 1) {
                            setCurrentItem(1, false) //Real first item
                        }
                    }
                }
            }
        })
        if (isInfinite) setCurrentItem(1, false)
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        super.setAdapter(adapter)
        if (isInfinite) setCurrentItem(1, false)
    }

    fun resumeAutoScroll() {
        isAutoScrollResumed = true
        autoScrollHandler.postDelayed(autoScrollRunnable, interval.toLong())
    }

    fun pauseAutoScroll() {
        isAutoScrollResumed = false
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }//Dummy first item is selected. Indicator should be at the first one//Dummy last item is selected. Indicator should be at the last one

    /**
     * A method that helps you integrate a ViewPager Indicator.
     * This method returns the expected count of indicators.
     */
    val indicatorCount: Int
        get() = if (adapter is LoopingPagerAdapter<*>) {
            (adapter as LoopingPagerAdapter<*>).listCount
        } else {
            adapter?.count ?: 0
        }

    /**
     * This function needs to be called if dataSet has changed,
     * in order to reset current selected item and currentPagePosition and autoPageSelectionLock.
     */
    fun reset() {
        currentPagePosition = if (isInfinite) {
            setCurrentItem(1, false)
            1
        } else {
            setCurrentItem(0, false)
            0
        }
    }

    fun setInterval(interval: Int) {
        this.interval = interval
        resetAutoScroll()
    }

    private fun resetAutoScroll() {
        pauseAutoScroll()
        resumeAutoScroll()
    }

    private fun getRealPosition(position: Int): Int {
        if (!isInfinite || adapter == null) return position
        return if (adapter!!.count == 1) {
            0
        } else if (position == 0) {
            adapter!!.count - 1 - 2 //First item is a dummy of last item
        } else if (position > adapter!!.count - 2) {
            0 //Last item is a dummy of first item
        } else {
            position - 1
        }
    }
}
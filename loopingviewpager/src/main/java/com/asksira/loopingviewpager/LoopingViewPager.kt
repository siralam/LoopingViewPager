package com.asksira.loopingviewpager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 * A ViewPager that auto-scrolls, and supports infinite scroll.
 * For infinite Scroll, you may use LoopingPagerAdapter.
 */
class LoopingViewPager : ViewPager {
    protected var isInfinite = true
    protected var isAutoScroll = false
    protected var wrapContent = true

    //AutoScroll
    private var interval = 5000
    private var currentPagePosition = 0
    private var isAutoScrollResumed = false
    private val autoScrollHandler = Handler(Looper.getMainLooper())
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
            isAutoScrollResumed = isAutoScroll
        } finally {
            a.recycle()
        }
        init()
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
        return if (position == 0) {
            adapter!!.count - 1 - 2 //First item is a dummy of last item
        } else if (position > adapter!!.count - 2) {
            0 //Last item is a dummy of first item
        } else {
            position - 1
        }
    }
}
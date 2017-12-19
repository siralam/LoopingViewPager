package com.asksira.loopingviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * A ViewPager that auto-scrolls, and supports infinite scroll.
 * For infinite Scroll, you may use LoopingPagerAdapter.
 */

public class AutoScrollViewPager extends ViewPager {

    protected boolean isInfinite = true;
    protected boolean isAutoScroll = false;
    protected boolean wrapContent = true;

    //AutoScroll
    private int interval = 5000;
    private int currentPagePosition = 0;
    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (getAdapter() == null || !isAutoScroll) return;
            if (!isInfinite && getAdapter().getCount() - 1 == currentPagePosition) {
                currentPagePosition = 0;
            } else {
                currentPagePosition++;
            }
            setCurrentItem(currentPagePosition, true);
        }
    };

    private IndicatorPageChangeListener indicatorPageChangeListener;


    public AutoScrollViewPager(Context context) {
        super(context);
        init();
    }

    public AutoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AutoScrollViewPager, 0, 0);
        try {
            isInfinite = a.getBoolean(R.styleable.AutoScrollViewPager_isInfinite, false);
            isAutoScroll = a.getBoolean(R.styleable.AutoScrollViewPager_autoScroll, false);
            wrapContent = a.getBoolean(R.styleable.AutoScrollViewPager_wrap_content, true);
        } finally {
            a.recycle();
        }
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!wrapContent) return;

        // find the first child view
        View view = getChildAt(0);
        if (view != null) {
            // measure the first child view with the specified measure spec
            view.measure(widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(getMeasuredWidth(), measureHeight(heightMeasureSpec, view));
    }

    private int measureHeight(int measureSpec, View view) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            // set the height from the base view if available
            if (view != null) {
                result = view.getMeasuredHeight();
            }
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    protected void init () {
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int realPosition = getIndicatorPosition(true);
                if (indicatorPageChangeListener != null) indicatorPageChangeListener.onIndicatorProgress(realPosition, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                currentPagePosition = position;
                if (indicatorPageChangeListener != null) indicatorPageChangeListener.onIndicatorPageChange(getIndicatorPosition(false));
                autoScrollHandler.removeCallbacks(autoScrollRunnable);
                autoScrollHandler.postDelayed(autoScrollRunnable, interval);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (!isInfinite) return;
                //Below are code to achieve infinite scroll.
                //We silently and immediately flip the item to the first / last.
                if (state == SCROLL_STATE_IDLE) {
                    if (getAdapter() == null) return;
                    int itemCount = getAdapter().getCount();
                    if (itemCount < 2) {
                        return;
                    }
                    int index = getCurrentItem();
                    if (index == 0) {
                        setCurrentItem(itemCount-2, false); //Real last item
                    } else if (index == itemCount - 1) {
                        setCurrentItem(1, false); //Real first item
                    }
                }
            }
        });
        if (isInfinite) setCurrentItem(1, false);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        if (isInfinite) setCurrentItem(1, false);
    }

    public void resumeAutoScroll () {
        autoScrollHandler.postDelayed(autoScrollRunnable, interval);
    }

    public void pauseAutoScroll () {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }


    /**
     * A method that helps you integrate a ViewPager Indicator.
     * This method returns the expected position (Starting from 0) of indicators.
     */
    public int getIndicatorPosition (boolean notYetSelected) {
        if (notYetSelected) { //Selection is in progress. currentPagePosition is not yet updated.
            if (!isInfinite) {
                return currentPagePosition+1;
            } else {
                if (!(getAdapter() instanceof LoopingPagerAdapter)) return currentPagePosition;
                if (currentPagePosition == 1) {
                    return ((LoopingPagerAdapter)getAdapter()).getLastItemPosition() -1;
                } else if (currentPagePosition == ((LoopingPagerAdapter)getAdapter()).getLastItemPosition()) {
                    return 0;
                } else {
                    return currentPagePosition;
                }
            }
        } else { //onPageSelectedIsTriggered. Now currentPagePosition has been updated to the new position.
            if (!isInfinite) {
                return currentPagePosition;
            } else {
                if (!(getAdapter() instanceof LoopingPagerAdapter)) return currentPagePosition;
                if (currentPagePosition == 0) {
                    return ((LoopingPagerAdapter)getAdapter()).getLastItemPosition();
                } else if (currentPagePosition == ((LoopingPagerAdapter)getAdapter()).getLastItemPosition()+1) {
                    return 0;
                } else {
                    return currentPagePosition-1;
                }
            }
        }
    }

    /**
     * A method that helps you integrate a ViewPager Indicator.
     * This method returns the expected count of indicators.
     */
    public int getIndicatorCount () {
        if (getAdapter() instanceof LoopingPagerAdapter) {
            return ((LoopingPagerAdapter)getAdapter()).getListCount();
        } else {
            return getAdapter().getCount();
        }
    }

    public void setIndicatorPageChangeListener (IndicatorPageChangeListener callback) {
        this.indicatorPageChangeListener = callback;
    }

    public interface IndicatorPageChangeListener {
        void onIndicatorProgress(int selectingPosition, float progress);
        void onIndicatorPageChange(int newIndicatorPosition);
    }

}

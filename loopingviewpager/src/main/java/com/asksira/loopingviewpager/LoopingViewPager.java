package com.asksira.loopingviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * A ViewPager that auto-scrolls, and supports infinite scroll.
 * For infinite Scroll, you may use LoopingPagerAdapter.
 */

public class LoopingViewPager extends ViewPager {

    protected boolean isInfinite = true;
    protected boolean isAutoScroll = false;
    protected boolean wrapContent = true;
    protected float aspectRatio;
    protected float itemAspectRatio;

    //AutoScroll
    private int interval = 5000;
    private int previousPosition = 0;
    private int currentPagePosition = 0;
    private boolean isAutoScrollResumed = false;
    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (getAdapter() == null || !isAutoScroll || getAdapter().getCount() < 2) return;
            if (!isInfinite && getAdapter().getCount() - 1 == currentPagePosition) {
                currentPagePosition = 0;
            } else {
                currentPagePosition++;
            }
            setCurrentItem(currentPagePosition, true);
        }
    };

    //For Indicator
    private IndicatorPageChangeListener indicatorPageChangeListener;
    private int previousScrollState = SCROLL_STATE_IDLE;
    private int scrollState = SCROLL_STATE_IDLE;
    private boolean isToTheRight = true;
    /**
     * This boolean indicates whether LoopingViewPager needs to continuously tell the indicator about
     * the progress of the scroll, even after onIndicatorPageChange().
     * If indicator is smart, it should be able to finish the animation by itself after we told it that a position has been selected.
     * If indicator is not smart, then LoopingViewPager will continue to fire onIndicatorProgress() to update the indicator
     * transition position.
     */
    private boolean isIndicatorSmart = false;

    public LoopingViewPager(Context context) {
        super(context);
        init();
    }

    public LoopingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LoopingViewPager, 0, 0);
        try {
            isInfinite = a.getBoolean(R.styleable.LoopingViewPager_isInfinite, false);
            isAutoScroll = a.getBoolean(R.styleable.LoopingViewPager_autoScroll, false);
            wrapContent = a.getBoolean(R.styleable.LoopingViewPager_wrap_content, true);
            interval = a.getInt(R.styleable.LoopingViewPager_scrollInterval, 5000);
            aspectRatio = a.getFloat(R.styleable.LoopingViewPager_viewpagerAspectRatio, 0f);
            itemAspectRatio = a.getFloat(R.styleable.LoopingViewPager_itemAspectRatio, 0f);
            isAutoScrollResumed = isAutoScroll;
        } finally {
            a.recycle();
        }
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (aspectRatio > 0) {
            int height = Math.round((float) MeasureSpec.getSize(widthMeasureSpec) / aspectRatio);
            int finalWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            /*
             * If child items can scale, fit inside their parent by increasing left/right padding.
             * https://github.com/siralam/LoopingViewPager/issues/17
             */
            if (itemAspectRatio > 0 && itemAspectRatio != aspectRatio) {
                // super has to be called in the beginning so the child views can be initialized.
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                // Remove padding from width
                int childWidthSize = width - getPaddingLeft() - getPaddingRight();
                // Make child width MeasureSpec
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
                for (int i = 0; i < getChildCount(); ) {
                    View child = getChildAt(i);
                    child.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    int w = child.getMeasuredWidth();
                    int h = child.getMeasuredHeight();

                    if (h > 0 && h > height) {
                        float ratio = (float) w / h;
                        // Round down largest width that fits.
                        double optimalWidth = Math.floor(height * ratio);
                        // Round up new padding size.
                        int newPadding = (int)Math.round((width - optimalWidth) / 2);
                        // Set new padding values
                        setPadding(newPadding, getPaddingTop(), newPadding, getPaddingBottom());
                        // Remove padding from width
                        childWidthSize = width - getPaddingLeft() - getPaddingRight();
                        // Make child width MeasureSpec
                        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
                        child.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    } else {
                        i++;
                    }
                }
            }
            super.onMeasure(finalWidthMeasureSpec, finalHeightMeasureSpec);
        } else {
            //https://stackoverflow.com/a/24666987/7870874
            if (wrapContent) {
                int mode = MeasureSpec.getMode(heightMeasureSpec);
                // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
                // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
                if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
                    // super has to be called in the beginning so the child views can be initialized.
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    int height = 0;
                    // Remove padding from width
                    int childWidthSize = width - getPaddingLeft() - getPaddingRight();
                    // Make child width MeasureSpec
                    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
                    for (int i = 0; i < getChildCount(); i++) {
                        View child = getChildAt(i);
                        child.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                        int h = child.getMeasuredHeight();
                        if (h > height)  {
                            height = h;
                        }
                    }
                    // Add padding back to child height
                    height += getPaddingTop() + getPaddingBottom();
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                }
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void init() {
        addOnPageChangeListener(new OnPageChangeListener() {
            float currentPosition;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (indicatorPageChangeListener == null) return;

                if (position + positionOffset >= currentPosition) {
                    isToTheRight = true;
                } else {
                    isToTheRight = false;
                }
                if (positionOffset == 0) currentPosition = position;

                int realPosition = getSelectingIndicatorPosition(isToTheRight);

                float progress;
                if (scrollState == SCROLL_STATE_SETTLING && Math.abs(currentPagePosition - previousPosition) > 1) {
                    int pageDiff = Math.abs(currentPagePosition - previousPosition);
                    if (isToTheRight) {
                        progress = (((float) (position - previousPosition) / pageDiff)) + (positionOffset / pageDiff);
                    } else {
                        progress = ((float) (previousPosition - (position + 1)) / pageDiff) + ((1 - positionOffset) / pageDiff);
                    }
                } else {
                    progress = isToTheRight ? positionOffset : (1 - positionOffset);
                }

                if (progress == 0 || progress > 1) return;

                if (isIndicatorSmart) {
                    if (scrollState != SCROLL_STATE_DRAGGING) return;
                    indicatorPageChangeListener.onIndicatorProgress(realPosition, progress);
                } else {
                    if (scrollState == SCROLL_STATE_DRAGGING) {
                        if ((isToTheRight && Math.abs(realPosition - currentPagePosition) == 2) ||
                                !isToTheRight && realPosition == currentPagePosition) {
                            //If this happens, it means user is fast scrolling where onPageSelected() is not fast enough
                            //to catch up with the scroll, thus produce wrong position value.
                            return;
                        }
                    }
                    indicatorPageChangeListener.onIndicatorProgress(realPosition, progress);
                }
            }

            @Override
            public void onPageSelected(int position) {
                previousPosition = currentPagePosition;
                currentPagePosition = position;
                if (indicatorPageChangeListener != null) {
                    indicatorPageChangeListener.onIndicatorPageChange(getIndicatorPosition());
                }
                if(isAutoScrollResumed) {
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                    autoScrollHandler.postDelayed(autoScrollRunnable, interval);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (!isIndicatorSmart) {
                    if (scrollState == SCROLL_STATE_SETTLING && state == SCROLL_STATE_DRAGGING) {
                        if (indicatorPageChangeListener != null) {
                            indicatorPageChangeListener.onIndicatorProgress(
                                    getSelectingIndicatorPosition(isToTheRight), 1);
                        }
                    }
                }
                previousScrollState = scrollState;
                scrollState = state;

                if (state == SCROLL_STATE_IDLE) {
                    //Below are code to achieve infinite scroll.
                    //We silently and immediately flip the item to the first / last.
                    if (isInfinite) {
                        if (getAdapter() == null) return;
                        int itemCount = getAdapter().getCount();
                        if (itemCount < 2) {
                            return;
                        }
                        int index = getCurrentItem();
                        if (index == 0) {
                            setCurrentItem(itemCount - 2, false); //Real last item
                        } else if (index == itemCount - 1) {
                            setCurrentItem(1, false); //Real first item
                        }
                    }

                    if (indicatorPageChangeListener != null) {
                        indicatorPageChangeListener.onIndicatorProgress(getIndicatorPosition(), 1);
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

    public void resumeAutoScroll() {
        isAutoScrollResumed = true;
        autoScrollHandler.postDelayed(autoScrollRunnable, interval);
    }

    public void pauseAutoScroll() {
        isAutoScrollResumed = false;
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }


    /**
     * A method that helps you integrate a ViewPager Indicator.
     * This method returns the expected position (Starting from 0) of indicators.
     * This method should be used after currentPagePosition is updated.
     */
    public int getIndicatorPosition() {
        if (!isInfinite) {
            return currentPagePosition;
        } else {
            if (!(getAdapter() instanceof LoopingPagerAdapter)) return currentPagePosition;
            if (currentPagePosition == 0) { //Dummy last item is selected. Indicator should be at the last one
                return ((LoopingPagerAdapter) getAdapter()).getListCount() - 1;
            } else if (currentPagePosition == ((LoopingPagerAdapter) getAdapter()).getLastItemPosition() + 1) {
                //Dummy first item is selected. Indicator should be at the first one
                return 0;
            } else {
                return currentPagePosition - 1;
            }
        }
    }

    /**
     * A method that helps you integrate a ViewPager Indicator.
     * This method returns the expected position (Starting from 0) of indicators.
     * This method should be used before currentPagePosition is updated, when user is trying to
     * select a different page, i.e. onPageScrolled() is triggered.
     */
    public int getSelectingIndicatorPosition(boolean isToTheRight) {
        if (scrollState == SCROLL_STATE_SETTLING || scrollState == SCROLL_STATE_IDLE
                || (previousScrollState == SCROLL_STATE_SETTLING && scrollState == SCROLL_STATE_DRAGGING)) {
            return getIndicatorPosition();
        }
        int delta = isToTheRight ? 1 : -1;
        if (isInfinite) {
            if (!(getAdapter() instanceof LoopingPagerAdapter)) return currentPagePosition + delta;
            if (currentPagePosition == 1 && !isToTheRight) { //Special case for first page to last page
                return ((LoopingPagerAdapter) getAdapter()).getLastItemPosition() - 1;
            } else if (currentPagePosition == ((LoopingPagerAdapter) getAdapter()).getLastItemPosition()
                    && isToTheRight) { //Special case for last page to first page
                return 0;
            } else {
                return currentPagePosition + delta - 1;
            }
        } else {
            return currentPagePosition + delta;
        }
    }

    /**
     * A method that helps you integrate a ViewPager Indicator.
     * This method returns the expected count of indicators.
     */
    public int getIndicatorCount() {
        if (getAdapter() instanceof LoopingPagerAdapter) {
            return ((LoopingPagerAdapter) getAdapter()).getListCount();
        } else {
            return getAdapter().getCount();
        }
    }


    /**
     * This function needs to be called if dataSet has changed,
     * in order to reset current selected item and currentPagePosition and autoPageSelectionLock.
     */
    public void reset() {
        if (isInfinite) {
            setCurrentItem(1, false);
            currentPagePosition = 1;
        } else {
            setCurrentItem(0, false);
            currentPagePosition = 0;
        }
    }

    public void setIndicatorSmart(boolean isIndicatorSmart) {
        this.isIndicatorSmart = isIndicatorSmart;
    }

    public void setIndicatorPageChangeListener(IndicatorPageChangeListener callback) {
        this.indicatorPageChangeListener = callback;
    }

    public interface IndicatorPageChangeListener {
        void onIndicatorProgress(int selectingPosition, float progress);

        void onIndicatorPageChange(int newIndicatorPosition);
    }

    public void setInterval(int interval) {
        this.interval = interval;
        resetAutoScroll();
    }

    private void resetAutoScroll() {
        pauseAutoScroll();
        resumeAutoScroll();
    }

}

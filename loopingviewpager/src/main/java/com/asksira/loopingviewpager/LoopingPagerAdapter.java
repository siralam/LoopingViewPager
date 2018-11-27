package com.asksira.loopingviewpager;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

/**
 * A Pager Adapter that supports infinite loop.
 * This is achieved by adding a fake item at both beginning and the last,
 * And then silently changing to the same, real item, thus looks like infinite.
 */

public abstract class LoopingPagerAdapter<T> extends PagerAdapter {

    protected Context context;
    protected List<T> itemList;
    protected SparseArray<View> viewCache = new SparseArray<>();

    protected boolean isInfinite = false;
    protected boolean canInfinite = true;

    private boolean dataSetChangeLock = false;

    public LoopingPagerAdapter(Context context, List<T> itemList, boolean isInfinite) {
        this.context = context;
        this.isInfinite = isInfinite;
        setItemList(itemList);
    }

    public void setItemList(List<T> itemList) {
        viewCache = new SparseArray<>();
        this.itemList = itemList;
        canInfinite = itemList.size() > 1;
        notifyDataSetChanged();
    }

    /**
     * Child should override this method and return the View that it wish to inflate.
     * View binding with data should be in another method - bindView().
     *
     * @param listPosition The current list position for you to determine your own view type.
     */
    protected abstract View inflateView(int viewType, ViewGroup container, int listPosition);

    /**
     * Child should override this method to bind the View with data.
     * If you wish to implement ViewHolder pattern, you may use setTag() on the convertView and
     * pass in your ViewHolder.
     *
     * @param convertView  The View that needs to bind data with.
     * @param listPosition The current list position for you to get data from itemList.
     */
    protected abstract void bindView(View convertView, int listPosition, int viewType);

    public T getItem(int listPosition) {
        if (listPosition >= 0 && listPosition < itemList.size()) {
            return itemList.get(listPosition);
        } else {
            return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int listPosition = (isInfinite && canInfinite) ? getListPosition(position) : position;

        int viewType = getItemViewType(listPosition);

        View convertView;
        if (viewCache.get(viewType, null) == null) {
            convertView = inflateView(viewType, container, listPosition);
        } else {
            convertView = viewCache.get(viewType);
            viewCache.remove(viewType);
        }

        bindView(convertView, listPosition, viewType);

        container.addView(convertView);

        return convertView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int listPosition = (isInfinite && canInfinite) ? getListPosition(position) : position;

        container.removeView((View) object);
        if (!dataSetChangeLock) viewCache.put(getItemViewType(listPosition), (View) object);
    }

    @Override
    public void notifyDataSetChanged() {
        dataSetChangeLock = true;
        super.notifyDataSetChanged();
        dataSetChangeLock = false;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (itemList != null) {
            count = itemList.size();
        }
        if (isInfinite && canInfinite) {
            return count + 2;
        } else {
            return count;
        }
    }

    /**
     * Allow child to implement view type by overriding this method.
     * instantiateItem() will call this method to determine which view to recycle.
     *
     * @param listPosition Determine view type using listPosition.
     * @return a key (View type ID) in the form of integer,
     */
    protected int getItemViewType(int listPosition) {
        return 0;
    }

    public int getListCount() {
        return itemList == null ? 0 : itemList.size();
    }

    private int getListPosition(int position) {
        if (!(isInfinite && canInfinite)) return position;
        int listPosition;
        if (position == 0) {
            listPosition = getCount() - 1 - 2; //First item is a dummy of last item
        } else if (position > getCount() - 2) {
            listPosition = 0; //Last item is a dummy of first item
        } else {
            listPosition = position - 1;
        }
        return listPosition;
    }

    public int getLastItemPosition() {
        if (isInfinite) {
            return itemList == null ? 0 : itemList.size();
        } else {
            return itemList == null ? 0 : itemList.size() - 1;
        }
    }

    public boolean isInfinite() {
        return this.isInfinite;
    }
}

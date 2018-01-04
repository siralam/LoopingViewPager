package com.asksira.loopingviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A Pager Adapter that supports infinite loop.
 * This is achieved by adding a fake item at both beginning and the last,
 * And then silently changing to the same, real item, thus looks like infinite.
 */

public abstract class LoopingPagerAdapter<T> extends PagerAdapter {

    protected Context context;
    protected ArrayList<T> itemList;
    protected LinkedList<View> viewRecyclingBin = new LinkedList<>();

    protected boolean isInfinite = false;
    protected boolean canInfinite = true;

    public LoopingPagerAdapter (Context context, ArrayList<T> itemList, boolean isInfinite) {
        this.context = context;
        this.isInfinite = isInfinite;
        setItemList(itemList);
    }

    public void setItemList (ArrayList<T> itemList) {
        viewRecyclingBin = new LinkedList<>();
        this.itemList = itemList;
        canInfinite = itemList.size() > 1;
        notifyDataSetChanged();
    }

    /**Child should override this method and return the View that it wish to inflate.
     * View binding with data should be in another method - bindView().
     */
    protected abstract View inflateView();

    /**
     * Child should override this method to bind the View with data.
     * If you wish to implement ViewHolder pattern, you may use setTag() on the convertView and
     * pass in your ViewHolder.
     *
     * @param convertView The View that needs to bind data with.
     */
    protected abstract void bindView(View convertView, int listPosition);

    public T getItem (int listPosition) {
        if (listPosition >= 0 && listPosition < itemList.size()) {
            return itemList.get(listPosition);
        } else {
            return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int listPosition = (isInfinite && canInfinite) ? getListPosition(position) : position;

        View convertView;
        if (viewRecyclingBin.size() == 0) {
            convertView = inflateView();
        } else {
            convertView = viewRecyclingBin.removeFirst();
        }

        bindView(convertView, listPosition);

        container.addView(convertView);

        return convertView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
        viewRecyclingBin.add((View)object);
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

    public int getListCount () {
        return itemList == null ? 0 : itemList.size();
    }

    private int getListPosition (int position) {
        if (!(isInfinite && canInfinite)) return position;
        int arrayListPosition;
        if (position == 0) {
            arrayListPosition = getCount()-1-2; //First item is a dummy of last item
        } else if (position > getCount() -2) {
            arrayListPosition = 0; //Last item is a dummy of first item
        } else {
            arrayListPosition = position - 1;
        }
        return arrayListPosition;
    }

    public int getLastItemPosition() {
        if (isInfinite) {
            return itemList == null ? 0 : itemList.size();
        } else {
            return itemList == null ? 0 : itemList.size()-1;
        }
    }

    public boolean isInfinite () {
        return this.isInfinite;
    }
}

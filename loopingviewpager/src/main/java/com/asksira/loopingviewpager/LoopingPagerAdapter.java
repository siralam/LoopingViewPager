package com.asksira.loopingviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A Pager Adapter that supports infinite loop.
 * This is achieved by adding a fake item at both beginning and the last,
 * And then silently changing to the same, real item, thus looks like infinite.
 */

public abstract class LoopingPagerAdapter<T> extends PagerAdapter {

    protected Context context;
    protected ArrayList<T> itemList;
    protected SparseArray<View> viewList = new SparseArray<>();

    protected boolean isInfinite = false;

    public LoopingPagerAdapter (Context context, ArrayList<T> itemList, boolean isInfinite) {
        this.context = context;
        this.isInfinite = isInfinite;
        setItemList(itemList);
    }

    public void setItemList (ArrayList<T> itemList) {
        this.itemList = itemList;
        if (itemList.size() < 2) { //Meaningless to be infinite if there is only 1 item
            this.isInfinite = false;
        }
    }

    /**Child should override this method and return the View that it wish to instantiate.
     * View binding with data should also be occurred here.
     *
     * @param convertView the View that it wants to instantiate. Suggest to use ViewHolder pattern.
     */
    protected abstract View getItemView(View convertView, int listPosition, ViewPager container);

    public T getItem (int listPosition) {
        if (listPosition >= 0 && listPosition < itemList.size()) {
            return itemList.get(listPosition);
        } else {
            return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int listPosition = isInfinite ? getListPosition(position) : position;

        View convertView = viewList.get(position, null);
        convertView = getItemView(convertView, listPosition, (ViewPager)container);
        viewList.put(position, convertView);
        return convertView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
        viewList.remove(position);
        object = null;
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
        if (isInfinite) {
            return count + 2;
        } else {
            return count;
        }
    }

    public int getListCount () {
        return itemList == null ? 0 : itemList.size();
    }

    private int getListPosition (int position) {
        if (!isInfinite) return position;
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
}

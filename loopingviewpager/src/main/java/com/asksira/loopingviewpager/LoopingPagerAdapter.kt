package com.asksira.loopingviewpager

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import androidx.viewpager.widget.PagerAdapter

/**
 * A Pager Adapter that supports infinite loop.
 * This is achieved by adding a fake item at both beginning and the last,
 * And then silently changing to the same, real item, thus looks like infinite.
 */
abstract class LoopingPagerAdapter<T>(
    itemList: List<T>,
    isInfinite: Boolean
) : PagerAdapter() {
    var itemList: List<T>? = null
        set(value) {
            field = value
            viewCache = SparseArray()
            canInfinite = (itemList?.size ?: 0) > 1
            notifyDataSetChanged()
        }
    protected var viewCache = SparseArray<ViewBinding>()
    var isInfinite = false
        protected set
    protected var canInfinite = true
    private var dataSetChangeLock = false

    init {
        this.isInfinite = isInfinite
        this.itemList = itemList
    }

    /**
     * Child should override this method and return the View that it wish to inflate.
     * View binding with data should be in another method - bindView().
     *
     * @param listPosition The current list position for you to determine your own view type.
     */
    protected abstract fun inflateView(
        viewType: Int,
        container: ViewGroup,
        listPosition: Int
    ): ViewBinding

    /**
     * Child should override this method to bind the View with data.
     * If you wish to implement ViewHolder pattern, you may use setTag() on the convertView and
     * pass in your ViewHolder.
     *
     * @param convertView  The View that needs to bind data with.
     * @param listPosition The current list position for you to get data from itemList.
     */
    protected abstract fun bindView(
        binding: ViewBinding,
        listPosition: Int,
        viewType: Int
    )

    fun getItem(listPosition: Int): T? {
        return if (listPosition >= 0 && listPosition < itemList?.size ?: 0) {
            itemList?.get(listPosition)
        } else {
            null
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val listPosition =
            if (isInfinite && canInfinite) getListPosition(position) else position
        val viewType = getItemViewType(listPosition)
        val convertView: ViewBinding
        if (viewCache[viewType, null] == null) {
            convertView = inflateView(viewType, container, listPosition)
        } else {
            convertView = viewCache[viewType]
            viewCache.remove(viewType)
        }
        bindView(convertView, listPosition, viewType)
        container.addView(convertView.root)
        return convertView
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean {
        return view === (`object` as ViewBinding).root
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        val listPosition =
            if (isInfinite && canInfinite) getListPosition(position) else position
        container.removeView((`object` as ViewBinding).root)
        if (!dataSetChangeLock) viewCache.put(
            getItemViewType(listPosition),
            `object`
        )
    }

    override fun notifyDataSetChanged() {
        dataSetChangeLock = true
        super.notifyDataSetChanged()
        dataSetChangeLock = false
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        val count = itemList?.size ?: 0
        return if (isInfinite && canInfinite) {
            count + 2
        } else {
            count
        }
    }

    /**
     * Allow child to implement view type by overriding this method.
     * instantiateItem() will call this method to determine which view to recycle.
     *
     * @param listPosition Determine view type using listPosition.
     * @return a key (View type ID) in the form of integer,
     */
    protected open fun getItemViewType(listPosition: Int): Int {
        return 0
    }

    val listCount: Int
        get() = itemList?.size ?: 0

    private fun getListPosition(position: Int): Int {
        if (!(isInfinite && canInfinite)) return position
        return when {
            position == 0 -> {
                count - 1 - 2 //First item is a dummy of last item
            }
            position > count - 2 -> {
                0 //Last item is a dummy of first item
            }
            else -> {
                position - 1
            }
        }
    }

    val lastItemPosition: Int
        get() = if (isInfinite) {
            itemList?.size ?: 0
        } else {
            itemList?.size ?: 1 - 1
        }
}
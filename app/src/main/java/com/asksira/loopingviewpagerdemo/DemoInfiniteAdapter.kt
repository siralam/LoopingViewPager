package com.asksira.loopingviewpagerdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import java.util.*

class DemoInfiniteAdapter(
    itemList: ArrayList<Int>,
    isInfinite: Boolean
) : LoopingPagerAdapter<Int>(itemList, isInfinite) {

    override fun getItemViewType(listPosition: Int): Int {
        return if (itemList?.get(listPosition) == 0) VIEW_TYPE_SPECIAL else VIEW_TYPE_NORMAL
    }

    override fun inflateView(
        viewType: Int,
        container: ViewGroup,
        listPosition: Int
    ): View {
        return if (viewType == VIEW_TYPE_SPECIAL) LayoutInflater.from(
            container.context
        ).inflate(R.layout.item_special, container, false) else LayoutInflater.from(container.context)
            .inflate(R.layout.item_pager, container, false)
    }

    override fun bindView(
        convertView: View,
        listPosition: Int,
        viewType: Int
    ) {
        if (viewType == VIEW_TYPE_SPECIAL) return
        convertView.findViewById<View>(R.id.image)
            .setBackgroundColor(convertView.context.resources.getColor(getBackgroundColor(listPosition)))
        val description = convertView.findViewById<TextView>(R.id.description)
        description.text = itemList?.get(listPosition).toString()
    }

    private fun getBackgroundColor(number: Int): Int {
        return when (number) {
            0 -> android.R.color.holo_red_light
            1 -> android.R.color.holo_orange_light
            2 -> android.R.color.holo_green_light
            3 -> android.R.color.holo_blue_light
            4 -> android.R.color.holo_purple
            5 -> android.R.color.black
            else -> android.R.color.black
        }
    }

    companion object {
        private const val VIEW_TYPE_NORMAL = 100
        private const val VIEW_TYPE_SPECIAL = 101
    }
}
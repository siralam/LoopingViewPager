package com.asksira.loopingviewpagerdemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.asksira.loopingviewpagerdemo.databinding.ItemPageBinding
import com.asksira.loopingviewpagerdemo.databinding.ItemSpecialBinding
import java.util.*

class DemoInfiniteAdapter(
    itemList: ArrayList<Int>,
    isInfinite: Boolean
) : LoopingPagerAdapter<Int>(itemList, isInfinite) {

    override fun getItemViewType(listPosition: Int): Int {
        return if (itemList?.get(listPosition) == 0) VIEW_TYPE_SPECIAL else VIEW_TYPE_NORMAL
    }

    override fun inflateView(viewType: Int, container: ViewGroup, listPosition: Int): ViewBinding {
        return if (viewType == VIEW_TYPE_SPECIAL) {
            ItemSpecialBinding.inflate(
                LayoutInflater.from(
                    container.context
                ), container, false
            )
        } else {
            ItemPageBinding.inflate(
                LayoutInflater.from(
                    container.context
                ), container, false
            )
        }
    }

    override fun bindView(
        binding: ViewBinding,
        listPosition: Int,
        viewType: Int
    ) {
        if (binding is ItemSpecialBinding) return
        if (binding is ItemPageBinding) {
            binding.image
                .setBackgroundColor(
                    binding.root.context.resources.getColor(
                        getBackgroundColor()
                    )
                )
            binding.message = itemList?.get(listPosition).toString()
        }
    }

    val colorSelector = mutableListOf<Int>(
        android.R.color.holo_red_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_green_light,
        android.R.color.holo_blue_light,
        android.R.color.holo_purple
    )

    private fun getBackgroundColor(): Int {
        return colorSelector.random()
    }

    companion object {
        private const val VIEW_TYPE_NORMAL = 100
        private const val VIEW_TYPE_SPECIAL = 101
    }
}
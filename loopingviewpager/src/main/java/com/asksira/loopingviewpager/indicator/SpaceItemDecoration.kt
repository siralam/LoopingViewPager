package com.asksira.loopingviewpager.indicator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val rvOrientation = (parent.layoutManager as LinearLayoutManager).orientation
        if (rvOrientation == RecyclerView.HORIZONTAL) {
            outRect.left = if (parent.getChildAdapterPosition(view) == 0) space else (space / 2)
            outRect.right =
                if (parent.getChildAdapterPosition(view) == (parent.layoutManager?.childCount ?: 0))
                    space
                else (space / 2)
        } else {
            outRect.top = if (parent.getChildAdapterPosition(view) == 0) space else (space / 2)
            outRect.bottom =
                if (parent.getChildAdapterPosition(view) == (parent.layoutManager?.childCount ?: 0))
                    space
                else (space / 2)
        }
    }
}
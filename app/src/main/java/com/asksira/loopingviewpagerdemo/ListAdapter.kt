package com.asksira.loopingviewpagerdemo

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager
import java.lang.IllegalArgumentException

class ListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> PagerViewHolder(parent.inflate(R.layout.item_pager, parent, false))
            1 -> DummyViewHolder(parent.inflate(R.layout.item_dummy, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PagerViewHolder -> holder.onBind()
            is DummyViewHolder -> holder.onBind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            1
        }
    }

    override fun getItemCount(): Int {
        return 100
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is PagerViewHolder -> holder.onAttach()
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is PagerViewHolder -> holder.onDetach()
        }
    }

    inner class PagerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val pager: LoopingViewPager = itemView.findViewById(R.id.viewpager)
        private var adapter: DemoInfiniteAdapter? = null

        fun onBind() {
            adapter = DemoInfiniteAdapter(createDummyItems(), true)
            pager.adapter = adapter
        }

        fun onAttach() {
            pager.resumeAutoScroll()
        }

        fun onDetach() {
            pager.pauseAutoScroll()
        }

    }

    inner class DummyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun onBind(position: Int) {
            itemView.findViewById<TextView>(R.id.tvDummy).text = position.toString()
        }

    }
}
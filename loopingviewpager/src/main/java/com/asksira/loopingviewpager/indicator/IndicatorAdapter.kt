package com.asksira.loopingviewpager.indicator

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.R
import com.asksira.loopingviewpager.databinding.ItemIndicatorBinding

class IndicatorAdapter(
    var indicatorsCount: Int,
    var selectedPosition: Int
) : RecyclerView.Adapter<IndicatorAdapter.IndicatorViewHolder>() {

    var indicatorSize: Int = 0
    var iconSelected: Drawable? = null
    var iconNormal: Drawable? = null

    class IndicatorViewHolder(val itemBinding: ItemIndicatorBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndicatorViewHolder {
        val binding =
            ItemIndicatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (indicatorSize > 0) {
            val layoutParams = binding.imageIndicator.layoutParams
            layoutParams.width = indicatorSize
            layoutParams.height = indicatorSize
            binding.imageIndicator.layoutParams = layoutParams
        }
        return IndicatorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IndicatorViewHolder, position: Int) {
        holder.itemBinding.imageIndicator.setImageDrawable(
            getItemBackground(holder.itemView.context, holder.absoluteAdapterPosition)
        )
    }

    private fun getItemBackground(context: Context, position: Int): Drawable? {
        return if (position == selectedPosition) {
            iconSelected ?: ContextCompat.getDrawable(
                context,
                R.drawable.indicator_selected
            )

        } else {
            iconNormal ?: ContextCompat.getDrawable(
                context,
                R.drawable.indicator_unselected
            )
        }
    }

    override fun getItemCount(): Int {
        return indicatorsCount
    }
}
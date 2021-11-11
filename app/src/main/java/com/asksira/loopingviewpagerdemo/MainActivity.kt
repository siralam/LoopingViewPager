package com.asksira.loopingviewpagerdemo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.asksira.loopingviewpagerdemo.databinding.ActivityMainBinding
import com.asksira.loopingviewpagerdemo.databinding.ItemSelectedBinding
import com.asksira.loopingviewpagerdemo.databinding.ItemUnselectedBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var adapter: DemoInfiniteAdapter? = null
    private var currentDataSet = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.changeDataset.setOnClickListener { changeDataset() }
        binding.btnListActivity.setOnClickListener {
            startActivity(Intent(this, ListActivity::class.java))
        }


        adapter = DemoInfiniteAdapter(createDummyItems(), true)
        binding.viewpager.adapter = adapter

        binding.viewpager.onIndicatorProgress = { selectingPosition, progress ->
            binding.indicator.onPageScrolled(
                selectingPosition,
                progress
            )
        }
        setupClicks()
        binding.indicator.updateIndicatorCounts(binding.viewpager.indicatorCount)
    }

    private fun setupClicks() {
        val pageSelectorListener =
            View.OnClickListener { v ->
                val number =
                    Integer.valueOf((v as Button).text.toString())
                binding.viewpager.currentItem =
                    if (adapter?.isInfinite == true) number else number - 1
            }
        binding.page1.setOnClickListener(pageSelectorListener)
        binding.page2.setOnClickListener(pageSelectorListener)
        binding.page3.setOnClickListener(pageSelectorListener)
        binding.page4.setOnClickListener(pageSelectorListener)
        binding.page5.setOnClickListener(pageSelectorListener)
        binding.page6.setOnClickListener(pageSelectorListener)
    }

    override fun onResume() {
        super.onResume()
        binding.viewpager.resumeAutoScroll()
    }

    override fun onPause() {
        binding.viewpager.pauseAutoScroll()
        super.onPause()
    }

    private fun changeDataset() {
        if (currentDataSet == 1) {
            adapter?.itemList = createSecondDummyItems().toList()
            currentDataSet++
            toggleSixButtons(false)
        } else if (currentDataSet == 2) {
            adapter?.itemList = createThirdDummyItems().toList()
            currentDataSet++
            toggleSixButtons(false)
        } else {
            adapter?.itemList = createDummyItems().toList()
            currentDataSet = 1
            toggleSixButtons(true)
        }
        binding.indicator.updateIndicatorCounts(binding.viewpager.indicatorCount)
        binding.viewpager.reset()
    }

    private fun toggleSixButtons(isVisible: Boolean) {
        val status = if (isVisible) View.VISIBLE else View.GONE
        binding.changePageLabel.visibility = status
        binding.page1.visibility = status
        binding.page2.visibility = status
        binding.page3.visibility = status
        binding.page4.visibility = status
        binding.page5.visibility = status
        binding.page6.visibility = status
    }
}
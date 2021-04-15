package com.asksira.loopingviewpagerdemo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.asksira.loopingviewpager.LoopingViewPager
import com.asksira.loopingviewpager.indicator.CustomShapePagerIndicator
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: LoopingViewPager
    private var adapter: DemoInfiniteAdapter? = null
    private lateinit var indicatorView: CustomShapePagerIndicator
    private lateinit var changeDataSetButton: Button
    private lateinit var changePageLabel: TextView
    private lateinit var page1: Button
    private lateinit var page2: Button
    private lateinit var page3: Button
    private lateinit var page4: Button
    private lateinit var page5: Button
    private lateinit var page6: Button
    private var currentDataSet = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager = findViewById(R.id.viewpager)
        indicatorView = findViewById(R.id.indicator)
        changeDataSetButton = findViewById(R.id.change_dataset)
        changeDataSetButton.setOnClickListener(View.OnClickListener { changeDataset() })
        changePageLabel = findViewById(R.id.change_page_label)
        page1 = findViewById(R.id.page1)
        page2 = findViewById(R.id.page2)
        page3 = findViewById(R.id.page3)
        page4 = findViewById(R.id.page4)
        page5 = findViewById(R.id.page5)
        page6 = findViewById(R.id.page6)

        adapter = DemoInfiniteAdapter(createDummyItems(), true)
        viewPager.adapter = adapter

        //Custom bind indicator
        indicatorView.highlighterViewDelegate = {
            val highlighter = View(this)
            highlighter.layoutParams = FrameLayout.LayoutParams(16.dp(), 2.dp())
            highlighter.setBackgroundColor(getColorCompat(R.color.white))
            highlighter
        }
        indicatorView.unselectedViewDelegate = {
            val unselected = View(this)
            unselected.layoutParams = LinearLayout.LayoutParams(16.dp(), 2.dp())
            unselected.setBackgroundColor(getColorCompat(R.color.white))
            unselected.alpha = 0.4f
            unselected
        }
        viewPager.onIndicatorProgress = { selectingPosition, progress -> indicatorView.onPageScrolled(selectingPosition, progress) }
        val pageSelector =
            View.OnClickListener { v ->
                val number =
                    Integer.valueOf((v as Button).text.toString())
                viewPager.currentItem = if (adapter?.isInfinite == true) number else number - 1
            }
        page1.setOnClickListener(pageSelector)
        page2.setOnClickListener(pageSelector)
        page3.setOnClickListener(pageSelector)
        page4.setOnClickListener(pageSelector)
        page5.setOnClickListener(pageSelector)
        page6.setOnClickListener(pageSelector)
        indicatorView.updateIndicatorCounts(viewPager.indicatorCount)
    }

    override fun onResume() {
        super.onResume()
        viewPager.resumeAutoScroll()
    }

    override fun onPause() {
        viewPager.pauseAutoScroll()
        super.onPause()
    }

    private fun createDummyItems(): ArrayList<Int> {
        val items = ArrayList<Int>()
        items.add(0, 1)
        items.add(1, 2)
        items.add(2, 3)
        items.add(3, 4)
        items.add(4, 5)
        items.add(5, 6)
        items.add(6, 0)
        return items
    }

    private fun createSecondDummyItems(): ArrayList<Int> {
        val items = ArrayList<Int>()
        items.add(0, 1)
        items.add(1, 2)
        return items
    }

    private fun createThirdDummyItems(): ArrayList<Int> {
        val items = ArrayList<Int>()
        items.add(0, 1)
        return items
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
        indicatorView.updateIndicatorCounts(viewPager.indicatorCount)
        viewPager.reset()
    }

    private fun toggleSixButtons(isVisible: Boolean) {
        val status = if (isVisible) View.VISIBLE else View.GONE
        changePageLabel.visibility = status
        page1.visibility = status
        page2.visibility = status
        page3.visibility = status
        page4.visibility = status
        page5.visibility = status
        page6.visibility = status
    }
}
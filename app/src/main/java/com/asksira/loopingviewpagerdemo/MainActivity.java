package com.asksira.loopingviewpagerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.asksira.loopingviewpager.AutoScrollViewPager;
import com.rd.PageIndicatorView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    AutoScrollViewPager viewPager;
    DemoInfiniteAdapter adapter;
    PageIndicatorView indicatorView;
    Button changeDataSetButton;

    private int currentDataSet = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        indicatorView = findViewById(R.id.indicator);
        changeDataSetButton = findViewById(R.id.change_dataset);
        changeDataSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDataset();
            }
        });

        adapter = new DemoInfiniteAdapter(this, createDummyItems(), true);
        viewPager.setAdapter(adapter);

        //Custom bind indicator
        indicatorView.setCount(viewPager.getIndicatorCount());
        viewPager.setIndicatorPageChangeListener(new AutoScrollViewPager.IndicatorPageChangeListener() {
            @Override
            public void onIndicatorProgress(int selectingPosition, float progress) {
                indicatorView.setProgress(selectingPosition, progress);
            }

            @Override
            public void onIndicatorPageChange(int newIndicatorPosition) {
                indicatorView.setSelection(newIndicatorPosition);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.resumeAutoScroll();
    }

    @Override
    protected void onPause() {
        viewPager.pauseAutoScroll();
        super.onPause();
    }

    private ArrayList<Integer> createDummyItems () {
        ArrayList<Integer> items = new ArrayList<>();
        items.add(0, 1);
        items.add(1, 2);
        items.add(2, 3);
        items.add(3, 4);
        items.add(4, 5);
        items.add(5, 6);
        return items;
    }

    private ArrayList<Integer> createAnotherDummyItems() {
        ArrayList<Integer> items = new ArrayList<>();
        items.add(0, 1);
        return items;
    }

    private void changeDataset () {
        if (currentDataSet == 1) {
            adapter.setItemList(createAnotherDummyItems());
            currentDataSet = 2;
        } else {
            adapter.setItemList(createDummyItems());
            currentDataSet = 1;
        }
        indicatorView.setCount(viewPager.getIndicatorCount());
        viewPager.reset();
    }
}

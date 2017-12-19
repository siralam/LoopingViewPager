package com.asksira.loopingviewpagerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.asksira.loopingviewpager.AutoScrollViewPager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    AutoScrollViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);

        viewPager.setAdapter(new DemoInfiniteAdapter(this, createDummyItems(), true));
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

    private ArrayList<PagerItem> createDummyItems () {
        ArrayList<PagerItem> items = new ArrayList<>();
        items.add(0, new PagerItem("https://store.storeimages.cdn-apple.com/8750/as-images.apple.com/is/image/AppleInc/aos/published/images/m/ac/macbook/default/macbook-default-image-201604?wid=2000&hei=856&fmt=jpeg&qlt=95&op_sharpen=0&resMode=bicub&op_usm=0.5,0.5,0,0&iccEmbed=0&layer=comp&.v=1493227953329", "Macbook"));
        items.add(1, new PagerItem("https://cdn.shopify.com/s/files/1/1162/6052/products/product-image-mug_3_1024x1024.png?v=1458576844", "NesCafe"));
        items.add(2, new PagerItem("https://d63z0236ucgyj.cloudfront.net/products/2805406/product/tempo-tissue-single-box-soft-strong-pack-of-80-574532cbb75a5.jpg", "Tempo"));
        items.add(3, new PagerItem("https://i.ytimg.com/vi/yhjXTatF9dE/maxresdefault.jpg", "Sora no Kiseki"));
        items.add(4, new PagerItem("https://www.daltonsbusiness.com/advice/wp-content/uploads/2017/10/mc.png", "McDonalds"));
        items.add(5, new PagerItem("https://i.imgur.com/VtQ3BKV.jpg", "Sen no Kiseki"));
        return items;
    }
}

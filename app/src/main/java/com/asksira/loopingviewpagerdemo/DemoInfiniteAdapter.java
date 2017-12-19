package com.asksira.loopingviewpagerdemo;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

public class DemoInfiniteAdapter extends LoopingPagerAdapter<PagerItem> {

    public DemoInfiniteAdapter(Context context, ArrayList<PagerItem> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    @Override
    protected View getItemView(View convertView, int listPosition, ViewPager container) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pager, null);
            container.addView(convertView);
        }
        SimpleDraweeView image = convertView.findViewById(R.id.image);
        image.setImageURI(itemList.get(listPosition).getImageUrl());
        TextView description = convertView.findViewById(R.id.description);
        description.setText(itemList.get(listPosition).getDescription());
        return convertView;
    }
}

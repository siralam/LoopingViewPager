package com.asksira.loopingviewpagerdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;

import java.util.ArrayList;

public class DemoInfiniteAdapter extends LoopingPagerAdapter<Integer> {

    public DemoInfiniteAdapter(Context context, ArrayList<Integer> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    @Override
    protected View inflateView() {
        return LayoutInflater.from(context).inflate(R.layout.item_pager, null);
    }

    @Override
    protected void bindView(View convertView, int listPosition) {
        convertView.findViewById(R.id.image).setBackgroundColor(context.getResources().getColor(getBackgroundColor(listPosition)));
        TextView description = convertView.findViewById(R.id.description);
        description.setText(String.valueOf(itemList.get(listPosition)));
    }


    private int getBackgroundColor (int number) {
        switch (number) {
            case 0:
                return android.R.color.holo_red_light;
            case 1:
                return android.R.color.holo_orange_light;
            case 2:
                return android.R.color.holo_green_light;
            case 3:
                return android.R.color.holo_blue_light;
            case 4:
                return android.R.color.holo_purple;
            case 5:
                return android.R.color.black;
            default:
                return android.R.color.black;
        }
    }
}

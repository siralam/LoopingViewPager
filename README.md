# LoopingViewPager

A ViewPager and a PagerAdapter that can:

1. AutoScroll (On/Off able)
2. Infinite Loop (On/Off able)
3. ViewPager's height can be wrap_content
4. Adjustable auto scroll interval
5. Won't scroll nor loop if there is only 1 item
6. Works well with notifyDataSetChanged()
7. Supports page indicators

## Demo Effect

Auto Scroll + Infinite Loop  
<img src="https://raw.githubusercontent.com/siralam/LoopingViewPager/master/readme_images/loopingViewPager_auto.gif" width="360" height="640" alt="Auto Scroll and Infinite Loop" />

Manual Scroll + Infinite Loop  
<img src="https://raw.githubusercontent.com/siralam/LoopingViewPager/master/readme_images/loopingViewPager_manual.gif" width="360" height="640" alt="Auto Scroll and Infinite Loop" />

## Why this library

Although there are already quite a number of similar libraries out there,  
I cannot find one that fits all of the below requirements:  

1. Sufficient documentation
2. Last updated in less than 3 years
3. Good infinite looping effect 
4. Configurable auto-scroll
5. ViewPager that supports wrap_content (or at least aspect ratio)
6. Good support with Page Indicators

Especially for 6, even some of them supports, they provide built-in indicators only; or does not tell user how to implement their own indicator.  
I wrote this library to tackle all of these problems I faced after trying a whole day with other libraries.

## Usage

### Add to Project

First make sure `jcenter()` is included as a repository in your **project**'s build.gradle:  

```
allprojects {
    repositories {
        jcenter()
    }
}
```

And then add the below to your app's build.gradle:  

```
    implementation 'com.asksira.android:loopingviewpager:1.0.0'
```

### Step 1: Create LoopingViewPager in XML

```xml
    <com.asksira.loopingviewpager.LoopingViewPager
        android:id="@+id/viewpager"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:isInfinite="true"
        app:autoScroll="true"
        app:scrollInterval="2000"
        app:wrap_content="true"/>
```

| Attribute Name   | Default | Allowed Values                |
|:-----------------|:--------|:------------------------------|
| isInfinite       | false   | true / false                  |
| autoScroll       | false   | true / false                  |
| wrap_content     | true    | true / false                  |
| scrollInterval   | 5000    | any integer (represents ms)   | 

If you wonder why you need to set `app:wrap_content="true"`, take a look at [this Stackoverflow post](https://stackoverflow.com/questions/8394681/android-i-am-unable-to-have-viewpager-wrap-content).

### Step 2: Create your PagerAdapter that extends LoopingPagerAdapter

You should
1. Specify the data type in the generic (`<DataType>`)
2. Create your own constructor according to this `DataType`
3. override `getItemView`

```java
public class DemoInfiniteAdapter extends LoopingPagerAdapter<Integer> {

    public DemoInfiniteAdapter(Context context, ArrayList<Integer> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    //You should return the View (With data binded) to display in this method.
    @Override
    protected View getItemView(View convertView, int listPosition, ViewPager container) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pager, null);
            container.addView(convertView);
        }

        //Bind your view elements with data in itemList here. 
        //You can also consider using a ViewHolder pattern.
        //Below is just an example in the demo app.
        convertView.findViewById(R.id.image).setBackgroundColor(context.getResources().getColor(getBackgroundColor(listPosition)));
        TextView description = convertView.findViewById(R.id.description);
        description.setText(String.valueOf(itemList.get(listPosition)));

        return convertView;
    }
}
```

### Step 3: Bind LoopingViewPager with your Adapter

```java
        adapter = new DemoInfiniteAdapter(context, dataItems, true);
        viewPager.setAdapter(adapter);
```

### Step 4: Resume and Pause autoScroll in your Activity (If you need autoScroll)

```java
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
```

### Handling dataSet change

If you have new data to update to your adapter, simply call:

```java
adapter.setItemList(newItemList);
viewPager.reset(); //In order to reset the current page position
```

## How do I integrate a Page Indicator?

I don't provide a built-in page indicator because:
1. ViewPager and Indicator are logically separated
2. I want to make this library adaptable to all page indicators

With that said, I personally suggest using this [PageIndicatorView](https://github.com/romandanylyk/PageIndicatorView).  
I create this demo and tested using this library.

### Principle

There are 2 callbacks in `LoopingViewPager` that are designed to tell a PageIndicator 2 things:
1. I am now being scrolled to a new page, please update your indicator transition position;
2. I am now being selected to a new page, please update your indicator selected position.

And a public method `getIndicatorCount()` that can tell the indicator how many indicators(dots) should it show.

### Example

And here is an example using [PageIndicatorView](https://github.com/romandanylyk/PageIndicatorView):

```java
        //Do not bind IndicatorView directly with ViewPager.
        //Below is how we achieve the effect by binding manually.

        //Tell the IndicatorView that how many indicators should it display:
        indicatorView.setCount(viewPager.getIndicatorCount());

        //Set IndicatorPageChangeListener on LoopingViewPager.
        //When the methods are called, update the Indicator accordingly.
        viewPager.setIndicatorPageChangeListener(new LoopingViewPager.IndicatorPageChangeListener() {
            @Override
            public void onIndicatorProgress(int selectingPosition, float progress) {
            }

            @Override
            public void onIndicatorPageChange(int newIndicatorPosition) {
                indicatorView.setSelection(newIndicatorPosition);
            }
        });
```

Don't forget to update the indicator counts if you updated items in adapter:

```java
indicatorView.setCount(viewPager.getIndicatorCount());
```

By implementing this way, you can basically use any indicators you like, as long as that indicator allows you to configure programmatically (1) The number of indicators; (2) Which indicator is selected. And even, if it supports, (3) The progress of indicator transition effect.

### Wait, if you want interactive indicator transition effect, please read this section

However, due to [this bug of PageIndicatorView](https://github.com/romandanylyk/PageIndicatorView/issues), the interactive transition effect on indicators cannot work properly.  

LoopingViewPager will trigger `onIndicatorProgress()` when scroll state of `LoopingViewPager` is `SCROLL_STATE_DRAGGING`.  
It will not trigger `onIndicatorProgress()` if scroll state is `SCROLL_STATE_SETTLING`.  
In fact, when user releases his finger during swiping (where scroll state changes from `SCROLL_STATE_DRAGGING` to `SCROLL_STATE_SETTLING`), `onPageSelected()` will be called and therefore `onIndicatorPageChange()`.  
LoopingViewPager expects the indicator will be able to **finish the animation by itself** after `indicatorView.setSelection()` (Or corresponding method of other libraries).

(In fact, I tried to trigger `onIndicatorProgress()` even when scroll state is `SCROLL_STATE_SETTLING`. At first it seems to work good.  
However, I cannot find a way to avoid problems that occur when user swipe fastly, i.e. from `SCROLL_STATE_SETTLING` directly to `SCROLL_STATE_DRAGGING` again **before the next page is selected**. So I decided to let indicator handles this.)

For now, if you use [PageIndicatorView](https://github.com/romandanylyk/PageIndicatorView/), do not call anything in `onIndicatorProgress()`.  
If you really want the interactive transition effect during `SCROLL_STATE_DRAGGING`, I would suggest you use another library or implement your own one.

## Release notes

v1.0.1  
- Fixed a bug where getSelectingIndicatorPosition() is returning incorrect value.
- Updated README.md according to PageIndicatorView v1.0.0 update.

## License

```
Copyright 2017 Sira Lam

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated documentation files (the LoopingViewPager), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or 
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```
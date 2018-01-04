# LoopingViewPager

A ViewPager and a PagerAdapter that can:

1. AutoScroll (On/Off able)
2. Infinite Loop (On/Off able)
3. ViewPager's height can be wrap_content / an aspect ratio
4. Adjustable auto scroll interval
5. Won't scroll nor loop if there is only 1 item
6. Works well with notifyDataSetChanged()
7. Supports page indicators
8. Supports different view types

## Demo Effect

<p>
<img src="readme_images/loopingViewPager_auto.gif" width="250" vspace="20" hspace="5" alt="Auto Scroll and Infinite Loop" />
<img src="readme_images/loopingViewPagerDemo_2.gif" width="250" vspace="20" hspace="5" alt="Manual Scroll and Infinite Loop" />
<img src="readme_images/loopingViewPagerDemo_3.gif" width="250" vspace="20" hspace="5" alt="Page skipping" />
</p>

## Why this library

Although there are already quite a number of similar libraries out there,  
I cannot find one that fits all of the below requirements:  

1. Sufficient documentation
2. Last updated in less than 3 years
3. Good infinite looping effect 
4. Configurable auto-scroll
5. ViewPager that supports fixed aspect ratio (Or wrap_content)
6. Good support with Page Indicators

Especially for 6, even some of them supports, they provide built-in indicators only; or does not tell user how to implement their own indicator.  
I wrote this library to tackle all of these problems I faced after trying a whole day with other libraries.

## Usage

### Add to Project

First make sure `jcenter()` is included as a repository in your **project**'s build.gradle:  

```groovy
allprojects {
    repositories {
        jcenter()
    }
}
```

And then add the below to your app's build.gradle:  

```groovy
    implementation 'com.asksira.android:loopingviewpager:1.1.0'
```

### Step 1: Create LoopingViewPager in XML

```xml
    <com.asksira.loopingviewpager.LoopingViewPager
        android:id="@+id/viewpager"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:isInfinite="true"
        app:autoScroll="true"
        app:scrollInterval="5000"
        app:viewpagerAspectRatio="1.33"/>
```

| Attribute Name          | Default | Allowed Values                |
|:------------------------|:--------|:------------------------------|
| isInfinite              | false   | true / false                  |
| autoScroll              | false   | true / false                  |
| viewpagerAspectRatio    | 0       | any float (width / height)    |
| wrap_content            | true    | true / false                  |
| scrollInterval          | 5000    | any integer (represents ms)   | 

viewpagerAspectRatio 0 means does not apply aspectRatio.  
That means, default LoopingViewPager has no aspect ratio and wrap_content is true.  
Once aspect ratio is set, wrap_content will be overrided (meaningless).

In most cases, you should set an aspect ratio.  

If you wonder why you need to set `app:wrap_content="true"`, take a look at [this Stackoverflow post](https://stackoverflow.com/questions/8394681/android-i-am-unable-to-have-viewpager-wrap-content).

### Step 2: Create your PagerAdapter that extends LoopingPagerAdapter

You should
1. Specify the data type in the generic (`<DataType>`)
2. Create your own constructor according to this `DataType`
3. override `inflateView()` and `bindView()`

```java
public class DemoInfiniteAdapter extends LoopingPagerAdapter<Integer> {

    public DemoInfiniteAdapter(Context context, ArrayList<Integer> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    //This method will be triggered if the item View has not been inflated before.
    @Override
    protected View inflateView(int viewType, int listPosition) {
        return LayoutInflater.from(context).inflate(R.layout.item_pager, null);
    }

    //Bind your data with your item View here.
    //Below is just an example in the demo app.
    //You can assume convertView will not be null here.
    //You may also consider using a ViewHolder pattern.
    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {
        convertView.findViewById(R.id.image).setBackgroundColor(context.getResources().getColor(getBackgroundColor(listPosition)));
        TextView description = convertView.findViewById(R.id.description);
        description.setText(String.valueOf(itemList.get(listPosition)));
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

## How do I implement different View types?

Simple! Override one more method in your Adapter:

```java
    @Override
    protected int getItemViewType(int listPosition) {
        //Return your own view type, same as what you did when using RecyclerView
    }
```

And then, of course, according to the `viewtype` parameter passed to you in `inflateView()` and `bindView()`, differentiate what you need to inflate or bind.

You may also refer to the demo app for a complete example.

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

### Advanced: Interactive Indicator Animation

You may have already noticed, there is an interactive effect - where the indicator position follows the swipe action of user, as shown in the second GIF in the demo section.

If you want to do this, use the below instead:

```java
        viewPager.setIndicatorPageChangeListener(new LoopingViewPager.IndicatorPageChangeListener() {
            @Override
            public void onIndicatorProgress(int selectingPosition, float progress) {
                indicatorView.setProgress(selectingPosition, progress);
            }

            @Override
            public void onIndicatorPageChange(int newIndicatorPosition) {
            }
        });
```

In my demo, I am using the [PageIndicatorView](https://github.com/romandanylyk/PageIndicatorView).  
This PageIndicatorView expects you to handle the indicator progress solely by yourself, i.e. The indicator **will not finish its animation if you call setSelection()** on it.  
e.g. I scrolled to 50% from page 1 to page 2. Then I released by finger. `PageIndicatorView` expects you to continue calling `setProgress()` from 0.5, 0.6, all the way to 0.99, 1.0; instead of calling `setCurrentItem()` the moment you released your finger.  
I call this type of indicator **non-smart**. Default handling of LoopingViewPager treats indicator as non-smart.

If you have another `IndicatorView` which is smart (i.e. It will finish the animation by itself if you call `setSelection()` the moment user released his finger), do the following:

```java
        viewPager.setIndicatorSmart(true);
```

By setting this, `LoopingViewPager` will call `onIndicatorProgress()` only when user is dragging, but not after he released his finger.  
Therefore you should call `indicatorView.setSelection(newIndicatorPosition)` in `onIndicatorPageChange()`.


However, I have to warn you that, up to the current release, the effect of `onIndicatorProgress()` is still imperfect on non-smart indicators.  
As far as I can find out, I noticed the below problems:  
1. If user swipes very fastly, i.e. before `ViewPager` reaches `SCROLL_STATE_IDLE`, directly from `SCROLL_STATE_SETTLING` to `SCROLL_STATE_DRAGGING`, the indicator will not move until user released his finger again. This is not obvious unless user is attempting to test this indicator effect.
2. If user skip pages very fastly, e.g. from page 1 to page 6 and then to page 3 quickly, the indicator may appears in a wrong position for a short moment.

if you cannot accept these minor defects, I suggest you use `onIndicatorPageChange()` only.

## Release notes

v1.1.0
- Added support for view type. But therefore changed parameters needed in `inflateView()` and `bindView()`.

v1.0.5
- Added asepct ratio attribute for `LoopingViewPager`  
- Rewrote the way of caching Views in `LoopingPagerAdapter`, and therefore separated inflation and data binding  
- Rewrote the way of implementing ViewPager wrap_content

v1.0.4
- Indicator now works with page skipping as well (By calling `selectCurrentItem()`)
- Leviated indicator fluctuating phenomenon when using `onIndicatorProgress()` callback
- Added option for smart or non-smart indicators


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
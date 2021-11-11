package com.asksira.loopingviewpagerdemo

import java.util.ArrayList

fun createDummyItems(): ArrayList<Int> {
    val items = ArrayList<Int>()
    (0..4).forEach {
        items.add(it + 1)
    }
    return items
}

fun createSecondDummyItems(): ArrayList<Int> {
    val items = ArrayList<Int>()
    items.add(0, 1)
    items.add(1, 2)
    return items
}

fun createThirdDummyItems(): ArrayList<Int> {
    val items = ArrayList<Int>()
    items.add(0, 1)
    return items
}
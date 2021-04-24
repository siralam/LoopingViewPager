package com.asksira.loopingviewpagerdemo

import java.util.ArrayList

fun createDummyItems(): ArrayList<Int> {
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
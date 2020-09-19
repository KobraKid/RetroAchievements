package com.kobrakid.retroachievements.view.viewpager

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Custom implementation of the ViewPager widget, allows for toggling paging.
 */
class ToggleableViewPager : ViewPager {

    private var pagingEnabled = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return pagingEnabled && super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return pagingEnabled && super.onTouchEvent(ev)
    }

    fun setPagingEnabled(pagingEnabled: Boolean) {
        this.pagingEnabled = pagingEnabled
    }
}
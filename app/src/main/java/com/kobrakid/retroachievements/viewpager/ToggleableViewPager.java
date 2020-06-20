package com.kobrakid.retroachievements.viewpager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

/**
 * Custom implementation of the ViewPager widget, allows for toggling paging.
 */
public class ToggleableViewPager extends ViewPager {

    private boolean pagingEnabled = true;

    public ToggleableViewPager(@NonNull Context context) {
        super(context);
    }

    public ToggleableViewPager(@NonNull Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) { return pagingEnabled && super.onInterceptTouchEvent(ev); }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return pagingEnabled && super.onTouchEvent(ev);
    }

    public void setPagingEnabled(boolean pagingEnabled) {
        this.pagingEnabled = pagingEnabled;
    }

}

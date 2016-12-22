package com.example.simpleratingbar;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipeableViewPager extends ViewPager {

  public NonSwipeableViewPager(Context context) {
    super(context);
  }

  public NonSwipeableViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    // Never allow swiping to switch between pages
    return false;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    // Never allow swiping to switch between pages
    return false;
  }
}


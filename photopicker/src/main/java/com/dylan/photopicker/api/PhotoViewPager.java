package com.dylan.photopicker.api;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.dylan.photopicker.R;

/**
 * Author: Dylan
 * Date: 2019/7/24
 * Desc:
 */

public class PhotoViewPager extends ViewPager {
    private float downY;
    public PhotoViewPager(Context context) {
        super(context);
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY=ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                ev.setLocation(ev.getX(),downY);
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

}

package com.dylan.photopicker.api;

import android.support.v4.app.FragmentActivity;

import com.dylan.photopicker.api.listener.GlobalSelectListener;

/**
 * Created by Dylan on 2017/5/24.
 */

public abstract class BasePickerActivity extends FragmentActivity implements GlobalSelectListener {
    protected int maxSelectCount = 9;
    protected int selectCount;

    @Override
    public abstract int select(String dirPath, String picPath);

    @Override
    public abstract int unselect(String dirPath, String picPath);

    protected void setMaxSelectCount(int maxSelectCount) {
        this.maxSelectCount = maxSelectCount;
    }
}

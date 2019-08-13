package com.dylan.photopicker.api.listener;

/**
 * Created by Dylan on 2017/5/23.
 */

public interface GlobalSelectListener {
    int select(String dirPath,String picPath);
    int unselect(String dirPath,String picPath);
}

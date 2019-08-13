package com.dylan.photopicker.api.listener;

import com.dylan.photopicker.api.PhotoBean;

import java.util.List;

/**
 * Created by Dylan on 2017/5/23.
 */

public interface BrowserOperatorListener {
     void dismiss(List<PhotoBean> beanList);
}

package com.dylan.photopicker.api;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.dylan.photopicker.R;
import com.dylan.photopicker.utils.ScreenUtils;

/**
 * Created by Dylan on 2017/5/16.
 */

public class FullScreenDialogFragment extends DialogFragment {
    protected boolean keepStatusBar=true;
    protected boolean isOpenAnim=true;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(R.style.TranslucentNoTitle, R.style.Dialog_FullScreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog=super.onCreateDialog(savedInstanceState);
        if (!keepStatusBar){
            showInFullScreen(dialog.getWindow());
        }
        if (isOpenAnim){
            setAnimation(dialog);
        }
        return dialog;
    }

    protected void removeStatusBar(){
        keepStatusBar=false;
    }
    protected void removeAnimation(){
            isOpenAnim =false;
    }


    protected void setAnimation(Dialog dialog){
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.fullScreenDialogAnim); // 添加动画
    }

    private void showInFullScreen(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
        ViewGroup mContentParent = (ViewGroup) mContentView.getParent();
        View statusBarView = mContentParent.getChildAt(0);
        if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == ScreenUtils.getStatusBarHeight(getActivity())) {
            //移除假的 View
            mContentParent.removeView(statusBarView);
        }
        //ContentView 不预留空间
        if (mContentParent.getChildAt(0) != null) {
            ViewCompat.setFitsSystemWindows(mContentParent.getChildAt(0), false);
        }
        //ChildView 不预留空间
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }
    }
}




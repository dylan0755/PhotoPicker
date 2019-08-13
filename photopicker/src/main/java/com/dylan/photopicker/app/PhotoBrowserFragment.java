package com.dylan.photopicker.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dylan.photopicker.R;
import com.dylan.photopicker.api.FullScreenDialogFragment;
import com.dylan.photopicker.api.PhotoBean;
import com.dylan.photopicker.api.PhotoViewPager;
import com.dylan.photopicker.api.PickerAnimationListener;
import com.dylan.photopicker.api.listener.BrowserOperatorListener;
import com.dylan.photopicker.api.listener.GlobalSelectListener;
import com.dylan.photopicker.api.listener.OnTouchCallBack;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Dylan on 2017/5/15.
 */

public class PhotoBrowserFragment extends FullScreenDialogFragment implements
        View.OnClickListener,OnTouchCallBack {
    private View contentView;
    private PhotoViewPager mViewPager;
    private PhotoBrowserAdapter mAdapter;
    private TextView mIndexIndicator;
    private int totalCount;
    private List<PhotoBean> photoList;
    private int currentIndex;
    private BrowserOperatorListener dismissListener;
    private GlobalSelectListener globalSelectListener;
    private FrameLayout rootLayout;
    private FrameLayout mTopLayout;
    private RelativeLayout mTitleBar;
    private ImageView ivSelectIcon;
    private boolean statusBar = true;
    private String currentPath = "";
    private AnimationSet enterAnim;
    private AnimationSet exitAnim;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeStatusBar();  //去掉状态栏
        //removeAnimation();//取消动画
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (dismissListener != null) dismissListener.dismiss(photoList);
                }

                return false;
            }
        });
        return dialog;
    }

    /**
     * DialogFragment每次调用show都会重新执行oncreateView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_photobrowser, container, false);
            initView(contentView);
        } else {
            ViewGroup vg = (ViewGroup) contentView.getParent();
            vg.removeView(contentView);
            showBar();
        }
        return contentView;
    }


    private void initView(View mainView) {
        mainView.findViewById(R.id.picker_back).setOnClickListener(this);
        rootLayout = (FrameLayout) mainView.findViewById(R.id.contentLayout);
        mTopLayout = (FrameLayout) mainView.findViewById(R.id.toplayout);
        mTitleBar = (RelativeLayout) mainView.findViewById(R.id.rl_titleBar);
        ivSelectIcon = (ImageView) mainView.findViewById(R.id.browseritem_select);
        enterAnim = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.title_bar_enter);
        exitAnim = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.title_bar_exit);
        mViewPager = (PhotoViewPager) mainView.findViewById(R.id.vp_photo);
        mViewPager.setOffscreenPageLimit(2);//这里要设置2，可以使缓存的视图多点，防止再滑动过程中图片的闪烁。
        mViewPager.addOnPageChangeListener(new PageChangeListener());
        mIndexIndicator = (TextView) mainView.findViewById(R.id.left_title);
        if (photoList != null) {
            this.currentPath = photoList.get(currentIndex).getPath();
            mAdapter = new PhotoBrowserAdapter(mViewPager);
           // mAdapter.setBrowserClickListener(this);
            mAdapter.addOnTouchCallBack(this);
            mViewPager.setAdapter(mAdapter);
            totalCount = photoList.size();
            mAdapter.bind(photoList);
            mViewPager.setCurrentItem(currentIndex);
        }
        initEvent();
    }


    private void initEvent() {
        ivSelectIcon.setOnClickListener(this);
        exitAnim.setAnimationListener(new PickerAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mTopLayout != null) {
                    removeBar();
                }
            }
        });
    }

    /**
     * //经过notifyDataSetChanged之后setCurrentItem要用handlerpost来设置，
     * 设置一遍就不需要再用Handler了，除非又执行notifyDataSetChanged
     */
    public void show(FragmentManager manager, String currentPath, List<PhotoBean> list, final int currentIndex) {
        if (!isAdded()&&manager.findFragmentByTag("show")==null){//如果不做判断，第一次快速双击就会报   Fragment already added异常
            super.show(manager, "show");
            this.photoList = list;
            this.currentIndex = currentIndex;
            if (contentView != null) {
                if (!this.currentPath.equals(currentPath)) {
                    this.currentPath = currentPath;
                    mAdapter.bind(photoList);
                } else {//两次打开同一张图片，onPageSelected没有回调，手动设置选中状态
                    setSelectIconRes();
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mViewPager.setCurrentItem(currentIndex);
                    }
                });
            }
        }
    }




    private void showBar() {
        rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        mTopLayout.setVisibility(View.VISIBLE);
        statusBar = true;
    }

    private void removeBar() {
        rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        mTopLayout.setVisibility(View.GONE);
        statusBar = false;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.picker_back) {
            dismiss();
            if (dismissListener != null) dismissListener.dismiss(photoList);
        } else if (v.getId() == R.id.browseritem_select) {
            if (photoList != null) {
                boolean bl = photoList.get(currentIndex).isSelected();
                if (bl) {
                    ivSelectIcon.setImageResource(R.mipmap.ic_checkbox_unselect_small);
                    photoList.get(currentIndex).setSelected(false);
                    if (globalSelectListener != null)globalSelectListener.unselect("", photoList.get(currentIndex).getPath());
                } else {
                    if (globalSelectListener != null){
                        int index=globalSelectListener.select("", photoList.get(currentIndex).getPath());
                        if (index==0){
                            ivSelectIcon.setImageResource(R.mipmap.ic_checkbox_select_small);
                            photoList.get(currentIndex).setSelected(true);
                        }else{
                            Toast.makeText(getContext(),"你最多可以选择"+index+"张图片",Toast.LENGTH_SHORT).show();
                        }

                    }
                }

            }

        }
    }
    /**
     * 点击浏览中的图片，控制状态栏和标题栏的显示
     */

    @Override
    public void singleActionUp() {
        if (statusBar) {
            mTitleBar.startAnimation(exitAnim);
        } else {
            showBar();
            mTitleBar.startAnimation(enterAnim);
        }
    }



    class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageSelected(int position) {
            currentIndex = position;
            setSelectIconRes();
            setIndicator(currentIndex + 1);
        }
    }

    private void setSelectIconRes() {
        boolean flag = photoList.get(currentIndex).isSelected();
        if (flag) {
            ivSelectIcon.setImageResource(R.mipmap.ic_checkbox_select_small);
        } else {
            ivSelectIcon.setImageResource(R.mipmap.ic_checkbox_unselect_small);
        }
    }

    public void setIndicator(int currentIndex) {
        mIndexIndicator.setText(currentIndex + "/" + totalCount);
    }

    public void setDismissListener(BrowserOperatorListener listener) {
        dismissListener = listener;
    }

    public void setGlobalSelectListener(GlobalSelectListener listener) {
        globalSelectListener = listener;
    }


}

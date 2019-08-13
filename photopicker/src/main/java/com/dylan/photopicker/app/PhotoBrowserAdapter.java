package com.dylan.photopicker.app;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.dylan.photopicker.api.FullScreenBitmapLoader;
import com.dylan.photopicker.api.PhotoBean;
import com.dylan.photopicker.api.PhotoView;
import com.dylan.photopicker.api.listener.OnTouchCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dylan on 2017/5/12.
 */


/**
 * 展示整张图片
 */
public class PhotoBrowserAdapter extends PagerAdapter {
    private List<PhotoBean> mDatas;
    private List<PhotoView> imageCaches = new ArrayList<>();
    private Context mContext;
    private FullScreenBitmapLoader loadBitmap;
    private OnTouchCallBack onTouchCallBack;
    private LastRecordItem recordItem;
    public PhotoBrowserAdapter(ViewPager viewPager) {
        mContext = viewPager.getContext();
        loadBitmap = new FullScreenBitmapLoader();
    }

    public ViewPager.LayoutParams getLayoutParam(){
        ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
        lp.width = ViewPager.LayoutParams.MATCH_PARENT;
        lp.height = ViewPager.LayoutParams.MATCH_PARENT;
        lp.gravity= Gravity.CENTER;
        return lp;
    }

    public void bind(List<PhotoBean> list) {
        if (list != null && !list.isEmpty()) {
            mDatas = list;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }


    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (recordItem==null){
            recordItem=new LastRecordItem();
            recordItem.position=position;
            recordItem.photoView= (PhotoView) object;
        }else{
            if (recordItem.position!=position){
                recordItem.photoView.restore();
                recordItem.position=position;
                recordItem.photoView= (PhotoView) object;
            }
        }
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView;
        if (imageCaches.size()==0){
            photoView=new PhotoView(container.getContext());
            photoView.setLayoutParams(getLayoutParam());
        }else{
            photoView=imageCaches.remove(0);
            photoView.setImageBitmap(null);
        }
        loadBitmap.load(mContext, mDatas.get(position).getPath(), photoView);
        container.addView(photoView);
        photoView.addOnTouchCallBack(onTouchCallBack);
        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        PhotoView imageView= (PhotoView) object;
        container.removeView(imageView);
        imageCaches.add(imageView);
    }


    public void addOnTouchCallBack(OnTouchCallBack callBack){
        onTouchCallBack=callBack;
    }

    class LastRecordItem{
        int position ;
        PhotoView photoView;
    }
}

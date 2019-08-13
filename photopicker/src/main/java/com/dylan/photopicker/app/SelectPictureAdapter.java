package com.dylan.photopicker.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.dylan.photopicker.R;
import com.dylan.photopicker.api.ImageLoader;
import com.dylan.photopicker.api.PhotoBean;
import com.dylan.photopicker.api.listener.GlobalSelectListener;

import java.util.ArrayList;
import java.util.List;


public class SelectPictureAdapter extends BaseAdapter {
    public  List<String> hasSelectedList = new ArrayList<String>();
    private List<PhotoBean> mDatas;
    private LayoutInflater inflater;
    private ImageLoader mLoader;
    private GlobalSelectListener globalSelectListener;
    private static final int unselectColor=Color.parseColor("#33000000");
    private static final int selectColor=Color.parseColor("#77000000");
    private static String dirPath;
    private boolean needShowLimitTip=true;
    private String limitSelectTip;
    private boolean isSingleChoice;//单选

    public SelectPictureAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mLoader = ImageLoader.getInstance(5, ImageLoader.Type.FIFO);

    }

    public void isSingleChoice(boolean bl){
        isSingleChoice=bl;
    }

    public void setShowLimitSelectTip(boolean show,String tip){
        needShowLimitTip =show;
        limitSelectTip=tip;
    }

    public List<PhotoBean> getDatas(){
            return mDatas;
    }




    public void bind(String dirPath,List<PhotoBean> list) {
        if (list != null && !list.isEmpty()) {
            this.mDatas = list;
            this.dirPath=dirPath;
            notifyDataSetChanged();
        }
    }


    public void bindInCurrentDir(List<PhotoBean> list){
        if (list != null && !list.isEmpty()) {
            this.mDatas = list;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public PhotoBean getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.photopicker_gridview_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //重置状态
        holder.imageview.setColorFilter(null);
       final String filepath=getItem(position).getPath();
        mLoader.loadImage(filepath, holder.imageview);
        if (isSingleChoice){
            holder.selectImg.setVisibility(View.GONE);
        }
        holder.selectImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //ic_nopic.png选中
                if (getItem(position).isSelected()) {
                    holder.setUnSelected();
                    hasSelectedList.remove(filepath);
                    getItem(position).setSelected(false);
                    if (globalSelectListener != null) {
                        globalSelectListener.unselect(dirPath,filepath);
                    }
                } else {
                        if (globalSelectListener != null) {
                            int index=globalSelectListener.select(dirPath,filepath);
                            if (index==0){
                                holder.setSelected();
                                hasSelectedList.add(filepath);
                                getItem(position).setSelected(true);
                            }else{
                                if (needShowLimitTip){
                                  if (limitSelectTip!=null&&!limitSelectTip.isEmpty()){
                                      Toast.makeText(parent.getContext(),limitSelectTip,Toast.LENGTH_SHORT).show();
                                  }else{
                                      Toast.makeText(parent.getContext(),"你最多可以选择"+index+"张图片",Toast.LENGTH_SHORT).show();
                                  }
                                }

                            }
                        }
                }
            }
        });

//        holder.imageview.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (globalSelectListener != null) {
//                    globalSelectListener.toView();
//                }
//            }
//        });

        if (getItem(position).isSelected()) {
            holder.setSelected();
            if (!hasSelectedList.contains(filepath))  hasSelectedList.add(filepath);
        }else{
           holder.setUnSelected();
            if (hasSelectedList.contains(filepath)) hasSelectedList.remove(filepath);
        }
        return convertView;
    }




    class ViewHolder {
        ImageView imageview;
        ImageView selectImg;
        public ViewHolder(View convertView) {
            selectImg = (ImageView) convertView.findViewById(R.id.browseritem_select);
            imageview = (ImageView) convertView.findViewById(R.id.gridview_item_imageview);
        }

        public void setSelected(){
            selectImg.setImageResource(R.mipmap.ic_checkbox_select_small);
            imageview.setColorFilter(selectColor);
        }

        public void setUnSelected(){
            selectImg.setImageResource(R.mipmap.ic_checkbox_unselect_small);
            imageview.setColorFilter(unselectColor);
        }
    }



    public void setGlobalSelectListener(GlobalSelectListener listener){
        globalSelectListener = listener;
    }

    public void recylce(){
        if (mLoader!=null)mLoader.recycle();
    }
}


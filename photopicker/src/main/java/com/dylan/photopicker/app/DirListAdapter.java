package com.dylan.photopicker.app;

/**
 * Created by AD on 2016/4/25.
 */

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dylan.photopicker.R;
import com.dylan.photopicker.api.PhotoDirectory;
import com.dylan.photopicker.api.ImageLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 显示所有包含图片的文件
 */
public class DirListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Set<String> mCheckBoxSet = new HashSet<String>();
    private DirSelectListener mListener;
    private List<PhotoDirectory> mDatas;
    private int currentIndex = 0;

    public DirListAdapter() {

    }

    public void bind(List<PhotoDirectory> objects) {
        if (objects != null && !objects.isEmpty()) {
            mDatas = objects;
            mCheckBoxSet.add(objects.get(0).getDirPath());
            notifyDataSetChanged();
        }

    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public PhotoDirectory getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            if (mInflater == null) mInflater = LayoutInflater.from(parent.getContext());
            convertView = mInflater.inflate(R.layout.photopicker_dir_listitem, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final PhotoDirectory bean = getItem(position);
        holder.mCheckBoxIv.setVisibility(View.GONE);
        holder.mImag.setImageResource(R.mipmap.ic_nopic);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(bean.getFirstChildPath(), holder.mImag);


        holder.mDirName.setText(bean.getDirName());
        holder.mDirChildCount.setText(bean.getChildCount() + "张");
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex = position;
                mCheckBoxSet.clear();
                holder.mCheckBoxIv.setVisibility(View.VISIBLE);
                mCheckBoxSet.add(bean.getDirPath());
                notifyDataSetChanged();
                if (mListener != null) {
                    mListener.selectDir(bean.getDirName(), bean.getDirPath());
                }

            }
        });

        if (mCheckBoxSet.contains(bean.getDirPath())) {
            holder.mCheckBoxIv.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckBoxIv.setVisibility(View.GONE);
        }

        if (currentIndex == position)
            holder.rootView.setBackgroundColor(Color.parseColor("#e5e5e5"));
        else
            holder.rootView.setBackgroundColor(0);
        return convertView;

    }

    class ViewHolder {
        View rootView;
        ImageView mImag;
        TextView mDirName;
        TextView mDirChildCount;
        ImageView mCheckBoxIv;

        public ViewHolder(View convertView) {
            rootView = convertView.findViewById(R.id.id_rootView);
            mImag = (ImageView) convertView.findViewById(R.id.id_folder_list_itemimg);
            mDirName = (TextView) convertView.findViewById(R.id.id_popup_foldername);
            mDirChildCount = (TextView) convertView.findViewById(R.id.id_folder_childcount);
            mCheckBoxIv = (ImageView) convertView.findViewById(R.id.id_popup_listview_item_checkbox);
        }
    }

    public interface DirSelectListener {
        void selectDir(String dirName, String dirpath);
    }

    public void setDirSelectListener(DirSelectListener listener) {
        mListener = listener;
    }

}
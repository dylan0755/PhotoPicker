package com.dylan.photopicker.api;

/**
 * Created by AD on 2016/4/25.
 */


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.dylan.library.io.FileUtils;
import com.dylan.library.media.MediaStoreLoader;
import com.dylan.library.media.PictureDirectory;
import com.dylan.library.utils.EmptyUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class PhotoPicker {
    private Context mContext;
    private Handler mHanlder;
    private List<PhotoDirectory> photoDirectoryList;
    private List<PhotoBean> mSelectList;
    private List<PhotoBean> mRecentlyList;//最近的
    private PhotoFileNameFilter mFilter;
    private static final int FIND_FINISH = 100;
    public static String defaultDirName = "最近照片";
    private static String default_dirpath = "/default";
    private long intervalMonth = 1;//最近时间 与当前月份间隔几个月
    private int currentMonth;


    public PhotoPicker(Context context) {
        currentMonth = new Date(System.currentTimeMillis()).getMonth();
        mContext = context;
        mSelectList = new ArrayList<>();
        mRecentlyList = new ArrayList<>();
        photoDirectoryList = new ArrayList<>();
        mFilter = new PhotoFileNameFilter();
        mHanlder = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == FIND_FINISH) { //查找完毕
                    findRecentlyWhileScanFinish();
                }
                super.handleMessage(msg);
            }

        };
    }


    /**
     * 间隔时间
     *
     * @param monthInterval 间隔几个月
     */
    public void setRecentInterval(int monthInterval) {
        intervalMonth = monthInterval;
    }


    /**
     * 获取所有图片文件夹
     */
    public void scanPicDirs() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(mContext, "当前SDcard不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread() {
            public void run() {
               List<PictureDirectory> directoryList=MediaStoreLoader.getPictureDirectoryFromImageStore(mContext);
                if (EmptyUtils.isNotEmpty(directoryList)){
                    for (PictureDirectory directory:directoryList){
                         photoDirectoryList.add(PhotoDirectory.dto(directory));
                    }
                }
                mHanlder.sendEmptyMessage(FIND_FINISH);
            }
        }.start();
    }






    /**
     * 显示最近图片
     */
    public void showRecently() {
        mSelectList.clear();
        if (mRecentlyList.isEmpty()) {
            List<File> recentList = new ArrayList<>();
            for (PhotoDirectory bean : photoDirectoryList) {
                if (defaultDirName.equals(bean.getDirName())) continue;
                recentList.addAll(findRecentlyInDir(bean));
            }
            sortPicFilesOrderByTime(recentList);
            //保存排序后的数据
            for (File f : recentList) {
                String path = f.getAbsolutePath();
                PhotoBean pb = new PhotoBean(path);
                mSelectList.add(pb);
                mRecentlyList.add(pb);
            }
        } else {
            mSelectList.addAll(mRecentlyList);
        }
        if (mOperatorListener != null) {
            mOperatorListener.loadFromDir(default_dirpath, mSelectList);
        }
    }


    /**
     * 选择文件夹
     *
     * @param dirPath
     */
    public void loadFromDir(String dirPath) {
        mSelectList.clear();
        File file = new File(dirPath);
        if (file.exists()) {
            File[] files = file.listFiles(mFilter);
            //对照片按时间排序
            List<File> sortList = Arrays.asList(files);
            sortPicFilesOrderByTime(sortList);
            //保存排序后的
            for (File f : sortList) {
                String path = f.getAbsolutePath();
                mSelectList.add(new PhotoBean(path));
            }
            if (mOperatorListener != null) {
                mOperatorListener.loadFromDir(dirPath, mSelectList);
            }
        }

    }


    /**
     * 1.对文件夹进行排序
     * 2.找出最近几个月的图片
     */
    private void findRecentlyWhileScanFinish() {
        //保存每个文件夹下符合最近x月要求的图片
        List<File> recentFiles = new ArrayList<>();
        for (PhotoDirectory bean : photoDirectoryList) {
            recentFiles.addAll(findRecentlyInDir(bean));
        }
        if (EmptyUtils.isNotEmpty(recentFiles)) {
            sortPicFilesOrderByTime(recentFiles);//排序
            for (File f : recentFiles) {
                mRecentlyList.add(new PhotoBean(f.getAbsolutePath()));
            }
        }
        //添加默认的文件夹
        PhotoDirectory bean = new PhotoDirectory();
        bean.setDirName(defaultDirName);
        bean.setDirPath(default_dirpath);
        if (EmptyUtils.isNotEmpty(mRecentlyList)) {
            bean.setChildCount(mRecentlyList.size());
            bean.setFirstChildPath(mRecentlyList.get(0).getPath());
        }

        photoDirectoryList.add(0, bean);
        if (mOperatorListener != null) {
            mOperatorListener.scanFinish(photoDirectoryList);
            if (EmptyUtils.isNotEmpty(mRecentlyList)) mOperatorListener.loadFromDir(default_dirpath, mRecentlyList);

        }

    }




    //找出该文件夹下最近几个月的图片
    private List<File> findRecentlyInDir(PhotoDirectory bean) {
        File dir = new File(bean.getDirPath());
        File[] files = dir.listFiles(mFilter);
        List<File> list = new ArrayList<>();
        Date date;
        if (files == null) return list;
        for (File f : files) {
            date = new Date(f.lastModified());
            int month = date.getMonth();
            if ((currentMonth - month) <= intervalMonth) {
                list.add(f);
            }
        }
        if (files.length!=bean.getChildCount()){
            notifyToScan(mContext,bean.getDirPath());
        }
        return list;
    }



    //对照片按时间排序
    private void sortPicFilesOrderByTime(List<File> recentList) {
        FileUtils.sortDESC(recentList);
    }

    /**
     * 图片过滤器
     */
    public class PhotoFileNameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            if (!filename.startsWith(".")) {
                String suffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length()).toLowerCase();
                return isPicture(suffix);
            }
            return false;
        }
    }


    private OperatorListener mOperatorListener;

    public interface OperatorListener {
        void scanFinish(List<PhotoDirectory> list);

        void loadFromDir(String dirPath, List<PhotoBean> picList);
    }

    public void setOperatorListener(OperatorListener listener) {
        mOperatorListener = listener;
    }

    /**
     * 根据后缀判断图片
     *
     * @param suffix
     * @return
     */
    private boolean isPicture(String suffix) {
        if (suffix.equals("jpg") || suffix.equals("png")
                || suffix.equals("jpeg") || suffix.equals("gif")) {
            return true;
        } else {
            return false;
        }
    }


    public void notifyToScan(Context context, String filePath) {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                FileUtils.notifyScanFile(context,filePath);
            } else {
                context.sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}



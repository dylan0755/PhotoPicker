package com.dylan.photopicker.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dylan on 2017/5/16.
 */

/**
 * 加载图片，查看大图，全屏查看
 */
public class FullScreenBitmapLoader {
   private Handler mHandler;
    private ExecutorService mThreadPool;
    private int screenWidth;
    private int screenHeight;
    public FullScreenBitmapLoader(){
        mHandler=new Handler(Looper.getMainLooper());
        mThreadPool= Executors.newFixedThreadPool(3);
    }
    public  void load(final Context context, final String absolutePath, final ImageView imageView) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig= Bitmap.Config.RGB_565;
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(absolutePath, opt);
                int picWidth = opt.outWidth;
                int picHeight = opt.outHeight;
                // 获取屏的宽度和高度
                if (screenWidth==0||screenHeight==0){
                    DisplayMetrics displayMetrics=context.getResources().getDisplayMetrics();
                    screenWidth= displayMetrics.widthPixels;
                    screenHeight= displayMetrics.heightPixels;
                }
                // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
                opt.inSampleSize =1;
                // 根据屏的大小和图片大小计算出缩放比例
                if (picWidth > picHeight) {
                    if (picWidth > screenWidth)
                        opt.inSampleSize = picWidth / screenWidth;
                } else {
                    if (picHeight > screenHeight)
                        opt.inSampleSize = picHeight / screenHeight;
                }
                // 这次再真正地生成一个有像素的，经过缩放了的bitmap
                opt.inJustDecodeBounds = false;
              final Bitmap bitmap= BitmapFactory.decodeFile(absolutePath, opt);
                if (bitmap!=null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }else{
                    Log.e("run: ", "bitmap==null");
                }
            }
        });

    }




}

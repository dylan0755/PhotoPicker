package com.dylan.photopicker.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ImageLoader {
    private static ImageLoader mInstance;
    private LruCache<String, Bitmap> mLruCache;
    private DisplayMetrics displayMetrics;
    //线程池
    private ExecutorService mThreadPool;
    private static final int DEFAULT_THREAD_COUNT = 1;

    //队列的调度方式
    public enum Type {
        FIFO, LIFO
    }

    private Type mType = Type.LIFO;
    private List<Runnable> mTaskQueue;
    //后台轮询线程
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    private Handler mUIHandler;
    private Semaphore mSemaphore = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;


    private ImageLoader(int mthreadCount, Type type) {
        init(mthreadCount, type);
    }

    private void init(int threadCount, Type type) {
        initLruCache();
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        //去线程池中取出一个任务进行执行
                        Runnable runnable = getTask();
                        if (runnable == null) return;
                        mThreadPool.execute(runnable);
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放信号量
                mSemaphore.release();
                Looper.loop();
            }

        };
        mPoolThread.start();

        //创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        LinkedList<Runnable> list=new LinkedList();
        mTaskQueue = Collections.synchronizedList(list);//加上同步，避免 在getTask()报NoSuchElementException
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    private void initLruCache() {
        int MaxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = MaxMemory / 6;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    private Runnable getTask() {
        try {
            if (mType == Type.FIFO) {
                return mTaskQueue.remove(0);
            } else if (mType == Type.LIFO) {
                if (mTaskQueue.size()!=0){
                    return mTaskQueue.remove(mTaskQueue.size()-1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEFAULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    public void loadImage(final String path, final ImageView imageview) {
        if (imageview==null)return;
        imageview.setTag(path);
        imageview.setImageBitmap(null);//设置空白，避免滑动出现闪烁问题。
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //获取到图片，为imageview回调设置图片
                    ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageview = holder.imageview;
                    String path = holder.path;
                    boolean animable = holder.animable;
                    if (imageview.getTag().toString().equals(path)) {
                        imageview.setImageBitmap(bm);
                        if (animable) {
                            AlphaAnimation mShowAnimation = getAlphaAnimation();
                            imageview.startAnimation(mShowAnimation);
                        }
                    }
                }
            };
        }

        final Bitmap bitmap = getBitmapFromCache(path);
        if (bitmap != null) {
            refreshBitmap(false, path, bitmap, imageview);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    //加载 图片
                    ImageSize imagesize = getImageSize(imageview);
                    //压缩
                    Bitmap bm = decodeSampleBitmap(path, imagesize.width, imagesize.height);
                    //添加到缓存
                    addBitmapToCache(path, bm);
                    refreshBitmap(true, path, bm, imageview);
                    mSemaphoreThreadPool.release();
                }
            });
        }

    }

    /**
     * 渐入动画
     *
     * @return
     */
    @NonNull
    private AlphaAnimation getAlphaAnimation() {
        AlphaAnimation mShowAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowAnimation.setDuration(250);
        mShowAnimation.setFillAfter(true);
        return mShowAnimation;
    }

    private void refreshBitmap(boolean anim, String path, Bitmap bitmap, ImageView imageview) {
        Message msg = Message.obtain();
        ImageBeanHolder holder = new ImageBeanHolder();
        holder.bitmap = bitmap;
        holder.path = path;
        holder.imageview = imageview;
        holder.animable = anim;
        msg.obj = holder;
        mUIHandler.sendMessage(msg);
    }

    private Bitmap decodeSampleBitmap(String path, int width, int height) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig= Bitmap.Config.RGB_565;
        option.inJustDecodeBounds = true;//不添加到内存，只测量宽高
        BitmapFactory.decodeFile(path, option);
        option.inSampleSize = caculateInSampleSize(option, width, height);
        option.inJustDecodeBounds = false;//加载图片到内存
        Bitmap bitmap = BitmapFactory.decodeFile(path, option);
        return bitmap;
    }

    //根据需求的宽和高以及图片实际的宽高计算SampleSize
    public int caculateInSampleSize(Options option, int reqWith, int reqHeight) {
        int width = option.outWidth;
        int height = option.outHeight;
        int inSampleSize = 1;
        if (width > reqWith || height > reqHeight) {
            int widthRatio = Math.round(width * 1.0f / reqWith);
            int heightRatio = Math.round(height * 1.0f / reqHeight);
            inSampleSize = Math.min(widthRatio, heightRatio); //这里取最小的缩放比例，取最大的话图片就太模糊了
        }
        return inSampleSize;
    }

    public synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler == null) {
                //在此会阻塞，知道有mSemaphore.release();
                //相当于
                mSemaphore.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0);
    }

    private void addBitmapToCache(String path, Bitmap bitmap) {
        if (getBitmapFromCache(path) == null) {
            if (bitmap != null) {
                mLruCache.put(path, bitmap);
            }
        }
    }

    private Bitmap getBitmapFromCache(String key) {
        if (mLruCache == null) {
            initLruCache();
        }
        return mLruCache.get(key);
    }

    public class ImageSize {
        int width;
        int height;
    }

    protected ImageSize getImageSize(ImageView imageview) {
        if (displayMetrics==null){
            displayMetrics= imageview.getContext().getResources().getDisplayMetrics();
        }
        ImageSize imagesize = new ImageSize();
        LayoutParams lp = imageview.getLayoutParams();
        int width = imageview.getWidth();//获取实际宽度
        if (width <= 0) {
            width = lp.width;//获取在布局中的宽度
        }
        if (width <= 0) {
            width = getImageViewFileValue(imageview, "mMaxWidth");//检查最大值
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;//设置为屏幕的宽度
        }

        int height = imageview.getHeight();
        if (height <= 0) {
            height = lp.height;
        }
        if (height <= 0) {
            height = getImageViewFileValue(imageview, "mMaxHeight");
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        imagesize.width = width;
        imagesize.height = height;
        return imagesize;
    }

    //反射获取属性值
    private static int getImageViewFileValue(Object object, String fieldName) {
        int value = 0;
        Field field = null;
        try {
            field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public void recycle() {
        if (mLruCache != null && mLruCache.size() > 0) {
            mLruCache.evictAll();
            mLruCache = null;
            mInstance = null;
        }
    }


    private class ImageBeanHolder {
        Bitmap bitmap;
        ImageView imageview;
        String path;
        boolean animable;
    }

}

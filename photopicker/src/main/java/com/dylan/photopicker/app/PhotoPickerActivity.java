package com.dylan.photopicker.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dylan.photopicker.R;
import com.dylan.photopicker.api.BasePickerActivity;
import com.dylan.photopicker.api.PhotoDirectory;
import com.dylan.photopicker.api.PhotoBean;
import com.dylan.photopicker.api.PhotoPicker;
import com.dylan.photopicker.api.listener.BrowserOperatorListener;
import com.dylan.photopicker.utils.PermissionUtils;
import com.dylan.photopicker.utils.ScreenUtils;

import java.io.File;
import java.util.List;

/**
 * Created by AD on 2016/4/25.
 */
public class PhotoPickerActivity extends BasePickerActivity implements View.OnClickListener, BrowserOperatorListener {
    public static final String IMAGE_SELECT_ARRAY = "imgpaths";
    public static final String EXTRA_MAX_SELECT = "maxSelectCount";
    public static final String EXTRA_LIMIT_SELECT_TIP = "limitSelectTip";
    public static final String EXTRA_NEED_SHOW_SELECT_LIMIT = "needshowlimit";
    public static final String EXTRA_SHOW_CAMERA_MULTI_CHOICE="showCamera";
    private static final int REQUEST_CAMERA_ACTIVITY = 1001;
    private static final int REQUEST_CAMERA_PERMISION = 2001;
    private static final int REQUEST_READ_PERMISION = 3001;
    private static final int REQUEST_APPSETTING_STORAGE = 4001;
    private static final int REQUEST_APPSETTING_CAMERA = 5001;
    private int maxSelectCount = 1;
    private GridView mGridView;
    private ImageView expandimv;
    private ListView mListView;
    private TextView tv_dir_name;
    private TextView tv_finish_picker;
    private TextView tv_message;
    private LinearLayout llOpenDrawTop;
    private DrawerLayout drawerLayout;
    private LinearLayout draw_right_layout;
    private LinearLayout permission_request_layout;
    private boolean isexpand = false;
    private DirListAdapter dirAdapter;
    private SelectPictureAdapter selectPicAdapter;
    private PhotoPicker mPicker;
    private PhotoBrowserFragment mBrowserFragment;
    private String tempPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ScreenUtils.setStatusBarLightMode(getWindow(),Color.WHITE);
        }
        setContentView(R.layout.picker_main);
        mBrowserFragment = new PhotoBrowserFragment();
        mBrowserFragment.setDismissListener(this);
        mBrowserFragment.setGlobalSelectListener(this);
        initView();
        parseIntentData();
        initAdapter();
    }


    private void parseIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            //设置最大选择数量
             maxSelectCount = intent.getIntExtra(EXTRA_MAX_SELECT, 1);
            if (maxSelectCount == 1) {
                findViewById(R.id.iv_open_camera).setVisibility(View.GONE);
                findViewById(R.id.llMultiChooseBottomBar).setVisibility(View.GONE);
                findViewById(R.id.llOpenDrawTop).setVisibility(View.VISIBLE);
                findViewById(R.id.llOpenDrawTop).setOnClickListener(this);
            }
            if (maxSelectCount > 1) {
                boolean needShowCamera=intent.getBooleanExtra(EXTRA_SHOW_CAMERA_MULTI_CHOICE, false);
                if (needShowCamera)findViewById(R.id.iv_open_camera).setVisibility(View.VISIBLE);

            }
            setMaxSelectCount(maxSelectCount);

        }
    }


    public void initAdapter() {
        mPicker = new PhotoPicker(this);
        mPicker.setRecentInterval(2);
        //显示文件夹的适配器
        dirAdapter = new DirListAdapter();
        mListView.setAdapter(dirAdapter);
        //显示图片的适配器
        selectPicAdapter = new SelectPictureAdapter(this);
        selectPicAdapter.setGlobalSelectListener(this);
        boolean needShowLimit = getIntent().getBooleanExtra(EXTRA_NEED_SHOW_SELECT_LIMIT, true);
        String limitTip = getIntent().getStringExtra(EXTRA_LIMIT_SELECT_TIP);
        selectPicAdapter.setShowLimitSelectTip(needShowLimit, limitTip);
        selectPicAdapter.isSingleChoice(maxSelectCount==1);

        mGridView.setAdapter(selectPicAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String picPath = selectPicAdapter.getItem(position).getPath();
                if (maxSelectCount>1){
                    mBrowserFragment.show(getSupportFragmentManager(), picPath, selectPicAdapter.getDatas(), position);
                }else{//单选返回
                    String[] imgs=new String[1];
                    imgs[0]=picPath;
                    Intent intent = getIntent();
                    intent.putExtra(IMAGE_SELECT_ARRAY, imgs);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });

        //文件夹选择
        dirAdapter.setDirSelectListener(new DirListAdapter.DirSelectListener() {
            @Override
            public void selectDir(String dirName, String dirpath) {
                if (PhotoPicker.defaultDirName.equals(dirName)) {
                    mPicker.showRecently();
                } else {
                    mPicker.loadFromDir(dirpath);
                }
                tv_dir_name.setText(dirName);
                toggle();
            }
        });

        //扫描包含图片文件夹，
        mPicker.setOperatorListener(new PhotoPicker.OperatorListener() {
            @Override
            public void scanFinish(List<PhotoDirectory> list) {
                dirAdapter.bind(list);
                tv_dir_name.setText(PhotoPicker.defaultDirName);
            }

            @Override
            public void loadFromDir(String dirPath, List<PhotoBean> picList) {
                selectPicAdapter.bind(dirPath, picList);
            }

        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (!PermissionUtils.hasStoragePermission(this)) {
                ActivityCompat.requestPermissions(this, PermissionUtils.storagePermission, REQUEST_READ_PERMISION);
                return;
            }
        }
        mPicker.scanPicDirs();//扫描文件夹

    }


    public void setHasSelectText() {
        if (selectCount != 0) {
            tv_finish_picker.setText("完成" + "(" + selectCount + "/" + maxSelectCount + ")");
            tv_finish_picker.setTextColor(Color.WHITE);
            tv_finish_picker.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_solid_green_select));
        } else {
            tv_finish_picker.setText("完成");
            tv_finish_picker.setTextColor(Color.parseColor("#6c9f6e"));
            tv_finish_picker.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_solid_green_unselect));
        }

    }

    public void initView() {
        findViewById(R.id.iv_back).setOnClickListener(this);
        tv_dir_name = (TextView) findViewById(R.id.left_title);
        tv_finish_picker = (TextView) findViewById(R.id.bt_finish_picker);
        tv_finish_picker.setOnClickListener(this);
        tv_message = (TextView) findViewById(R.id.tv_permission_message);
        findViewById(R.id.iv_open_camera).setOnClickListener(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.picker_drawer_ly);
        draw_right_layout = (LinearLayout) findViewById(R.id.draw_right_layout);
        permission_request_layout = (LinearLayout) findViewById(R.id.ll_permission_request);
        mGridView = (GridView) findViewById(R.id.id_gridview);
        mListView = (ListView) findViewById(R.id.right_folder_listview);
        expandimv = (ImageView) findViewById(R.id.bt_expand_drawer);
        llOpenDrawTop = (LinearLayout) findViewById(R.id.llOpenDrawTop);
        expandimv.setOnClickListener(this);
        drawerLayout.setDrawerListener(new MyDrawerListner());
        tv_finish_picker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (selectCount == 0) return true;  //没有选择图片不能完成
                }
                return false;
            }
        });
    }


    //AndroidLibrary中不能使用switch -case 语句访问资源id，改成if else
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            Intent intent = getIntent();
            intent.putExtra(IMAGE_SELECT_ARRAY, new String[]{});
            this.setResult(RESULT_OK, intent);
            this.finish();
        } else if (v.getId() == R.id.bt_expand_drawer || v.getId() == R.id.llOpenDrawTop) {
            toggle();
        } else if (v.getId() == R.id.bt_finish_picker) {
            String[] imgs = new String[selectPicAdapter.hasSelectedList.size()];
            int i = 0;
            for (String path : selectPicAdapter.hasSelectedList) {
                imgs[i] = path;
                i++;
            }
            Intent intent = getIntent();
            intent.putExtra(IMAGE_SELECT_ARRAY, imgs);
            this.setResult(RESULT_OK, intent);
            this.finish();
        } else if (v.getId() == R.id.iv_open_camera) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // 设置系统相机拍照后的输出路径
                // 创建临时文件
                if (Build.VERSION.SDK_INT >= 23) {
                    //权限还没有授予，需要在这里写申请权限的代码
                    if (!PermissionUtils.hasCameraStoragePermission(this)) {
                        ActivityCompat.requestPermissions(this, PermissionUtils.camerStoragePermission, REQUEST_CAMERA_PERMISION);
                        return;
                    }
                }
                openCamera();
            } else {
                Toast.makeText(getApplicationContext(), "打开相机失败", Toast.LENGTH_SHORT).show();
            }
        }

    }


    /**
     * 有权限了才能调用相机
     */
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final File imageStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + "photopicker" + File.separator + "temp");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File[] files = imageStorageDir.listFiles();
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            if (file != null) file.delete();
                        }
                    }
                }
            }).start();
        }
        tempPath = imageStorageDir + File.separator + "photopicker_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(tempPath);
        Uri imageUri = Uri.fromFile(file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, REQUEST_CAMERA_ACTIVITY);
    }


    @Override
    public void dismiss(List<PhotoBean> beanList) {
        if (selectPicAdapter != null) selectPicAdapter.bindInCurrentDir(beanList);

    }

    @Override
    public int select(String dirPath, String picPath) {
        if (selectCount != maxSelectCount) {
            selectCount++;
            setHasSelectText();
            return 0;
        } else {
            return maxSelectCount;
        }

    }

    @Override
    public int unselect(String dirPath, String picPath) {
        selectCount--;
        setHasSelectText();
        Log.e("unselect: ", "hasSelect:" + selectCount);
        return selectCount;
    }


    class MyDrawerListner implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {
            isexpand = true;
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            isexpand = false;
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }

    public void toggle() {
        if (!isexpand) {
            drawerLayout.openDrawer(draw_right_layout);
            isexpand = true;
        } else {
            drawerLayout.closeDrawer(draw_right_layout);
            isexpand = false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA_ACTIVITY) {
            String[] imagArray=null;
            if (tempPath != null && !tempPath.isEmpty()) {
                File file = new File(tempPath);
                if (file.exists() && file.length() > 0) {
                    imagArray= new String[1];
                    imagArray[0] = tempPath;
                }
            }
            Intent intent = getIntent();
            intent.putExtra(IMAGE_SELECT_ARRAY, imagArray);
            this.setResult(RESULT_OK, intent);
            this.finish();
        } else if (requestCode == REQUEST_APPSETTING_STORAGE) {
            if (!PermissionUtils.hasStoragePermission(this)) {
                Toast.makeText(this, "没有开启存储权限，无法读取照片信息", Toast.LENGTH_SHORT).show();
            } else {
                mPicker.scanPicDirs();
            }
        } else if (requestCode == REQUEST_APPSETTING_CAMERA) {
            if (!PermissionUtils.hasCameraStoragePermission(this)) {
                Toast.makeText(this, "没有开启相机，存储权限，无法使用相机", Toast.LENGTH_SHORT).show();
            } else {
                openCamera();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
                hidePermissionLayout();
            } else {
                String message = "检测到您没有开启相机或存储权限，是否前往权限管理开启？";
                showPermissionLayout(message, REQUEST_APPSETTING_CAMERA);
            }
        } else if (requestCode == REQUEST_READ_PERMISION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//判断是否有读的权限，有则开始扫描
                mPicker.scanPicDirs();//开始扫描
                hidePermissionLayout();
            } else {
                String message = "检测到您没有开启存储权限，是否前往权限管理开启？";
                showPermissionLayout(message, REQUEST_APPSETTING_STORAGE);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showPermissionLayout(String message, final int requestCode) {
        if (permission_request_layout.getVisibility() != View.VISIBLE) {
            Animation animator = AnimationUtils.loadAnimation(this, R.anim.layout_anim_enter_frombottom);
            permission_request_layout.setVisibility(View.VISIBLE);
            permission_request_layout.startAnimation(animator);
            tv_message.setText(message);
            permission_request_layout.findViewById(R.id.tv_go).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hidePermissionLayout();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, requestCode);
                }
            });
        }
    }

    private void hidePermissionLayout() {
        if (permission_request_layout.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.layout_anim_exit_tobottom);
            permission_request_layout.setAnimation(animation);
            permission_request_layout.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    permission_request_layout.setVisibility(View.GONE);
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (selectPicAdapter != null) selectPicAdapter.recylce();
    }


    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(IMAGE_SELECT_ARRAY, new String[]{});
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

}

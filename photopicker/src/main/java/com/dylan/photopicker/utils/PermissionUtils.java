package com.dylan.photopicker.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Dylan on 2017/11/4.
 */

public class PermissionUtils {
    public static String[] camerStoragePermission = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static String[] storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,};

    public static boolean hasCameraStoragePermission(Activity activity) {
        int hasCameraPermison = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int hasWritePermison = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //权限还没有授予，需要在这里写申请权限的代码
        if (hasCameraPermison != PackageManager.PERMISSION_GRANTED || hasWritePermison != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasStoragePermission(Activity activity) {
        int hasReadPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int hasWritePermison = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //权限还没有授予，需要在这里写申请权限的代码
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED || hasWritePermison != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }
}

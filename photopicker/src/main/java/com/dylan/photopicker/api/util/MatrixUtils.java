package com.dylan.photopicker.api.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Author: Dylan
 * Date: 2019/7/31
 * Desc:
 */

public class MatrixUtils {

    //四个顶点的坐标
    public static PointF[] getLocation(Matrix matrix, Rect rect) {
        if (matrix == null || rect == null) return null;
        RectF rectF = new RectF(0, 0, rect.width() * 1.0f, rect.height() * 1.0f);
        PointF[] points = new PointF[4];
        float[] location = new float[9];
        matrix.getValues(location);
        //左上角
        points[0]=new PointF(location[2],location[5]) ;
        //右上角
        points[1] =new PointF(location[2] + rectF.width() * location[0],points[0].y) ;
        //左下角
        points[2] =new PointF(points[0].x,location[5] + rectF.height() * location[4]) ;
        //右下角
        points[3] =new PointF(points[1].x,points[2].y);
        return points;
    }


    //四个顶点的坐标
    public static PointF[] getLocation(Matrix matrix, Bitmap bitmap) {
        if (matrix == null || bitmap == null) return null;
        RectF rectF = new RectF(0, 0, bitmap.getWidth() * 1.0f, bitmap.getHeight() * 1.0f);
        PointF[] points = new PointF[4];
        float[] location = new float[9];
        matrix.getValues(location);
        //左上角
        points[0]=new PointF(location[2],location[5]) ;
        //右上角
        points[1] =new PointF(location[2] + rectF.width() * location[0],points[0].y) ;
        //左下角
        points[2] =new PointF(points[0].x,location[5] + rectF.height() * location[4]) ;
        //右下角
        points[3] =new PointF(points[1].x,points[2].y);
        return points;
    }

    //X,Y的缩放倍率
    public static PointF getScaleSize(Matrix matrix) {
        float[] location = new float[9];
        matrix.getValues(location);
        PointF pointF=new PointF();
        pointF.x=location[0];
        pointF.y=location[4];
        return pointF;
    }

    //左上角坐标
    public static PointF getLefTop(Matrix matrix){
        float[] location = new float[9];
        matrix.getValues(location);
        PointF pointF=new PointF();
        pointF.x=location[2];
        pointF.y=location[5];
        return pointF;
    }


    //缩小至最大范围
    public static Matrix zoomOutToMaxRange(Matrix matrix, Bitmap bm, float rangeWidth, float rangeHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        //缩放
        if (width > rangeWidth && height > rangeHeight) {//宽高都超过规定宽高
            float scale;
            float wScale = width * 1.0f / rangeWidth;
            float hScale = height * 1.0f / rangeHeight;
            scale = Math.max(wScale, hScale);
            scale = 1.0f / scale;
            matrix.postScale(scale, scale);
        } else if (width > rangeWidth && height == rangeHeight) {//宽大于规定宽，高等于规定高，缩至规定宽
            float wScale = width * 1.0f / rangeWidth;
            float scale = 1.0f / wScale;
            matrix.postScale(scale, scale);
        } else if (width > rangeWidth && height < rangeHeight) {//宽大于规定宽，高小于规定高
            float scale;
            float wScale = width * 1.0f / rangeWidth;
            scale = 1.0f / wScale;
            matrix.postScale(scale, scale);
        } else if (width <= rangeWidth && height > rangeHeight) {//图片高大于规定高度，而宽度没有，则图片缩至规定高度
            float hScale = height * 1.0f / rangeHeight;
            float scale = 1.0f / hScale;
            matrix.postScale(scale, scale);
        } else if (width < rangeWidth && height < rangeHeight) {//宽高都小于规定宽高
            float scale;
            if (width == height || width > height) {
                scale = width * 1.0f / rangeWidth;
            } else {
                float wScale = width * 1.0f / rangeWidth;
                float hScale = height * 1.0f / rangeHeight;
                scale = Math.min(wScale, hScale);
            }
            scale = 1.0f / scale;
            matrix.postScale(scale, scale);
        }
        return matrix;
    }


    //在范围内居中
    public static Matrix centerInRange(Matrix mMatrix, Bitmap bitmap, float rangeWidth, float rangeHeight) {
        Matrix m = new Matrix();
        m.set(mMatrix);
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        m.mapRect(rect);
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0, deltaY = 0;
        // 图片小于控件大小，则居中显示。大于控件，上方留空则往上移，下方留空则往下移
        if (height < rangeHeight) {
            deltaY = (rangeHeight - height) / 2 - rect.top;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < rangeHeight) {
            deltaY = rangeHeight - rect.bottom;
        }


        if (width < rangeWidth) {
            deltaX = (rangeWidth - width) / 2 - rect.left;
        } else if (rect.left > 0) {
            deltaX = -rect.left;
        } else if (rect.right < rangeWidth) {
            deltaX = rangeWidth - rect.right;
        }
        mMatrix.postTranslate(deltaX, deltaY);
        return mMatrix;
    }


}

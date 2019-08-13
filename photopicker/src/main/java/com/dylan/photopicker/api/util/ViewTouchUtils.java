package com.dylan.photopicker.api.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.logging.Logger;

import static android.content.ContentValues.TAG;

/**
 * Created by Dylan on 2017/5/25.
 */

public class ViewTouchUtils {

    // 求两点距离， 两点间距离公式
    public static float spacing(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y); // 两点间距离公式(注意这里的X,Y都是差值) // 两点间距离公式(注意这里的X,Y都是差值)
    }


    public static float spacing(PointF startPoint, PointF endPoint) {
        float x = 0;
        float y = 0;
        try {
            x = startPoint.x - endPoint.x;
            y = startPoint.y - endPoint.y;
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y); // 两点间距离公式(注意这里的X,Y都是差值) // 两点间距离公式(注意这里的X,Y都是差值)
    }
    // 求两点间中点

    public static PointF midPoint(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        PointF pointF = new PointF();
        pointF.set(x / 2, y / 2);
        return pointF;
    }

    public static void midPoint(PointF pointF, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        pointF.set(x / 2, y / 2);
    }




    public static float checkDxBound(float dx, Matrix matrix, Bitmap bitmap, ImageView imageView) {
        float[] values = new float[9];
        matrix.getValues(values);
        float width = imageView.getWidth();
        float bmWidth = bitmap.getWidth();
        if (bmWidth * values[Matrix.MSCALE_X] < width)
            return 0;
        if (values[Matrix.MTRANS_X] + dx > 0)
            dx = -values[Matrix.MTRANS_X];
        else if (values[Matrix.MTRANS_X] + dx < -(bmWidth * values[Matrix.MSCALE_X] - width))
            dx = -(bmWidth * values[Matrix.MSCALE_X] - width) - values[Matrix.MTRANS_X];
        return dx;
    }

    public static float checkDyBound(float dy, Matrix matrix, Bitmap bitmap, ImageView imageView) {
        float[] values = new float[9];
        matrix.getValues(values);
        float height = imageView.getHeight();
        float bmHeight = bitmap.getHeight();
        if (bmHeight * values[Matrix.MSCALE_Y] < height)
            return 0;
        if (values[Matrix.MTRANS_Y] + dy > 0)
            dy = -values[Matrix.MTRANS_Y];
        else if (values[Matrix.MTRANS_Y] + dy < -(bmHeight * values[Matrix.MSCALE_Y] - height))
            dy = -(bmHeight * values[Matrix.MSCALE_Y] - height) - values[Matrix.MTRANS_Y];
        return dy;
    }

}

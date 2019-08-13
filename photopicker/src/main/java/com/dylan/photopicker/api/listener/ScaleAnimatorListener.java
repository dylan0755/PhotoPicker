package com.dylan.photopicker.api.listener;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Matrix;

/**
 * Created by Dylan on 2017/5/31.
 */

public abstract class ScaleAnimatorListener implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {
    private Matrix matrix;
    private float value;

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public  void onAnimationEnd(Animator animation){
        onAnimationEnd(animation ,matrix);
    };

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }


    public float getAnimatorValue(){
        return value;
    }

    @Override
    public  void onAnimationUpdate(ValueAnimator animation){
        float value= (float) animation.getAnimatedValue();
        this.value=value;
        matrix=onAnimationUpdate(value);

    };

    public abstract void onAnimationEnd(Animator animation,Matrix matrix);
    public abstract  Matrix onAnimationUpdate(float scaleValue);
}



